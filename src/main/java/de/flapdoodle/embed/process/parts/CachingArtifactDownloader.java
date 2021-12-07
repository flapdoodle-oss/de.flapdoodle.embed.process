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
package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.net.UrlStreams;
import de.flapdoodle.embed.process.net.UrlStreams.DownloadCopyListener;
import de.flapdoodle.types.Try;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

public class CachingArtifactDownloader implements ArtifactPathForUrl {

	private final TimeoutConfig timeoutConfig = TimeoutConfig.defaults();
	private final DownloadCopyListener listener;

	public CachingArtifactDownloader(DownloadCopyListener listener) {
		this.listener = listener;
	}

	@Override
	public ArtifactPath apply(ArtifactsBasePath basePath, ArtifactUrl url, LocalArtifactPath localPath) {
		Path artifactPath = basePath.value().resolve(localPath.value());

		if (!artifactPath.toFile().exists()) {
			return Try.supplier(() -> {
				URL downloadUrl = new URL(url.value());
				URLConnection connection = UrlStreams.urlConnectionOf(downloadUrl, "flapdoodle-user-agent", timeoutConfig,
						Optional.empty());
				UrlStreams.downloadTo(connection, artifactPath, listener);
				return ArtifactPath.of(artifactPath);
			})
					.mapCheckedException(RuntimeException::new)
					.get();
		}
		return ArtifactPath.of(artifactPath);
	}
}
