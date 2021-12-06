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

import de.flapdoodle.embed.process.extract.Archive;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractTarAdapter extends AbstractExtractFileSet {

	protected static class TarArchiveWrapper implements Archive.Wrapper {
	
			private final TarArchiveInputStream _is;
	
			public TarArchiveWrapper(TarArchiveInputStream is) {
				_is = is;
			}
	
			@Override
			public ArchiveEntry getNextEntry() throws IOException {
				return _is.getNextTarEntry();
			}
	
			@Override
			public boolean canReadEntryData(ArchiveEntry entry) {
				return _is.canReadEntryData(entry);
			}
	
			@Override
			public void close() throws IOException {
				_is.close();
			}
	
			@Override
			public InputStream asStream(ArchiveEntry entry) {
				return _is;
			}
	
		}

}
