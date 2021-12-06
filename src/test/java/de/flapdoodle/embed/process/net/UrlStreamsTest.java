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
package de.flapdoodle.embed.process.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import de.flapdoodle.embed.process.net.UrlStreams;
import org.junit.Test;

import de.flapdoodle.embed.process.HttpServers;
import de.flapdoodle.embed.process.net.UrlStreams.DownloadCopyListener;
import de.flapdoodle.embed.process.runtime.Network;

public class UrlStreamsTest {

	@Test
	public void downloadShouldBeMovedToDestinationOnSuccess() throws MalformedURLException, IOException {
		int httpPort = Network.getFreeServerPort();
		long contentLengt = 2*1024*1024;
		byte[] content = randomFilledByteArray((int) contentLengt);
		
		Path destination = Files.createTempFile("moveToThisFile", "");
		Files.delete(destination);
		
		try (HttpServers.Server server = HttpServers.httpServer(httpPort, (session) -> Optional.empty())) {
			URLConnection connection = new URL("http://localhost:123/toLong?foo=bar").openConnection();
			UrlStreams.downloadTo(connection, destination, url -> {
				Path downloadMock = Files.createTempFile("moveThis", "");
				Files.write(downloadMock, content, StandardOpenOption.TRUNCATE_EXISTING);
				return downloadMock;
			});
		}
		
		File asFile = destination.toFile();
		assertTrue(asFile.exists());
		assertTrue(asFile.isFile());
		
		byte[] transferedBytes = Files.readAllBytes(destination);
		assertSameContent(content, transferedBytes);
		
		Files.delete(destination);
	}
	
	@Test
	public void downloadShouldBeCompleteAndMatchContent() throws IOException {
		int httpPort = Network.freeServerPort(Network.getLocalHost());
		long contentLengt = 2*1024*1024;
		byte[] content = randomFilledByteArray((int) contentLengt);
		
		HttpServers.Listener listener=(session) -> {
			if (session.getUri().equals("/download")) {
				return Optional.of(HttpServers.response(200, "text/text", content));
			}
			return Optional.empty();
		};
		
		List<Long> downloadSizes = new ArrayList<>();
		
		try (HttpServers.Server server = HttpServers.httpServer(httpPort, listener)) {
			URLConnection connection = new URL("http://localhost:"+httpPort+"/download?foo=bar").openConnection();
			
			DownloadCopyListener copyListener=(bytesCopied, downloadContentLength) -> {
				downloadSizes.add(bytesCopied);
				assertEquals("contentLengt", contentLengt, downloadContentLength);
			};
			Path destination = UrlStreams.downloadIntoTempFile(connection, copyListener);
			assertNotNull(destination);
			
			File asFile = destination.toFile();
			assertTrue(asFile.exists());
			assertTrue(asFile.isFile());
			byte[] transferedBytes = Files.readAllBytes(destination);
			assertSameContent(content, transferedBytes);
			
			Files.delete(destination);
		}
		
		List<Long> downloadSizesMatchingFullDownload = downloadSizes.stream()
		 	.filter(l -> l == contentLengt)
		 	.collect(Collectors.toList());
		
		assertEquals(1,downloadSizesMatchingFullDownload.size());
		
		List<Long> downloadSizesBiggerThanContentLength = downloadSizes.stream()
			.filter(l -> l > contentLengt)
			.collect(Collectors.toList());
		
		assertTrue(downloadSizesBiggerThanContentLength.isEmpty());
	}

