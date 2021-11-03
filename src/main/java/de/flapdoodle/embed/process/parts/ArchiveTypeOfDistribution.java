package de.flapdoodle.embed.process.parts;

import java.util.function.Function;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;

public interface ArchiveTypeOfDistribution extends Function<Distribution, ArchiveType> {

}
