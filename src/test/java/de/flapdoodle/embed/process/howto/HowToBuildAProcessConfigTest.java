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
package de.flapdoodle.embed.process.howto;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.net.UrlStreams;
import de.flapdoodle.embed.process.io.net.UrlStreams.DownloadCopyListener;
import de.flapdoodle.embed.process.parts.ArtifactPath;
import de.flapdoodle.embed.process.parts.ArtifactUrl;
import de.flapdoodle.embed.process.parts.CachingArtifactDownloader;
import de.flapdoodle.embed.process.parts.LocalArtifactPath;
import de.flapdoodle.embed.process.parts.ProcessFactory;
import de.flapdoodle.embed.process.types.DownloadPath;
import de.flapdoodle.transition.StateID;
import de.flapdoodle.transition.initlike.InitLike;
import de.flapdoodle.transition.initlike.InitLike.Init;
import de.flapdoodle.transition.initlike.InitRoutes;
import de.flapdoodle.transition.initlike.State;
import de.flapdoodle.transition.routes.SingleDestination;
import de.flapdoodle.types.Try;

public class HowToBuildAProcessConfigTest {

	@Test
	public void readableSample() {

		try (Init<Path> tempArtifactStorePath = InitLike.with(InitRoutes.builder()
				.state(Path.class).isReachedBy(() -> artifactStore())
				.build()).init(StateID.of(Path.class))) {

			ProcessFactory processFactory = ProcessFactory.builder()
					.version(Version.of("2.1.1"))
					.baseDownloadUrl("https://bitbucket.org/ariya/phantomjs/downloads/")
					.artifactsBasePath(tempArtifactStorePath.current())
					.archiveTypeForDistribution(HowToBuildAProcessConfigTest::getArchiveType)
					.fileSetOfDistribution(HowToBuildAProcessConfigTest::fileSetFor)
					.urlOfDistributionAndArchiveType(
							(baseUrl, dist, archiveType) -> ArtifactUrl.of(baseUrl.value() + getPath(dist, archiveType)))
					.localArtifactPathOfDistributionAndArchiveType(
							(dist, archiveType) -> LocalArtifactPath.of(getPath(dist, archiveType)))
					.artifactPathForUrl(new CachingArtifactDownloader(listener()))
					.build();

			if (true) {
				String dotFile = processFactory.setupAsDot("processBuild_sample");
				System.out.println("---------------------------");
				System.out.println(dotFile);
				System.out.println("---------------------------");
			}

			InitLike initLike = processFactory.initLike();

			try (Init<ArtifactPath> init = initLike.init(StateID.of(ArtifactPath.class))) {
				System.out.println("downloaded: " + init.current());

			}
		}
	}

	@Test
	@Ignore("it is an integration test")
	public void simpleSample() {
		StateID<Path> artifactStore = StateID.of("artifactStore", Path.class);
		StateID<Path> artifactPath = StateID.of("artifactPath", Path.class);
		StateID<Path> downloadedArtifactPath = StateID.of("downloadedArtifactPath", Path.class);
		StateID<URL> url = StateID.of(URL.class);
		InitRoutes<SingleDestination<?>> routes = InitRoutes.builder()
				.state(Version.class).isInitializedWith(Version.of("2.1.1"))
				.state(artifactStore).isReachedBy(() -> artifactStore())
				.state(DownloadPath.class)
				.isInitializedWith(DownloadPath.of("https://bitbucket.org/ariya/phantomjs/downloads/"))
				.given(Version.class).state(Distribution.class).isDerivedBy(Distribution::detectFor)
				.given(Distribution.class).state(DistributionPackage.class).isReachedBy(HowToBuildAProcessConfigTest::packageOf)
				.given(DownloadPath.class, DistributionPackage.class).state(url)
				.isReachedBy(HowToBuildAProcessConfigTest::downloadUrl)
				.given(artifactStore, StateID.of(DistributionPackage.class)).state(artifactPath)
				.isReachedBy(HowToBuildAProcessConfigTest::artifactPath)
				.given(artifactPath, url).state(downloadedArtifactPath)
				.isReachedBy(HowToBuildAProcessConfigTest::useOrDownloadArtifact)
				.build();

		try (Init<Path> initArtifactStore = InitLike.with(routes).init(artifactStore)) {

			try (Init<Path> init = initArtifactStore.init(downloadedArtifactPath)) {
				System.out.println("current: " + init.current());

			}

			try (Init<Path> init = initArtifactStore.init(downloadedArtifactPath)) {
				System.out.println("current: " + init.current());

			}
		}
	}

