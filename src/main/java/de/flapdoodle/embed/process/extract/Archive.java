package de.flapdoodle.embed.process.extract;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.io.InputStream;

public interface Archive {
	interface Wrapper {
		ArchiveEntry getNextEntry() throws IOException;

		InputStream asStream(ArchiveEntry entry) throws IOException;

		void close() throws IOException;

		boolean canReadEntryData(ArchiveEntry entry);
	}

	interface Entry {
		boolean isDirectory();

		String getName();
	}
}
