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
package de.flapdoodle.embed.processg.store;

import de.flapdoodle.embed.process.archives.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableFileSet;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHashExtractedFileSetStoreTest {

	@Test
	public void cacheFileSet(@TempDir Path temp) throws IOException {
		Path store = temp.resolve("store");
		ContentHashExtractedFileSetStore testee = new ContentHashExtractedFileSetStore(store);

		Path archive = temp.resolve("archive");
		write(archive, "ARCHIVE");

		Path srcBase = temp.resolve("src");
		createDir(srcBase);
		createDir(srcBase.resolve("bin"));
		Path executable = srcBase.resolve("bin").resolve("executable");
		write(executable, "EXE");

		Path libA = srcBase.resolve("libA");
		write(libA, "LIB-A");
		createDir(srcBase.resolve("libs"));
		Path libB = srcBase.resolve("libs").resolve("libB");
		write(libB, "LIB-B");

		ImmutableExtractedFileSet src = ExtractedFileSet.builder(srcBase)
			.executable(executable)
			.addLibraryFiles(libA)
			.addLibraryFiles(libB)
			.build();

		ImmutableFileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "bin/executable")
			.addEntry(FileType.Library, "libA")
			.addEntry(FileType.Library, "libs/libB")
			.build();

		ExtractedFileSet stored = testee.store(archive, fileSet, src);

		assertThat(stored.baseDir().relativize(stored.executable()))
			.extracting(Path::toString)
			.isEqualTo("bin/executable");

		List<String> libraryFilePaths = stored.libraryFiles()
			.stream()
			.map(it -> stored.baseDir().relativize(it))
			.map(Path::toString)
			.collect(Collectors.toList());

		assertThat(libraryFilePaths)
			.containsExactly("libA","libs/libB");

		Optional<ExtractedFileSet> readAgain = testee.extractedFileSet(archive, fileSet);

		assertThat(readAgain)
			.contains(stored);
	}

	private static void createDir(Path path) throws IOException {
		Files.createDirectory(path);
	}

	private static void write(Path path, String content) throws IOException {
		Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
	}
}