	private static State<Path> useOrDownloadArtifact(Path artifactPath, URL downloadUrl) {
		if (!artifactPath.toFile().exists()) {
			Try.supplier(() -> {
				TimeoutConfig timeoutConfig = TimeoutConfig.defaults();
				URLConnection connection = UrlStreams.urlConnectionOf(downloadUrl, "flapdoodle-user-agent", timeoutConfig,
						Optional.empty());
				UrlStreams.downloadTo(connection, artifactPath, listener());
				return State.of(artifactPath);
			})
					.mapCheckedException(RuntimeException::new)
					.get();
		}
		return State.of(artifactPath);
	}

	private static UrlStreams.DownloadCopyListener listener() {
		return new DownloadCopyListener() {
			long lastMessage = System.currentTimeMillis();

			@Override
			public void downloaded(long bytesCopied, long contentLength) {
				long current = System.currentTimeMillis();
				if (current - lastMessage > 1000 || bytesCopied == contentLength) {
					lastMessage = current;
					if (contentLength > 0) {
						System.out.print(bytesCopied * 100 / contentLength + "% ");
					}
					if (contentLength == bytesCopied) {
						System.out.println();
					}
				}
			}
		};
	}

	private static State<Path> artifactPath(Path storePath, DistributionPackage dist) {
		return State.of(storePath.resolve(dist.archivePath()));
	}

	private static State<Path> artifactStore() {
		return Try.supplier(() -> {
			Path artifactStore = Files.createTempDirectory("artifactStore-");
			return State.of(artifactStore, (path) -> {
				deleteDirectoryAndContent(path);
			});
		})
				.mapCheckedException(RuntimeException::new)
				.get();
	}

	private static void deleteDirectoryAndContent(Path dir) {
		try {
			Files.walkFileTree(dir, new FileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (attrs.isSymbolicLink()) {
						return FileVisitResult.SKIP_SIBLINGS;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.TERMINATE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static State<URL> downloadUrl(DownloadPath path, DistributionPackage dist) {
		try {
			return State.of(new URL(path.value() + dist.archivePath()));
		} catch (MalformedURLException e) {
			throw new RuntimeException("could not create downloadUrl for " + path + " and " + dist);
		}
	}

	private static State<DistributionPackage> packageOf(Distribution distribution) {
		ArchiveType archiveType = getArchiveType(distribution);
		return State.of(DistributionPackage.of(archiveType, fileSetFor(distribution), getPath(distribution, archiveType)));
	}

	private static FileSet fileSetFor(Distribution distribution) {
		String execName;
		switch (distribution.platform().operatingSystem()) {
			case Windows:
				execName = "phantomjs.exe";
				break;
			default:
				execName = "phantomjs";
		}

		return FileSet.builder()
				.addEntry(FileType.Executable, execName)
				.build();
	}

	private static ArchiveType getArchiveType(Distribution distribution) {
		switch (distribution.platform().operatingSystem()) {
			case OS_X:
			case Windows:
				return ArchiveType.ZIP;
		}
		return ArchiveType.TBZ2;
	}

	private static String getPath(Distribution distribution, ArchiveType archiveType) {
		final String packagePrefix;
		String bitVersion = "";
		switch (distribution.platform().operatingSystem()) {
			case OS_X:
				packagePrefix = "macosx";
				break;
			case Windows:
				packagePrefix = "windows";
				break;
			default:
				packagePrefix = "linux";
				switch (distribution.platform().architecture().bitSize()) {
					case B64:
						bitVersion = "-x86_64";
						break;
					default:
						bitVersion = "-i686";
				}
		}

		String packageExtension = ".zip";
		if (archiveType == ArchiveType.TBZ2) {
			packageExtension = ".tar.bz2";
		}
		return "phantomjs-" + distribution.version().asInDownloadPath() + "-" + packagePrefix + bitVersion
				+ packageExtension;
	}

}