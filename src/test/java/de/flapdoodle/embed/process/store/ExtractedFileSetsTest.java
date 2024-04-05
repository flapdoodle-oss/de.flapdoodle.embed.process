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
package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableFileSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractedFileSetsTest {

	@Test
	public void hashShouldNotChangeIfArchiveIsNotReadInOneGo(@TempDir Path tempDir) throws IOException {
		byte[] content = new byte[1024 * 256 + 123];
		ThreadLocalRandom.current().nextBytes(content);

		Path archive = tempDir.resolve("archive");

		Files.write(archive, content, StandardOpenOption.CREATE_NEW);

		ImmutableFileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo")
			.build();

		String newHash1024 = ExtractedFileSets.archiveContentAndFileSetDescriptionHash(archive, fileSet, 1024);
		String newHash123 = ExtractedFileSets.archiveContentAndFileSetDescriptionHash(archive, fileSet, 123);

		assertThat(newHash1024).isEqualTo(newHash123);
	}
}