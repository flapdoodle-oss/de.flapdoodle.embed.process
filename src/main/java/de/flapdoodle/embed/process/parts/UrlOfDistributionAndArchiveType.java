package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;

public interface UrlOfDistributionAndArchiveType {
	ArtifactUrl apply(BaseUrl baseUrl, Distribution distribution, ArchiveType archiveType);
}
