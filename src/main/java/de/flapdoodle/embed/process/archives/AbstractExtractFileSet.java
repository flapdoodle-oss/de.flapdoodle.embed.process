/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.archives;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.io.Files;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractExtractFileSet implements ExtractFileSet {

	protected abstract ArchiveStream archiveStream(Path source) throws IOException;

	private ArchiveStream archiveStreamWithExceptionHint(Path source) throws IOException {
		try {
			return archiveStream(source);
		} catch (IOException iox) {
			throw new IOException("You should check if the file is corrupt: "+source.toAbsolutePath(),iox);
		}
	}

	@Override
	public ExtractedFileSet extract(Path destination, Path archive, FileSet filesToExtract) throws IOException {
		ImmutableExtractedFileSet.Builder builder = ExtractedFileSet.builder(destination);

		ArchiveStream wrapper = archiveStreamWithExceptionHint(archive);
		Tracker tracker = new Tracker(filesToExtract);

		try {
			org.apache.commons.compress.archivers.ArchiveEntry archiveEntry;
			while ((archiveEntry = wrapper.getNextEntry()) != null) {
				Optional<FileSet.Entry> matchingEntry = tracker.find(archiveEntry);
				if (matchingEntry.isPresent()) {
					if (wrapper.canReadEntryData(archiveEntry)) {
						long size = archiveEntry.getSize();
						FileType type = matchingEntry.get().type();
						Path dest = destination.resolve(matchingEntry.get().destination());
						Files.write(wrapper.asStream(archiveEntry), size, dest);

						if (type==FileType.Executable) {
							builder.executable(dest);
							if (!dest.toFile().setExecutable(true)) {
								throw new IllegalArgumentException("could not set executable flag on "+ dest);
							}
						} else {
							builder.addLibraryFiles(dest);
						}
					} else {
						throw new IllegalArgumentException("could not read "+archiveEntry);
					}
				}
				if (tracker.nothingLeft()) {
					break;
				}
			}

		} finally {
			wrapper.close();
		}

		return builder.build();
	}

	static class Tracker {
		private final ArrayList<FileSet.Entry> files;

		public Tracker(FileSet fileSet) {
			this.files = new ArrayList<>(fileSet.entries());
		}

		public boolean nothingLeft() {
			return files.isEmpty();
		}

		public Optional<FileSet.Entry> find(ArchiveEntry entry) {
			Optional<FileSet.Entry> ret = Optional.empty();

			if (!entry.isDirectory()) {
				ret = findMatchingEntry(files, entry);

				if (ret.isPresent()) {
					files.remove(ret.get());
				}
			}
			return ret;
		}

	}


	static Optional<FileSet.Entry> findMatchingEntry(List<FileSet.Entry> files, ArchiveEntry entry) {
		return files.stream()
			.filter(e -> e.matchingPattern().matcher(entry.getName()).matches())
			.findFirst();
	}
}
