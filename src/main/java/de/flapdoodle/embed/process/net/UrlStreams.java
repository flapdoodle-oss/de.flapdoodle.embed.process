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

import de.flapdoodle.embed.process.config.TimeoutConfig;
import de.flapdoodle.net.URLConnections;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

public abstract class UrlStreams {

	static class Adapter implements DownloadToPath {
		@Override
		public void download(URL downloadUrl, Path destination, Optional<Proxy> proxy, String userAgent, TimeoutConfig timeoutConfig,
			DownloadCopyListener copyListener) throws IOException {
			URLConnection connection = UrlStreams.urlConnectionOf(downloadUrl, userAgent, timeoutConfig,proxy);
			UrlStreams.downloadTo(connection, destination, copyListener);
		}
	}

	public static DownloadToPath asDownloadToPath() {
		return new Adapter();
	}

	public static void downloadTo(URLConnection connection, Path destination, DownloadToPath.DownloadCopyListener copyListener) throws IOException {
		URLConnections.downloadIntoFile(connection, destination, copyListener::downloaded);
	}

	public static URLConnection urlConnectionOf(URL url, String userAgent, TimeoutConfig timeoutConfig, Optional<Proxy> proxy) throws IOException {
		URLConnection openConnection = proxy.isPresent()
			? URLConnections.urlConnectionOf(url, proxy.get())
			: URLConnections.urlConnectionOf(url);

		openConnection.setRequestProperty("User-Agent",userAgent);
		openConnection.setConnectTimeout(timeoutConfig.getConnectionTimeout());
		openConnection.setReadTimeout(timeoutConfig.getReadTimeout());
		return openConnection;
	}

}
