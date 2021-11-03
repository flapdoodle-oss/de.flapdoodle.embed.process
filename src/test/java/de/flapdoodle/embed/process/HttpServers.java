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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class HttpServers {

	public static Server httpServer(int port, Listener listener) throws IOException {
		return new Server(port, listener);
	}
	
	public static class Server extends NanoHTTPD implements AutoCloseable {

		private final Listener listener;

		public Server(int port, Listener listener) throws IOException {
			super(port);
			this.listener = listener;
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		}

		@Override
		public void close() {
			this.stop();
		}
		
		@Override
		public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
			Optional<Response> response = listener.serve(uri, method, headers, parms, files);
			return response
					.orElseGet(() -> super.serve(uri, method, headers, parms, files));
		}
	}
	
	@FunctionalInterface
	public static interface Listener {
		Optional<Response> serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files);
	}

	public static Response response(int status, String mimeType, byte[] data) {
		return response(status, mimeType, data, data.length);
	}

	public static Response response(int status, String mimeType, byte[] data, int contentLength) {
		Response ret = NanoHTTPD.newFixedLengthResponse(Status.lookup(status), mimeType, new ByteArrayInputStream(data), data.length);
		ret.addHeader("Content-Length", ""+contentLength);
		return ret;
				
	}
	
	public static Response chunkedResponse(int status, String mimeType, byte[] data) {
		return NanoHTTPD.newChunkedResponse(Status.lookup(status), mimeType, new ByteArrayInputStream(data));
	}

}
