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

import de.flapdoodle.embed.process.config.TimeoutConfig;
import de.flapdoodle.embed.process.io.progress.ProgressListener;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public interface DownloadToPath {
	void download(
		URL url,
		Path destination,
		Optional<Proxy> proxy,

		String userAgent,
		TimeoutConfig timeoutConfig,
		
		DownloadCopyListener copyListener
	) throws IOException;

	interface DownloadCopyListener {
		void downloaded(URL url, long bytesCopied, long contentLength);
	}

	static DownloadCopyListener downloadCopyListenerDelegatingTo(ProgressListener progressListener) {
		return (url, bytesCopied, contentLength) -> {
			if (bytesCopied == 0) {
				progressListener.start("download " + url);
			} else {
				if (contentLength!=-1L) {
					if (bytesCopied == contentLength) {
						progressListener.done("download " + url);
					} else {
						int percent = (int) (bytesCopied * 100 / contentLength);
						progressListener.progress("download " + url, percent);
					}
				} else {
					progressListener.info("download "+url, bytesCopied + " bytes");
				}
			}
		};
	}
}
