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

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.PlatformTempDir;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class AbstractExtractorTest {

	@Test(expected=IOException.class)
	public void testForExceptionHint() throws IOException {

		Directory factory=new PlatformTempDir();
		TempNaming exeutableNaming=new UUIDTempNaming();
		
		FileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo-bar.exe", Pattern.compile("."))
			.build();
		
		FilesToExtract filesToExtract=new FilesToExtract(factory, exeutableNaming, fileSet);

		new AbstractExtractor() {
			
			@Override
			protected Archive.Wrapper archiveStream(File source) throws IOException {
				throw new IOException("foo");
			}
		}.extract(new File("bar"), filesToExtract);
	}

}
