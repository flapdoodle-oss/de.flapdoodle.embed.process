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
package de.flapdoodle.embed.process;

import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.types.Try;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HttpServers {
	private static Logger logger= LoggerFactory.getLogger(HttpServers.class);

	public static Server httpServer(Class<?> testClass, Map<String, String> resourcePathMap) throws IOException {
		int serverPort = Try.get(() -> Network.freeServerPort(Network.getLocalHost()));
		Map<String, Supplier<Response>> map = resourcePathMap.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> Try.supplier(() -> {
				Path resourcePath = Resources.resourcePath(testClass, entry.getValue());
				byte[] content = Files.readAllBytes(resourcePath);
				return response(200, "application/octet-stream", content);
			}).mapCheckedException(RuntimeException::new)::get
				//.fallbackTo(ex -> { throw new RuntimeException(ex);})
			));

			return httpServer(serverPort, map);
	}

	public static Server httpServer(int port, Map<String, Supplier<Response>> responseMap) throws IOException {
		return httpServer(port, session -> Optional.ofNullable(responseMap.get(session.getUri()))
			.map(it -> {
				Response response = it.get();
				logger.info(""+session.getUri()+" -> "+response.getStatus()+":"+response.getMimeType()+"(size="+response.getHeader("content-length")+")");
				return response;
			}));
	}

	public static Server httpServer(int port, Listener listener) throws IOException {
		return new Server(port, listener);
	}
	
	public static class Server extends NanoHTTPD implements AutoCloseable {

		private final Listener listener;

		public Server(int port, Listener listener) throws IOException {
			super("localhost", port);
			this.listener = listener;
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		}

		@Override
		public void close() {
			this.stop();
		}

		public String serverUrl() {
			return "http://"+getHostname()+":"+getListeningPort();
		}
//		@Override
//		public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
//			Optional<Response> response = listener.serve(uri, method, headers, parms, files);
//			return response
//					.orElseGet(() -> super.serve(uri, method, headers, parms, files));
//		}

		@Override
		public Response serve(IHTTPSession session) {
				Optional<Response> response = listener.serve(session);
				return response
						.orElseGet(() -> super.serve(session));
		}
	}
	
	@FunctionalInterface
	public interface Listener {
		Optional<Response> serve(NanoHTTPD.IHTTPSession session);
	}

	public static Response response(int status, String mimeType, byte[] data) {
		return response(status, mimeType, data, data.length);
	}

	public static Response response(int status, String mimeType, byte[] data, int contentLength) {
		Response ret = NanoHTTPD.newFixedLengthResponse(Status.lookup(status), mimeType, new ByteArrayInputStream(data), data.length);
		ret.addHeader("content-length", ""+contentLength);
		return ret;
				
	}
	
	public static Response chunkedResponse(int status, String mimeType, byte[] data) {
		return NanoHTTPD.newChunkedResponse(Status.lookup(status), mimeType, new ByteArrayInputStream(data));
	}

}
