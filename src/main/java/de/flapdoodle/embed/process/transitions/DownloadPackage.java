/*
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.transitions;

import de.flapdoodle.embed.process.config.DownloadConfig;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import de.flapdoodle.embed.process.net.DownloadToPath;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.io.directories.TempDir;
import de.flapdoodle.embed.process.net.UrlStreams;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.DownloadCacheGuessStorePath;
import de.flapdoodle.embed.process.types.Archive;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.net.ProxyFactory;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Value.Immutable
public abstract class DownloadPackage implements Transition<Archive>, HasLabel {
	@Override
	@Value.Default
	public String transitionLabel() {
		return "DownloadPackage";
	}

	@Value.Default
	protected StateID<Name> name() {
		return StateID.of(Name.class);
	}

	@Value.Default
	protected StateID<DownloadCache> downloadCache() {
		return StateID.of(DownloadCache.class);
	}

	@Value.Default
	protected StateID<ProgressListener> progressListener() { return StateID.of(ProgressListener.class); }

	@Value.Default
	protected DownloadConfig downloadConfig() {
		return DownloadConfig.defaults();
	}

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	protected StateID<Package> distPackage() {
		return StateID.of(Package.class);
	}

	@Value.Default
	protected StateID<TempDir> tempDirectory() {
		return StateID.of(TempDir.class);
	}

	@Override
	@Value.Default
	public StateID<Archive> destination() {
		return StateID.of(Archive.class);
	}

	@Value.Default
	public DownloadToPath downloadToPath() {
		return UrlStreams.asDownloadToPath();
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(distribution(), distPackage(), name(), tempDirectory(), downloadCache(), progressListener());
	}

	@Override
	@Value.Auxiliary
	public State<Archive> result(StateLookup lookup) {
		Distribution dist = lookup.of(distribution());
		Package distPackage = lookup.of(distPackage());
		DownloadCache downloadCache = lookup.of(downloadCache());
		ProgressListener progressListener = lookup.of(progressListener());
		Name name = lookup.of(name());
		TempDir temp = lookup.of(tempDirectory());

		URL downloadUrl = Try.supplier(() -> new URL(distPackage.url()))
			.mapToUncheckedException(RuntimeException::new)
			.get();

		Optional<Path> archive = downloadCache.archiveFor(downloadUrl, distPackage.archiveType());
		if (archive.isPresent()) {
			return State.of(archive.map(Archive::of).get());
		} else {
			Path downloadedArchive = Try.supplier(() -> temp.createDirectory(name.value()))
				.mapToUncheckedException(cause -> new IllegalStateException("could not create archive path", cause))
				.get()
				.resolve(UUID.randomUUID().toString());

			Try.runable(() -> {
					downloadToPath().download(
						downloadUrl,
						downloadedArchive,
						downloadConfig().proxyFactory().map(ProxyFactory::create),
						downloadConfig().getUserAgent(),
						downloadConfig().getTimeoutConfig(),
						DownloadToPath.downloadCopyListenerDelegatingTo(progressListener)
					);
				}).mapToUncheckedException(cause -> {
					String hint="";
					if (downloadCache instanceof DownloadCacheGuessStorePath) {
						Path destinationPath = ((DownloadCacheGuessStorePath) downloadCache).archivePath(downloadUrl, distPackage.archiveType());
						hint=" (if this issue persist, you can download it manually to "+destinationPath+")";
					}

					if (cause instanceof SocketTimeoutException && cause.getLocalizedMessage().contains("onnect timed out")) {
						return new IllegalStateException("could not download " + distPackage.url()+", ensure that no firewall or vpn is blocking this connection"+hint, cause);
					}
					return new IllegalStateException("could not download " + distPackage.url()+hint, cause);
				})
				.run();

			Path storedArchive = Try.supplier(() -> downloadCache.store(downloadUrl, distPackage.archiveType(), downloadedArchive))
				.mapToUncheckedException(cause -> new IllegalArgumentException("could not store downloaded artifact", cause))
				.get();
			
			return State.of(Archive.of(storedArchive), it -> {
				Try.run(() -> Files.delete(downloadedArchive));
			});
		}
	}

	public static ImmutableDownloadPackage.Builder builder() {
		return ImmutableDownloadPackage.builder();
	}

	public static ImmutableDownloadPackage withDefaults() {
		return builder().build();
	}
}
