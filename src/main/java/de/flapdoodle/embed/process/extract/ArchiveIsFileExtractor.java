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

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.progress.ProgressListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ArchiveIsFileExtractor implements Extractor {

	@Override
	public ExtractedFileSet extract(DownloadConfig runtime, File source, FilesToExtract toExtract) throws IOException {
		Builder builder = ExtractedFileSet.builder(toExtract.baseDir())
				.baseDirIsGenerated(toExtract.baseDirIsGenerated());

		ProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract (not really) " + source;
		progressListener.start(progressLabel);

		IExtractionMatch match = toExtract.find(new FileAsArchiveEntry(source));
		if (match != null) {
			try (FileInputStream fin = new FileInputStream(source);
				 BufferedInputStream in = new BufferedInputStream(fin)) {
				File file = match.write(in, source.length());
				FileType type = match.type();
				if (type == FileType.Executable) {
					builder.executable(file);
				} else {
					builder.addLibraryFiles(file);
				}

				if (!toExtract.nothingLeft()) {
					progressListener.info(progressLabel,
							"Something went a little wrong. Listener say something is left, but we dont have anything");
				}
				progressListener.done(progressLabel);

			}
		}

		return builder.build();
	}

	static class FileAsArchiveEntry implements IArchiveEntry {

		private final File _source;

		public FileAsArchiveEntry(File source) {
			_source = source;
		}

		@Override
		public boolean isDirectory() {
			return _source.isDirectory();
		}

		@Override
		public String getName() {
			return _source.getName();
		}

	}

}
