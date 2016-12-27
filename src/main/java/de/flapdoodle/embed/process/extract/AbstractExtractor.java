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
package de.flapdoodle.embed.process.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.progress.ProgressListener;

public abstract class AbstractExtractor implements Extractor {
	
	private static Logger _logger=LoggerFactory.getLogger(AbstractExtractor.class);
	
	protected abstract ArchiveWrapper archiveStream(File source) throws FileNotFoundException, IOException;

	private ArchiveWrapper archiveStreamWithExceptionHint(File source) throws FileNotFoundException, IOException {
		try {
			return archiveStream(source);
		} catch (IOException iox) {
			_logger.warn("\n--------------------------\n"
                    + "If you get this exception more than once, you should check if the file is corrupt.\n"
                    + "If you remove the file ({}), it will be downloaded again.\n"
                    + "--------------------------", source.getAbsolutePath(), iox);
			throw new IOException("File "+source.getAbsolutePath(),iox);
		}
	}

	@Override
	public ExtractedFileSet extract(DownloadConfig runtime, File source, FilesToExtract toExtract) throws IOException {
		Builder builder = ExtractedFileSet.builder(toExtract.baseDir())
				.baseDirIsGenerated(toExtract.baseDirIsGenerated());

		ProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract " + source;
		progressListener.start(progressLabel);

		ArchiveWrapper archive = archiveStreamWithExceptionHint(source);

		try {
			ArchiveEntry entry;
			while ((entry = archive.getNextEntry()) != null) {
				IExtractionMatch match = toExtract.find(new CommonsArchiveEntryAdapter(entry));
				if (match != null) {
					if (archive.canReadEntryData(entry)) {
						long size = entry.getSize();
						FileType type = match.type();
						File file = match.write(archive.asStream(entry), size);
						if (type==FileType.Executable) {
							builder.executable(file);
						} else {
							builder.addLibraryFiles(file);
						}
						//						destination.setExecutable(true);
						progressListener.info(progressLabel,"extract "+entry.getName());
					}
					if (toExtract.nothingLeft()) {
						progressListener.info(progressLabel,"nothing left");
						break;
					}
				}
			}

		} finally {
			archive.close();
		}
		
		progressListener.done(progressLabel);

		return builder.build();
	}

	protected static interface ArchiveWrapper {

		ArchiveEntry getNextEntry() throws IOException;

		InputStream asStream(ArchiveEntry entry) throws IOException;

		void close() throws IOException;

		boolean canReadEntryData(ArchiveEntry entry);
	}

}
