package de.flapdoodle.embed.process.parts;

public interface ArtifactPathForUrl {
	ArtifactPath apply(ArtifactsBasePath basePath, ArtifactUrl url, LocalArtifactPath localPath);
}
