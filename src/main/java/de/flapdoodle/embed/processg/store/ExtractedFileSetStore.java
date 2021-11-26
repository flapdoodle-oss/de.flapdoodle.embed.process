package de.flapdoodle.embed.processg.store;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.processg.extract.ExtractedFileSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface ExtractedFileSetStore {
	Optional<ExtractedFileSet> extractedFileSet(Distribution distribution, FileSet fileSet);

	ExtractedFileSet store(Distribution distribution, ExtractedFileSet src) throws IOException;
}
