package de.flapdoodle.embed.process.parts;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.io.net.UrlStreams;
import de.flapdoodle.embed.process.io.net.UrlStreams.DownloadCopyListener;
import de.flapdoodle.types.Try;

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
			Try.supplier(() -> {
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
