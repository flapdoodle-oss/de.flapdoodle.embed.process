/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.progress.IProgressListener;

public class ArchiveIsFileExtractor implements IExtractor {

	@Override
	public IExtractedFileSet extract(IDownloadConfig runtime, File source, FilesToExtract toExtract) throws IOException {
		Builder builder = ImmutableExtractedFileSet.builder(toExtract.generatedBaseDir());

		IProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract (not really) " + source;
		progressListener.start(progressLabel);

		IExtractionMatch match = toExtract.find(new FileAsArchiveEntry(source));
		if (match != null) {
			FileInputStream fin = new FileInputStream(source);
			try {
				BufferedInputStream in = new BufferedInputStream(fin);
				builder.file(match.type(), match.write(in, source.length()));

				if (!toExtract.nothingLeft()) {
					progressListener.info(progressLabel,
							"Something went a little wrong. Listener say something is left, but we dont have anything");
				}
				progressListener.done(progressLabel);

			} finally {
				fin.close();
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
