package de.flapdoodle.embed.process.extract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.io.progress.IProgressListener;


public class Tbz2Extractor implements IExtractor {

	@Override
	public void extract(IDownloadConfig runtime, File source, File destination, Pattern file) throws IOException {
		IProgressListener progressListener = runtime.getProgressListener();
		String progressLabel = "Extract " + source;
		progressListener.start(progressLabel);

		FileInputStream fin = new FileInputStream(source);
		BufferedInputStream in = new BufferedInputStream(fin);
		BZip2CompressorInputStream gzIn = new BZip2CompressorInputStream(in);

		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
		try {
			TarArchiveEntry entry;
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if (file.matcher(entry.getName()).matches()) {
					if (tarIn.canReadEntryData(entry)) {
						long size = entry.getSize();
						Files.write(tarIn, size, destination);
						destination.setExecutable(true);
						progressListener.done(progressLabel);
					}
					break;

				}
			}

		} finally {
			tarIn.close();
			gzIn.close();
		}

	}

}
