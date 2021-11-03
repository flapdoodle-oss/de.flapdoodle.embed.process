package de.flapdoodle.embed.process.parts;

import java.util.function.Function;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.Distribution;

public interface FileSetOfDistribution extends Function<Distribution, FileSet> {

}
