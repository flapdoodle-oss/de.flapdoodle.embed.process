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

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.HttpServers;
import de.flapdoodle.embed.process.Resources;
import de.flapdoodle.embed.process.config.DownloadConfig;
import de.flapdoodle.embed.process.config.TimeoutConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.DownloadCacheGuessStorePath;
import de.flapdoodle.embed.process.types.Archive;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.os.CommonOS;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.types.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownloadPackageTest {

	@Test
	void successfullyDownloadPackage(@TempDir Path tempDir) throws IOException, URISyntaxException {
		Map<String, String> map=new LinkedHashMap<>();
		map.put("/archive.zip","/archives/sample.zip");
		
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), map)) {
			ImmutableDownloadPackage testee = DownloadPackage.withDefaults();
			StateLookup statelookup = stateLookupOf(
				distribution(),
				packageOf(server.serverUrl() + "/archive.zip"),
				downloadCache(tempDir),
				state(ProgressListener.class, new StandardConsoleProgressListener()),
				state(Name.class, Name.of("noop")),
				state(de.flapdoodle.embed.process.io.directories.TempDir.class,
					de.flapdoodle.embed.process.io.directories.TempDir.of(tempDir))
			);

			State<Archive> result = testee.result(statelookup);
			assertThat(result.value().value())
				.isReadable()
				.hasSameBinaryContentAs(Resources.resourcePath(getClass(),"/archives/sample.zip"));
		}
	}
	private static Pair<StateID<DownloadCache>, State<DownloadCache>> downloadCache(Path tempDir) {
		return state(DownloadCache.class, new StoreArchiveInPath(tempDir.resolve("cachedArtifact.zip")));
	}

	@Test
	void enhanceExceptionOnConnectionTimeout(@TempDir Path tempDir) throws URISyntaxException {
		ImmutableDownloadPackage testee = DownloadPackage.withDefaults()
			.withDownloadConfig(DownloadConfig.defaults()
				.withTimeoutConfig(TimeoutConfig.defaults()
					.withConnectionTimeout(10)));

		Path cachedArtifactPath = tempDir.resolve("cachedArtifact.zip");

		String url = "http://example.com:81/";
		StateLookup statelookup = stateLookupOf(
			distribution(),
			packageOf(url),
			state(DownloadCache.class, new StoreArchiveInPath(cachedArtifactPath)),
			state(ProgressListener.class, new StandardConsoleProgressListener()),
			state(Name.class, Name.of("noop")),
			state(de.flapdoodle.embed.process.io.directories.TempDir.class,
				de.flapdoodle.embed.process.io.directories.TempDir.of(tempDir))
		);

		assertThatThrownBy(() -> testee.result(statelookup))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("could not download")
			.hasMessageContaining("ensure that no firewall or vpn is blocking this connection")
			.hasMessageContaining("if this issue persist, you can download it manually to "+cachedArtifactPath)
			.hasCauseInstanceOf(SocketTimeoutException.class)
			.cause()
			.hasMessageContaining("connect timed out");
	}
	private static Pair<StateID<Package>, State<Package>> packageOf(String url) {
		return state(Package.class, Package.of(ArchiveType.ZIP, FileSet.builder()
			.addEntry(FileType.Executable, "noop")
			.build(), url));
	}
	private static Pair<StateID<Distribution>, State<Distribution>> distribution() {
		return state(Distribution.class, Distribution.detectFor(CommonOS.list(), Version.of("noop")));
	}

	static <T> Pair<StateID<T>, State<T>> state(StateID<T> id, State<T> state) {
		return Pair.of(id, state);
	}

	static <T> Pair<StateID<T>, State<T>> state(Class<T> type, T state) {
		return Pair.of(StateID.of(type), State.of(state));
	}

	static StateLookup stateLookupOf(Pair<? extends StateID<?>, ? extends State<?>>... mappings) {
		return new MapBasedStateLookup(Stream.of(mappings)
			.collect(Collectors.toMap(Pair::first, Pair::second)));
	}

	static class StoreArchiveInPath implements DownloadCache, DownloadCacheGuessStorePath {

		private final Path destination;

		public StoreArchiveInPath(Path destination) {
			Preconditions.checkArgument(!Files.exists(destination),"%s already exists", destination);
			this.destination = destination;
		}

		@Override
		public Optional<Path> archiveFor(URL url, ArchiveType archiveType) {
			return Optional.empty();
		}

		@Override
		public Path archivePath(URL url, ArchiveType archiveType) {
			return destination;
		}
		
		@Override
		public Path store(URL url, ArchiveType archiveType, Path archive) throws IOException {
			Files.copy(archive, destination);
			return destination;
		}
	}

	static class MapBasedStateLookup implements StateLookup {

		private final Map<StateID<?>, State<?>> stateMap;

		public MapBasedStateLookup(Map<StateID<?>, State<?>> stateMap) {
			this.stateMap = new LinkedHashMap<>(stateMap);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <D> D of(StateID<D> type) {
			return ((State<D>) Preconditions.checkNotNull(stateMap.get(type), "could find state for %s", type)).value();
		}
	}
}