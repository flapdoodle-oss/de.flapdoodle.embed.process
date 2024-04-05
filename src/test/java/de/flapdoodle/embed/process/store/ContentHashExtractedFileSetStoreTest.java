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

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.archives.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableFileSet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHashExtractedFileSetStoreTest {

	@BeforeAll
	static void before() {
		Assertions.setMaxStackTraceElementsDisplayed(40);
	}

	@Test
	public void cacheFileSet(@TempDir Path tempDir) throws IOException {
		Path store = tempDir.resolve("store");
		ContentHashExtractedFileSetStore testee = new ContentHashExtractedFileSetStore(store);

		Path archive = tempDir.resolve("archive");
		write(archive, "ARCHIVE");

		Path srcBase = tempDir.resolve("src");
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

		assertThat(stored.executable())
			.isReadable()
			.isExecutable();

		List<String> libraryFilePaths = stored.libraryFiles()
			.stream()
			.map(it -> stored.baseDir().relativize(it))
			.map(Path::toString)
			.collect(Collectors.toList());

		assertThat(libraryFilePaths)
			.containsExactly("libA", "libs/libB");

		Optional<ExtractedFileSet> readAgain = testee.extractedFileSet(archive, fileSet);

		assertThat(readAgain)
			.contains(stored);
	}

	@Test
	public void hashShouldBeCached(@TempDir Path tempDir) throws IOException {
		byte[] content = new byte[1024 * 256 + 123];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) i;
		}
		;

		Path archive = tempDir.resolve("archive");
		Files.write(archive, content, StandardOpenOption.CREATE_NEW);

		Path cacheDir = tempDir.resolve("cache");
		Files.createDirectory(cacheDir);

		ImmutableFileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo")
			.build();

		String hash = ContentHashExtractedFileSetStore.archiveContentAndFileSetDescriptionHash(cacheDir, archive, fileSet);
		assertThat(hash).isEqualTo("715ade7b9214161b8ca25d03b3c0a98fb7f9b891b969f828d50b3e1b4cf28fad");

		String cacheHash = ExtractedFileSets.archiveAndFileSetDescriptionHash(archive, fileSet);
		assertThat(cacheDir.resolve(cacheHash))
			.exists()
			.content().isEqualTo(hash);

		String secondHash = ContentHashExtractedFileSetStore.archiveContentAndFileSetDescriptionHash(cacheDir, archive, fileSet);
		assertThat(secondHash).isEqualTo(hash);
	}

	@Test
	public void writeArchiveTwiceShouldOnlyFailIfHashIsDifferent(@TempDir Path tempDir) throws IOException {
		Path store = tempDir.resolve("store");
		ContentHashExtractedFileSetStore testee = new ContentHashExtractedFileSetStore(store);

		Path archive = tempDir.resolve("archive");
		write(archive, "ARCHIVE");

		Path srcBase = tempDir.resolve("src");
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
		ExtractedFileSet storedAgain = testee.store(archive, fileSet, src);

		assertThat(stored).isEqualTo(storedAgain);
	}

	@Test
	public void writeFileSetTwiceShouldOnlyFailIfHashIsDifferent(@TempDir Path tempDir) throws IOException, InterruptedException {
		Path store = tempDir.resolve("store");
		ContentHashExtractedFileSetStore testee = new ContentHashExtractedFileSetStore(store);

		Path archive = tempDir.resolve("archive");
		write(archive, "ARCHIVE");

		Path srcBase = tempDir.resolve("src");
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

		AtomicReference<ExtractedFileSet> stored = new AtomicReference<>();// testee.store(archive, fileSet, src);
		AtomicReference<ExtractedFileSet> storedAgain = new AtomicReference<>(); //testee.store(archive, fileSet, src);
		AtomicReference<IOException> exception = new AtomicReference<>();

		Thread t1 = new Thread(() -> {
			try {
				stored.set(testee.store(archive, fileSet, src));
			}
			catch (IOException e) {
				exception.set(e);
			}
		});
		Thread t2 = new Thread(() -> {
			try {
				storedAgain.set(testee.store(archive, fileSet, src));
			}
			catch (IOException e) {
				exception.set(e);
			}
		});

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertThat(exception.get()).isNull();
		assertThat(stored.get()).isEqualTo(storedAgain.get());
	}

	private static void createDir(Path path) throws IOException {
		Files.createDirectory(path);
	}

	private static void write(Path path, String content) throws IOException {
		Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
	}
}