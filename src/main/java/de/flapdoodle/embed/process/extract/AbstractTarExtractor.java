package de.flapdoodle.embed.process.extract;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;


public abstract class AbstractTarExtractor extends AbstractExtractor {

	protected static class TarArchiveWrapper implements ArchiveWrapper {
	
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
			public InputStream asStream() {
				return _is;
			}
	
		}

}
