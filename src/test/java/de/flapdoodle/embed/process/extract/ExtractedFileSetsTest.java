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
package de.flapdoodle.embed.process.extract;

import de.flapdoodle.embed.process.io.directories.Directory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractedFileSetsTest {

	@Test
	public void shouldNotCopyIfFileAlreadyExists(@TempDir Path tempFolder) throws IOException {
		// given
		// Make sure we already have a file at the target destination
		File platformTempDir = tempFolder.resolve("target").toFile();
		Files.createDirectory(platformTempDir.toPath());
		File executableInPlatformTempDir = new File(platformTempDir, "mongod");
		Files.write(executableInPlatformTempDir.toPath(), "old".getBytes(StandardCharsets.UTF_8));

		FileTime lastModified = Files.getLastModifiedTime(executableInPlatformTempDir.toPath());

		// when
		File baseDir = tempFolder.resolve("source").toFile();
		Files.createDirectory(baseDir.toPath());
		File executable = new File(baseDir, "mongod");
		Files.write(executable.toPath(), "old".getBytes(StandardCharsets.UTF_8));
		ExtractedFileSet src = ExtractedFileSet.builder(baseDir).executable(executable).baseDirIsGenerated(false).build();

		ExtractedFileSets.copy(src, directory(platformTempDir), (prefix, postfix) -> "mongod");

		// then
		byte[] allBytes = Files.readAllBytes(executableInPlatformTempDir.toPath());
		assertThat(new String(allBytes, StandardCharsets.UTF_8)).isEqualTo("old");

		FileTime lastModifiedAfterCopy = Files.getLastModifiedTime(executableInPlatformTempDir.toPath());
		assertThat(lastModifiedAfterCopy).isEqualTo(lastModified);
	}

	private static Directory directory(File directory) {
		return new Directory() {
			@Override public File asFile() {
				return directory;
			}
			@Override public boolean isGenerated() {
				return false;
			}
		};
	}

	@Test
	public void shouldCopyIfContentIsDifferent(@TempDir Path tempFolder) throws IOException {
		// given
		// Make sure we already have a file at the target destination
		Path target = tempFolder.resolve("target");
		Files.createDirectory(target);

		File executableToReplace = target.resolve("mongod").toFile();
		Files.write(executableToReplace.toPath(), "replace".getBytes(StandardCharsets.UTF_8));
		FileTime lastModified = Files.getLastModifiedTime(executableToReplace.toPath());

		// when
		Path source = tempFolder.resolve("source");
		Files.createDirectory(source);

		File executable = source.resolve("mongod").toFile();
		Files.write(executable.toPath(), "replaced".getBytes(StandardCharsets.UTF_8));
		ExtractedFileSet src = ExtractedFileSet.builder(source.toFile())
			.executable(executable)
			.baseDirIsGenerated(false)
			.build();

		ExtractedFileSets.copy(src, directory(target.toFile()), (prefix, postfix) -> "mongod");

		// then
		byte[] allBytes = Files.readAllBytes(executableToReplace.toPath());
		assertThat(new String(allBytes, StandardCharsets.UTF_8)).isEqualTo("replaced");

		FileTime lastModifiedAfterCopy = Files.getLastModifiedTime(executableToReplace.toPath());
		assertThat(lastModifiedAfterCopy).isGreaterThanOrEqualTo(lastModified);
	}
}
