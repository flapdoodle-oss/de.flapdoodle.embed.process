package de.flapdoodle.embed.process.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.distribution.IVersion;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.file.Files;


public class StaticArtifactStoreBuilderTest {

	@Test
	public void returnStaticFileSetForDistribution() throws IOException {
		Distribution distribution=Distribution.detectFor(new GenericVersion("13.7.121"));
		
		File generatedBaseDir=Files.createTempDir(PropertyOrPlatformTempDir.defaultInstance(),	"static");
		
		IExtractedFileSet fileSet=ImmutableExtractedFileSet.builder(generatedBaseDir)
				.executable(new File("bla.exe"))
				.file(FileType.Library, new File("foo.lib"))
				.build();
		
		IArtifactStore store = new StaticArtifactStoreBuilder()
			.fileSet(distribution, fileSet)
			.build();
		
		assertTrue(store.checkDistribution(distribution));
		
		IExtractedFileSet extractFileSet = store.extractFileSet(distribution);
		assertNotNull(extractFileSet);
		
		assertEquals("bla.exe", extractFileSet.executable().getName());
		
		Files.forceDelete(generatedBaseDir);
	}
}
