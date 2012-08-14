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
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.io.progress.IProgressListener;

/**
 *
 */
public class ZipExtractor implements IExtractor {
	@Override
	public void extract(IDownloadConfig runtime, File source, File destination, Pattern file) throws IOException {
		IProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract " + source;
		progressListener.start(progressLabel);

		FileInputStream fin = new FileInputStream(source);
		BufferedInputStream in = new BufferedInputStream(fin);

		ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in);
		try {
			ZipArchiveEntry entry;
			while ((entry = zipIn.getNextZipEntry()) != null) {
				if (file.matcher(entry.getName()).matches()) {
					if (zipIn.canReadEntryData(entry)) {
						long size = entry.getSize();
						Files.write(zipIn, size, destination);
						destination.setExecutable(true);
						progressListener.done(progressLabel);
					}
					break;

				}
			}

		} finally {
			zipIn.close();
		}

	}
}
