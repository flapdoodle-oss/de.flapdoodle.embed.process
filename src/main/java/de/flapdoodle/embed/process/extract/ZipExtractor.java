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
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 *
 */
public class ZipExtractor extends AbstractExtractor {

    @Override
    protected ArchiveWrapper archiveStream(File source) throws IOException {
        ZipFile zipIn = new ZipFile(source);
        return new ZipArchiveWrapper(zipIn);
    }

    protected static class ZipArchiveWrapper implements ArchiveWrapper {

        private final Enumeration<ZipArchiveEntry> entries;
        private final ZipFile zFile;

        public ZipArchiveWrapper(ZipFile source) {
            zFile = source;
            entries = source.getEntries();
        }

        @Override
        public ArchiveEntry getNextEntry() {
            return entries.hasMoreElements() ? entries.nextElement() : null;
        }

        @Override
        public boolean canReadEntryData(ArchiveEntry entry) {
            return zFile.canReadEntryData(zFile.getEntry(entry.getName()));
        }

        @Override
        public void close() throws IOException {
            zFile.close();
        }

        @Override
        public InputStream asStream(ArchiveEntry entry) throws IOException {
            return zFile.getInputStream(zFile.getEntry(entry.getName()));
        }
    }
}
