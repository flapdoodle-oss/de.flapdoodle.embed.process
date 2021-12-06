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
package de.flapdoodle.embed.processg.io.directories;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class TempDirTest {

	@Test
	public void tempDirectoryIsPlatformTemp() {
		TempDir result = TempDir.platformTempDir().get();

		assertThat(result)
			.extracting(TempDir::value)
			.isEqualTo(Paths.get(System.getProperty("java.io.tmpdir")));
	}

	@Test
	public void parentOfSubDirIsPlatformTemp() {
		TempDir result = TempDir.platformTempSubDir(new UUIDNaming()).get();

		assertThat(result)
			.extracting(TempDir::value)
			.extracting(Path::getParent)
			.isEqualTo(Paths.get(System.getProperty("java.io.tmpdir")));
	}
}