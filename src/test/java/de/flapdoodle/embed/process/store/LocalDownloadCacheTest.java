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

import de.flapdoodle.embed.process.distribution.ArchiveType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDownloadCacheTest {

	@Test
	public void storeAndReadBack(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalDownloadCache testee = new LocalDownloadCache(baseDir);

		URL url=new URL("http://foo/downloads/archive?latest=true");

		ArchiveType zip = ArchiveType.ZIP;

		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path storedArchive = testee.store(url, zip, archive);

		Optional<Path> readBack = testee.archiveFor(url, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}

	@Test
	public void storeSameFileMustSucceed(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalDownloadCache testee = new LocalDownloadCache(baseDir);

		URL url=new URL("http://foo/downloads/archive?latest=true");

		ArchiveType zip = ArchiveType.ZIP;

		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path storedArchive = testee.store(url, zip, archive);
		Path storedSecondTime = testee.store(url, zip, archive);

		assertThat(storedArchive)
			.isEqualTo(storedSecondTime);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}

	@Test
	public void storeDifferentFileMustFail(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalDownloadCache testee = new LocalDownloadCache(baseDir);

		URL url=new URL("http://foo/downloads/archive?latest=true");

		ArchiveType zip = ArchiveType.ZIP;

		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());
		Path otherContent = Paths.get(this.getClass().getResource("/archives/sample.tgz").toURI());

		testee.store(url, zip, archive);
		
		assertThatThrownBy(() -> testee.store(url, zip, otherContent))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("already exists with different content");
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
	void fileUrls() throws MalformedURLException {
		assertThat(new URL("file:/c:/some/path").toString())
			.isEqualTo("file:/c:/some/path");
		assertThat(new URL("file:///c:/some/path").toString())
			.isEqualTo("file:/c:/some/path");
		assertThat(new URL("file:///c:/some/path").toString())
			.isEqualTo("file:/c:/some/path");
		assertThat(new URL("file://server/c:/some/path").toString())
			.isEqualTo("file://server/c:/some/path");
	}

	@Test
	void webUrls() throws MalformedURLException {
		URL withServer = new URL("http://server/some/path");
		assertThat(withServer.toString())
			.isEqualTo("http://server/some/path");
		assertThat(withServer.getHost()).isEqualTo("server");

		URL withoutServer = new URL("http:///some/path");
		assertThat(withoutServer.toString())
			.isEqualTo("http:/some/path");
		assertThat(withoutServer.getHost()).isEqualTo("");
	}

	@Test
	void serverPart() throws MalformedURLException {
		assertThat(LocalDownloadCache.serverPart(new URL("http://server/some/path")))
			.isEqualTo("http://server");
		assertThat(LocalDownloadCache.serverPart(new URL("http:///some/path")))
			.isEqualTo("http:");
		assertThat(LocalDownloadCache.serverPart(new URL("file://server/c:/some/path")))
			.isEqualTo("file://server");
		assertThat(LocalDownloadCache.serverPart(new URL("file:///c:/some/path")))
			.isEqualTo("file:");
		assertThat(LocalDownloadCache.serverPart(new URL("file:/c:/some/path")))
			.isEqualTo("file:");
	}

	@Test
	public void hostHashOfLocalFilePath() throws MalformedURLException {
		URL url=new URL("file:///c:/some/path");

		Path result = LocalDownloadCache.resolve(Paths.get("base"), url, ArchiveType.TGZ);

		assertThat(result.toString())
			.isEqualTo("base"
				+ "/file-"
				+ "/1ec3bfecf409569cee8f7787388bd40c"
				+ "/c-somepath"
				+ "/0319b21be38cb136077cc8bf0af6f4807fc71dd203731c3773382309c89c7593"
				+ "/archive.tgz");
	}

	@Test
	public void sanitizeMustFilterUnwantedChars() {
		String result = LocalDownloadCache.sanitize("ABC?/\\+-");

		assertThat(result).isEqualTo("ABC-");
	}
}