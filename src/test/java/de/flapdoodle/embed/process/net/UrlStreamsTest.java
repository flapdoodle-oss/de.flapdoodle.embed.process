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
package de.flapdoodle.embed.process.net;

import de.flapdoodle.embed.process.HttpServers;
import de.flapdoodle.embed.process.config.TimeoutConfig;
import de.flapdoodle.net.Net;
import de.flapdoodle.types.Try;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlStreamsTest {

	@Test
	void connectTimeoutOnGetInputStream() throws IOException {
		URL url = new URL("http://example.com:81/");
		URLConnection connection = UrlStreams.urlConnectionOf(url, "test", TimeoutConfig.defaults()
				.withConnectionTimeout(100)
			, Optional.empty());
		assertThatThrownBy(() -> connection.getInputStream())
			.isInstanceOf(SocketTimeoutException.class)
			.hasMessageContaining("onnect timed out"); // can be 'Connect' or 'connect'
	}

	@Test
	void authTest(@TempDir Path tempDir) throws IOException {
		int serverPort = Try.get(Net::freeServerPort);
		HttpServers.Server server = HttpServers.httpServer(serverPort, session -> {
			String auth = session.getHeaders().get("authorization");
			if (auth.equals("Basic c29tZVVzZXI6aGlzUGFzc3dvcmQ=")) {
				return Optional.of(HttpServers.response(200,"text/text","ok".getBytes(StandardCharsets.UTF_8)));
			}
			return Optional.of(HttpServers.response(200,"text/text",("fail: "+auth).getBytes(StandardCharsets.UTF_8)));
		});


		URL url = new URL(server.serverUrl()
			.replace("http://","http://someUser:hisPassword@"));
		URLConnection connection = UrlStreams.urlConnectionOf(url, "test", TimeoutConfig.defaults()
				.withConnectionTimeout(100)
			, Optional.empty());
		UrlStreams.downloadTo(connection, tempDir.resolve("download"), (u, bytesCopied, contentLength) -> {
			
		});
		assertThat(tempDir.resolve("download")).content(StandardCharsets.UTF_8).isEqualTo("ok");
	}
}