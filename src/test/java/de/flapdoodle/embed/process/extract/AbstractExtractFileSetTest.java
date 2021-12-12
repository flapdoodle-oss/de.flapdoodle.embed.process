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

import de.flapdoodle.embed.process.archives.AbstractExtractFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.Archive;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

class AbstractExtractFileSetTest {

	@TempDir
	Path tempDir;

	@Test
	public void testForExceptionHint() {
		FileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo-bar.exe", Pattern.compile("."))
			.build();

		AbstractExtractFileSet testee = new AbstractExtractFileSet() {

			@Override
			protected Archive.Wrapper archiveStream(Path source) throws IOException {
				throw new IOException("foo");
			}
		};

		Assertions.assertThatThrownBy(() -> testee.extract(tempDir, Paths.get("bar"), fileSet))
			.isInstanceOf(IOException.class);
	}

}