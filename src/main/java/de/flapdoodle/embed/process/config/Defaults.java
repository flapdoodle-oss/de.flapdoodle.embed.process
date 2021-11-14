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
package de.flapdoodle.embed.process.config;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.ArtifactStore;
import de.flapdoodle.embed.process.store.Downloader;

public abstract class Defaults {
	
	public static Directory tempDirFactory() {
		return PropertyOrPlatformTempDir.defaultInstance();
	}
	
	public static UUIDTempNaming executableNaming() {
		return new UUIDTempNaming();
	}
	
	public static ArtifactStore artifactStore(DownloadConfig downloadConfig) {
		return ArtifactStore.builder()
				.downloadConfig(downloadConfig)
				.downloader(Downloader.platformDefault())
				.tempDirFactory(tempDirFactory())
				.executableNaming(executableNaming())
				.build();
	}

	public static DownloadConfig genericDownloadConfig(String name, String downloadPath, PackageResolver packageResolver) {
		String prefix = "."+name;
		return DownloadConfig.builder()
				.downloadPath((__) -> downloadPath)
				.packageResolver(packageResolver)
				.artifactStorePath(new UserHome(prefix))
				.fileNaming(executableNaming())
				.progressListener(progressListener())
				.userAgent("Mozilla/5.0 (compatible; embedded "+name+"; +https://github.com/flapdoodle-oss/de.flapdoodle.embed.process)")
				.build();
	}

	public static StandardConsoleProgressListener progressListener() {
		return new StandardConsoleProgressListener();
	}
}
