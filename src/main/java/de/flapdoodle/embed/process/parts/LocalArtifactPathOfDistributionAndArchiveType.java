package de.flapdoodle.embed.process.parts;

import java.util.function.BiFunction;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;

public interface LocalArtifactPathOfDistributionAndArchiveType
		extends BiFunction<Distribution, ArchiveType, LocalArtifactPath> {
}
