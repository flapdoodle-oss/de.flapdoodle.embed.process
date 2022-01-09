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
package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.archives.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDownloadCacheTest {

	@Test
	public void storeAndReadBack(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalDownloadCache testee = new LocalDownloadCache(baseDir);

		URL url=new URL("http://foo/downloads/archive?latest=true");

		ArchiveType zip = ArchiveType.ZIP;

		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path storedArchive = testee.store(url, zip, archive);

		System.out.println("--> "+storedArchive);

		Optional<Path> readBack = testee.archiveFor(url, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}

	@Test
	public void hostHashOfUrl() throws MalformedURLException {
		URL url=new URL("https://foo:1234/some/path?query=123");
		Path result = LocalDownloadCache.resolve(Paths.get("base"), url, ArchiveType.TGZ);

		assertThat(result.toString())
			.isEqualTo("base"
				+ "/https-foo-1234"
				+ "/4d108babda751006adf21d0969e2cdea"
				+ "/somepath-query-123"
				+ "/9d68130d57496404b727f602c611e7ab09c68be55975cca9be0e2c03b5ebb38c"
				+ "/archive.tgz");
	}

	@Test
	public void sanitizeMustFilterUnwantedChars() {
		String result = LocalDownloadCache.sanitize("ABC?/\\+-");

		assertThat(result).isEqualTo("ABC-");
	}
}