	@Test
	public void downloadWithoutContentLengthShouldWorkToo() throws IOException {
		int httpPort = Network.getFreeServerPort();
		long contentLengt = 2*1024*1024;
		byte[] content = randomFilledByteArray((int) contentLengt);
		
		HttpServers.Listener listener=(session) -> {
			if (session.getUri().equals("/download")) {
				return Optional.of(HttpServers.chunkedResponse(200, "text/text", content));
			}
			return Optional.empty();
		};
		
		List<Long> downloadSizes = new ArrayList<>();
		
		try (HttpServers.Server server = HttpServers.httpServer(httpPort, listener)) {
			URLConnection connection = new URL("http://localhost:"+httpPort+"/download?foo=bar").openConnection();
			
			DownloadCopyListener copyListener=(bytesCopied, downloadContentLength) -> {
				downloadSizes.add(bytesCopied);
				assertEquals("contentLengt", -1, downloadContentLength);
			};
			Path destination = UrlStreams.downloadIntoTempFile(connection, copyListener);
			assertNotNull(destination);
			
			File asFile = destination.toFile();
			assertTrue(asFile.exists());
			assertTrue(asFile.isFile());
			byte[] transferedBytes = Files.readAllBytes(destination);
			assertSameContent(content, transferedBytes);
			
			Files.delete(destination);
		}
		
		List<Long> downloadSizesMatchingFullDownload = downloadSizes.stream()
		 	.filter(l -> l == contentLengt)
		 	.collect(Collectors.toList());
		
		assertEquals(1,downloadSizesMatchingFullDownload.size());
		
		List<Long> downloadSizesBiggerThanContentLength = downloadSizes.stream()
			.filter(l -> l > contentLengt)
			.collect(Collectors.toList());
		
		assertTrue(downloadSizesBiggerThanContentLength.isEmpty());
	}
	
	@Test
	public void downloadShouldFailIfContentLengthDoesNotMatch() throws IOException {
		int httpPort = Network.freeServerPort(Network.getLocalHost());
		long contentLengt = 2*1024*1024;
		byte[] content = randomFilledByteArray((int) contentLengt);
		
		HttpServers.Listener listener=(session) -> {
			if (session.getUri().equals("/toShort")) {
				return Optional.of(HttpServers.response(200, "text/text", content, content.length*2));
			}
			if (session.getUri().equals("/toLong")) {
				return Optional.of(HttpServers.response(200, "text/text", content, content.length/2));
			}
			return Optional.empty();
		};
		
		try (HttpServers.Server server = HttpServers.httpServer(httpPort, listener)) {
			
			try {
				URLConnection connection = new URL("http://localhost:"+httpPort+"/toShort?foo=bar").openConnection();
				UrlStreams.downloadIntoTempFile(connection, (bytesCopied, downloadContentLength) -> {
				});
				fail("should not reach this");
			} catch (IllegalArgumentException iax) {
				assertTrue(iax.getLocalizedMessage().contains("partial"));
			}

			// looks like URLConnection Impl does only read to content-length size if provided
			boolean weCanFakeNanoHttpToSendMoreStuffThanInContentLength=false;
			if (weCanFakeNanoHttpToSendMoreStuffThanInContentLength) {
				try {
					URLConnection connection = new URL("http://localhost:"+httpPort+"/toLong?foo=bar").openConnection();
					UrlStreams.downloadIntoTempFile(connection, (bytesCopied, downloadContentLength) -> {
					});
					fail("should not reach this");
				} catch (IllegalArgumentException iax) {
					assertTrue(iax.getLocalizedMessage().contains("partial"));
				}
			}

		}
	}

	@Test
	public void shouldNotFailIfCalledTwice() throws IOException {
		int httpPort = Network.freeServerPort(Network.getLocalHost());
		long contentLengt = 2*1024*1024;
		byte[] content = randomFilledByteArray((int) contentLengt);

		HttpServers.Listener listener=(session) -> {
			if (session.getUri().equals("/stuff")) {
				return Optional.of(HttpServers.response(200, "text/text", content));
			}
			return Optional.empty();
		};

		try (HttpServers.Server server = HttpServers.httpServer(httpPort, listener)) {
			URLConnection connection = new URL("http://localhost:"+httpPort+"/stuff").openConnection();
			UrlStreams.downloadIntoTempFile(connection, (bytesCopied, downloadContentLength) -> {
				assertEquals("contentLength", content.length, downloadContentLength);
			});

			connection = new URL("http://localhost:"+httpPort+"/stuff").openConnection();
			UrlStreams.downloadIntoTempFile(connection, (bytesCopied, downloadContentLength) -> {
				assertEquals("contentLength", content.length, downloadContentLength);
			});
		}
	}

	private void assertSameContent(byte[] expected, byte[] result) {
		assertEquals("length", expected.length, result.length);
		for (int i=0;i<expected.length;i++) {
			assertEquals("length", expected[i], result[i]);
		}
	}


	private byte[] randomFilledByteArray(int size) {
		byte[] content = new byte[size];
		for (int i=0;i<content.length;i++) {
			content[i]=(byte) ThreadLocalRandom.current().nextInt();
		}
		return content;
	}
}
