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
package de.flapdoodle.embed.processg.extract;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.extract.Archive;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class SingleFileAdapter extends AbstractExtractFileSet {

	@Override protected Archive.Wrapper archiveStream(Path source) throws IOException {
		return new SingleFileAsArchiveWrapper(source);
	}

	static class SingleFileAsArchiveWrapper implements Archive.Wrapper {

		private final SingleFileEntryWrapper singleFile;
		private boolean nextCalled=false;

		public SingleFileAsArchiveWrapper(Path singleFile) throws IOException {
			this.singleFile = new SingleFileEntryWrapper(singleFile.toFile());
		}

		@Override public ArchiveEntry getNextEntry() throws IOException {
			if (!nextCalled) {
				nextCalled=true;
				return singleFile;
			}
			return null;
		}

		@Override public InputStream asStream(ArchiveEntry entry) throws IOException {
			Preconditions.checkArgument(entry==singleFile,"unexpected entry: %s", entry);
			return singleFile.inputStream();
		}

		@Override public void close() throws IOException {
			singleFile.close();
		}

		@Override public boolean canReadEntryData(ArchiveEntry entry) {
			return entry == singleFile;
		}
	}

	static class SingleFileEntryWrapper implements ArchiveEntry {

		private final File wrapped;
		private final Date lastModified;
		private final long length;
		private final boolean isDirectory;
		private FileInputStream inputStream;

		public SingleFileEntryWrapper(File wrapped) throws IOException{
			this.wrapped = wrapped;
			BasicFileAttributes attr = Files.readAttributes(wrapped.toPath(), BasicFileAttributes.class);
			this.lastModified = Date.from(attr.lastModifiedTime().toInstant());
			this.length = attr.size();
			this.isDirectory = attr.isDirectory();
		}

		@Override public String getName() {
			return wrapped.getName();
		}
		@Override public long getSize() {
			return length;
		}
		@Override public boolean isDirectory() {
			return isDirectory;
		}
		@Override public Date getLastModifiedDate() {
			return lastModified;
		}

		public InputStream inputStream() throws FileNotFoundException {
			Preconditions.checkArgument(inputStream==null,"inputStream already created");
			this.inputStream = new FileInputStream(wrapped);
			return inputStream;
		}

		public void close() throws IOException {
			if (inputStream!=null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}
}
