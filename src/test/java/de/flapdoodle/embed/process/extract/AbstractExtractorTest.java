package de.flapdoodle.embed.process.extract;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import de.flapdoodle.embed.process.config.store.DownloadConfigBuilder;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileSet.Entry;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.example.GenericPackageResolver;
import de.flapdoodle.embed.process.extract.AbstractExtractor.ArchiveWrapper;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.directories.PlatformTempDir;
import de.flapdoodle.embed.process.io.progress.IProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;


public class AbstractExtractorTest {

	@Test(expected=IOException.class)
	public void testForExceptionHint() throws FileNotFoundException, IOException {
		
		IPackageResolver packageResolver=new GenericPackageResolver();
		
		IDownloadConfig runtime=new DownloadConfigBuilder()
			.downloadPath("http://192.168.0.1")
			.downloadPrefix("prefix")
			.packageResolver(packageResolver)
			.artifactStorePath(new PlatformTempDir())
			.fileNaming(new UUIDTempNaming())
			.progressListener(new StandardConsoleProgressListener())
			.userAgent("foo-bar")
			.build();
		
		IDirectory factory=new PlatformTempDir();
		ITempNaming exeutableNaming=new UUIDTempNaming();
		List<Entry> entries=new ArrayList<Entry>();
		entries.add(new Entry(FileType.Executable, "foo-bar.exe", Pattern.compile(".")));
		FileSet fileSet=new FileSet(entries);
		FilesToExtract filesToExtract=new FilesToExtract(factory, exeutableNaming, fileSet);
		
		new AbstractExtractor() {
			
			@Override
			protected ArchiveWrapper archiveStream(File source) throws FileNotFoundException, IOException {
				throw new IOException("foo");
			}
		}.extract(runtime, new File("bar"), filesToExtract);
	}

}
