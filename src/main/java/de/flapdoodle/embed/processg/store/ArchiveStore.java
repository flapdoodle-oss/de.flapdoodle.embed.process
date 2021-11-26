package de.flapdoodle.embed.processg.store;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.processg.extract.ArchiveType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface ArchiveStore {
	Optional<Path> archiveFor(String name, Distribution distribution, ArchiveType archiveType);

	Path store(String name, Distribution distribution, ArchiveType archiveType, Path archive) throws IOException;
}
