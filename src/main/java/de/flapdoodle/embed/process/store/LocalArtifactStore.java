/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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
package de.flapdoodle.embed.process.store;

import java.io.File;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.io.file.Files;

/**
 *
 */
public class LocalArtifactStore {

	public static boolean checkArtifact(IDownloadConfig runtime, Distribution distribution) {
		return getArtifact(runtime, distribution) != null;
	}


	public static boolean store(IDownloadConfig runtime, Distribution distribution, File download) {
		File dir = createOrGetBaseDir(runtime);
		File artifactFile = new File(dir, runtime.getPath(distribution));
		createOrCheckDir(artifactFile.getParentFile());
		if (!Files.moveFile(download, artifactFile))
			throw new IllegalArgumentException("Could not move " + download + " to " + artifactFile);
		File checkFile = new File(dir, runtime.getPath(distribution));
		return checkFile.exists() & checkFile.isFile() & checkFile.canRead();
	}

	private static File createOrGetBaseDir(IDownloadConfig runtime) {
		File dir = new File(runtime.getArtifactStorePathNaming().getPath());
		createOrCheckDir(dir);
		return dir;
	}

	private static void createOrCheckDir(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdir())
				throw new IllegalArgumentException("Could NOT create Directory " + dir);
		}
		if (!dir.isDirectory())
			throw new IllegalArgumentException("" + dir + " is not a Directory");
	}


	public static File getArtifact(IDownloadConfig runtime, Distribution distribution) {
		File dir = createOrGetBaseDir(runtime);
		File artifactFile = new File(dir, runtime.getPath(distribution));
		if ((artifactFile.exists()) && (artifactFile.isFile()))
			return artifactFile;
		return null;
	}
}
