package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.archives.ArchiveType;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public interface DownloadCache {
	Optional<Path> archiveFor(URL url, ArchiveType archiveType);

	Path store(URL url, ArchiveType archiveType, Path archive) throws IOException;
}
