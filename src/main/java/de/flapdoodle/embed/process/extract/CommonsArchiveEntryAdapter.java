package de.flapdoodle.embed.process.extract;

import org.apache.commons.compress.archivers.ArchiveEntry;

public class CommonsArchiveEntryAdapter implements IArchiveEntry {

	private final ArchiveEntry _entry;

	public CommonsArchiveEntryAdapter(ArchiveEntry entry) {
		_entry = entry;
	}

	public String getName() {
		return _entry.getName();
	}

	public boolean isDirectory() {
		return _entry.isDirectory();
	}
}
