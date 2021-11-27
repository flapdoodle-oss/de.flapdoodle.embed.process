/**
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
package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.process.config.store.ProxyFactory;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.processg.config.store.DownloadConfig;
import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.net.UrlStreams;
import de.flapdoodle.embed.processg.runtime.Name;
import de.flapdoodle.embed.processg.store.ArchiveStore;
import de.flapdoodle.embed.processg.store.Downloader;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.types.ThrowingFunction;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Value.Immutable
public abstract class DownloadPackage implements Transition<Archive> {

	@Value.Default
	protected StateID<Name> name() {
		return StateID.of(Name.class);
	}

	protected abstract ArchiveStore archiveStore();

	@Value.Default
	protected UrlStreams.DownloadCopyListener downloadCopyListener() {
		return (bytesCopied, contentLength) -> {
		};
	}

	@Value.Default
	protected DownloadConfig downloadConfig() {
		return DownloadConfig.defaults();
	}

	@Value.Default
	protected ThrowingFunction<String, Path, IOException> tempDir() {
		return Files::createTempDirectory;
	}

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	protected StateID<Package> distPackage() {
		return StateID.of(Package.class);
	}

	@Override
	@Value.Default
	public StateID<Archive> destination() {
		return StateID.of(Archive.class);
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(distribution(), distPackage(), name());
	}

	@Override
	@Value.Auxiliary
	public State<Archive> result(StateLookup lookup) {
		Distribution dist = lookup.of(distribution());
		Package distPackage = lookup.of(distPackage());
		Name name = lookup.of(name());

		Optional<Path> archive = archiveStore().archiveFor(name.value(), dist, distPackage.archiveType());
		if (archive.isPresent()) {
			return State.of(archive.map(Archive::of).get());
		} else {
			Path downloadedArchive = Try.function(tempDir())
				.mapCheckedException(cause -> new IllegalStateException("could not create archive path", cause))
				.apply(name.value())
				.resolve(UUID.randomUUID().toString());

			Try.runable(() -> {
					URL downloadUrl = new URL(distPackage.url());
					URLConnection connection = UrlStreams.urlConnectionOf(downloadUrl, downloadConfig().getUserAgent(), downloadConfig().getTimeoutConfig(),
						downloadConfig().proxyFactory().map(ProxyFactory::createProxy));
					UrlStreams.downloadTo(connection, downloadedArchive, downloadCopyListener());
				}).mapCheckedException(cause -> new IllegalStateException("could not download "+distPackage.url(), cause))
				.run();

			Path storedArchive = Try.supplier(() -> archiveStore().store(name.value(), dist, distPackage.archiveType(), downloadedArchive))
				.mapCheckedException(cause -> new IllegalArgumentException("could not store downloaded artifact", cause))
				.get();
			
			return State.of(Archive.of(storedArchive), it -> {
				Try.run(() -> Files.delete(it.value()));
			});
		}
	}

	public static ImmutableDownloadPackage with(ArchiveStore archiveStore) {
		return builder().archiveStore(archiveStore).build();
	}

	public static ImmutableDownloadPackage.Builder builder() {
		return ImmutableDownloadPackage.builder();
	}

}
