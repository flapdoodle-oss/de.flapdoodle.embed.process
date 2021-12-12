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
package de.flapdoodle.embed.process.extract;

import de.flapdoodle.embed.process.archives.*;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtractFileSetTest {
	private FileSet fileSet;
	private Path fileInArchive;

	@BeforeEach
	public void setUp() throws IOException {
		fileInArchive = Paths.get(this.getClass().getResource("/archives/readme.txt").getPath());
		fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "readme.txt")
			.build();
	}

	@Test
	public void testZipFormat(@TempDir Path destination) throws IOException, URISyntaxException {
		Path source = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());
		ZipAdapter extractor = new ZipAdapter();
		de.flapdoodle.embed.process.archives.ExtractedFileSet extracted = extractor.extract(destination, source, fileSet);

		assertTrue("extracted file exists", extracted.executable().toFile().exists());
		assertEquals(new String(Files.readAllBytes(fileInArchive)), new String(Files.readAllBytes(extracted.executable())));
	}

	@Test
	public void testTgzFormat(@TempDir Path destination) throws IOException, URISyntaxException {
		Path source = Paths.get(this.getClass().getResource("/archives/sample.tgz").toURI());
		TgzAdapter extractor = new TgzAdapter();

		de.flapdoodle.embed.process.archives.ExtractedFileSet extracted = extractor.extract(destination, source, fileSet);

		assertTrue("extracted file exists", extracted.executable().toFile().exists());
		assertEquals(new String(Files.readAllBytes(fileInArchive)), new String(Files.readAllBytes(extracted.executable())));
	}

	@Test
	public void testTbz2Format(@TempDir Path destination) throws IOException, URISyntaxException {
		Path source = Paths.get(this.getClass().getResource("/archives/sample.tbz2").toURI());
		Tbz2Adapter extractor = new Tbz2Adapter();

		de.flapdoodle.embed.process.archives.ExtractedFileSet extracted = extractor.extract(destination, source, fileSet);

		assertTrue("extracted file exists", extracted.executable().toFile().exists());
		assertEquals(new String(Files.readAllBytes(fileInArchive)), new String(Files.readAllBytes(extracted.executable())));
	}

	@Test
	public void testFileIsArchive(@TempDir Path destination) throws IOException, URISyntaxException {
		Path source = Paths.get(this.getClass().getResource("/archives/sample.txt").toURI());
		SingleFileAdapter extractor = new SingleFileAdapter();
		ExtractedFileSet extracted = extractor.extract(destination, source, FileSet.builder()
			.addEntry(FileType.Executable, "sample.txt")
			.build());

		assertTrue("extracted file exists", extracted.executable().toFile().exists());
		assertEquals(new String(Files.readAllBytes(fileInArchive)), new String(Files.readAllBytes(extracted.executable())));
	}
}
