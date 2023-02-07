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

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.config.TimeoutConfig;
import de.flapdoodle.types.Optionals;
import de.flapdoodle.types.ThrowingFunction;
import de.flapdoodle.types.ThrowingSupplier;

import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public abstract class UrlStreams {

	static final int BUFFER_LENGTH = 1024 * 8 * 8;
	static final int READ_COUNT_MULTIPLIER = 100;

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
		downloadTo(connection, destination, c -> downloadIntoTempFile(c, copyListener));
	}

	protected static <E extends Exception> void downloadTo(URLConnection connection, Path destination, ThrowingFunction<URLConnection, Path, E> urlToTempFile) throws IOException,E {
		Preconditions.checkArgument(!destination.toFile().exists(), "destination exists: %s",destination);
		Path tempFile = urlToTempFile.apply(connection);
		Files.copy(tempFile, destination);
		Files.delete(tempFile);
	}
	
	protected static Path downloadIntoTempFile(URLConnection connection, DownloadToPath.DownloadCopyListener copyListener) throws IOException, FileNotFoundException {
		Path tempFile = java.nio.file.Files.createTempFile("download", "");
		boolean downloadSucceeded=false; 
		try {
			downloadAndCopy(connection, () -> new BufferedOutputStream(Files.newOutputStream(tempFile.toFile().toPath())), copyListener);
			downloadSucceeded=true;
			return tempFile;
		} finally {
			if (!downloadSucceeded) {
				Files.delete(tempFile);
			}
		}
	}

	private static <E extends Exception> void downloadAndCopy(URLConnection connection, ThrowingSupplier<BufferedOutputStream, E> output, DownloadToPath.DownloadCopyListener copyListener) throws IOException, E {
		long length = connection.getContentLengthLong();
		copyListener.downloaded(connection.getURL(), 0, length);
		try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
			try (BufferedOutputStream bos = output.get()) {
				byte[] buf = new byte[BUFFER_LENGTH];
				int read = 0;
				long readCount = 0;
				while ((read = bis.read(buf)) != -1) {
					bos.write(buf, 0, read);
					readCount = readCount + read;
					Preconditions.checkArgument(length==-1 || length>=readCount, "hmm.. readCount bigger than contentLength(more than we want to): %s > %s",readCount, length);
					copyListener.downloaded(connection.getURL(), readCount, length);
				}
				bos.flush();
				Preconditions.checkArgument(length==-1 || length==readCount, "hmm.. readCount smaller than contentLength(partial download?): %s > %s",readCount, length);
			}
		}
	}

	public static URLConnection urlConnectionOf(URL url, String userAgent, TimeoutConfig timeoutConfig, Optional<Proxy> proxy) throws IOException {
		URLConnection openConnection = Optionals.with(proxy)
			.map(url::openConnection)
			.orElseGet(url::openConnection);
		
		openConnection.setRequestProperty("User-Agent",userAgent);
		openConnection.setConnectTimeout(timeoutConfig.getConnectionTimeout());
		openConnection.setReadTimeout(timeoutConfig.getReadTimeout());
		return openConnection;
	}

}
