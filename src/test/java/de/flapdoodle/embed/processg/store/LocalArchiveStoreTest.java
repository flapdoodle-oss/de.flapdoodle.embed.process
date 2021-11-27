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

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.processg.extract.ArchiveType;
import de.flapdoodle.os.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LocalArchiveStoreTest {

	@Test
	public void storeAndReadBack(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalArchiveStore testee = new LocalArchiveStore(baseDir);

		Distribution distribution = Distribution.of(Version.of("foo"), Platform.detect());
		ArchiveType zip = ArchiveType.ZIP;
		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path storedArchive = testee.store("test", distribution, zip, archive);

		System.out.println("--> "+storedArchive);

		Optional<Path> readBack = testee.archiveFor("test", distribution, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}
}