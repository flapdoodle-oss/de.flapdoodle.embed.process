package de.flapdoodle.embed.process.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.progress.IProgressListener;

public abstract class AbstractExtractor implements IExtractor {

	protected abstract ArchiveWrapper archiveStream(File source) throws FileNotFoundException, IOException;

	@Override
	public IExtractedFileSet extract(IDownloadConfig runtime, File source, FilesToExtract toExtract) throws IOException {
		Builder builder = ImmutableExtractedFileSet.builder();

		IProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract " + source;
		progressListener.start(progressLabel);

		ArchiveWrapper archive = archiveStream(source);

		try {
			ArchiveEntry entry;
			while ((entry = archive.getNextEntry()) != null) {
				IExtractionMatch match = toExtract.find(new CommonsArchiveEntryAdapter(entry));
				if (match != null) {
					if (archive.canReadEntryData(entry)) {
						long size = entry.getSize();
						builder.file(match.type(),match.write(archive.asStream(), size));
						//						destination.setExecutable(true);
					}
					if (toExtract.nothingLeft()) {
						progressListener.done(progressLabel);
						break;
					}
				}
			}

		} finally {
			archive.close();
		}

		return builder.build();
	}

	protected static interface ArchiveWrapper {

		ArchiveEntry getNextEntry() throws IOException;

		InputStream asStream();

		void close() throws IOException;

		boolean canReadEntryData(ArchiveEntry entry);

	}

}
