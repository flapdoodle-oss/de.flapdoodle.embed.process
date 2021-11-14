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
package de.flapdoodle.embed.process.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.flapdoodle.embed.process.TempDir;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.TempDirInPlatformTempDir;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;


public class ExtractedArtifactStoreTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
	@Test
	public void askingForArtifactShouldExtractAndKeepFiles() throws IOException {
		Distribution distribution = Distribution.detectFor(Version.of("1.0.37"));
		
		Directory artifactDir=new TempDir(tempFolder);
		Directory extractedArtifactDir=new TempDir(tempFolder);
		
		File source = new File(this.getClass().getResource("/mocks/mocked-artifact.zip").getPath());
		Path copiedFile = Files.copy(source.toPath(), artifactDir.asFile().toPath().resolve(artefactName(distribution)), StandardCopyOption.REPLACE_EXISTING);
		assertNotNull(copiedFile);
		
		ImmutableExtractedArtifactStore store = ExtractedArtifactStore.builder()
				.downloader(failingDownloader())
			.downloadConfig(downloadConfig(artifactDir))
			.extraction(DirectoryAndExecutableNaming.builder()
					.directory(extractedArtifactDir)
					.executableNaming(new UUIDTempNaming())
					.build())
			.temp(DirectoryAndExecutableNaming.builder()
					.directory(new TempDirInPlatformTempDir())
					.executableNaming(new UUIDTempNaming())
					.build())
			.build();
		
		// extract files if not exists
		Optional<ExtractedFileSet> optExtractFileSet = store.extractFileSet(distribution);
		assertTrue(optExtractFileSet.isPresent());
		ExtractedFileSet extractFileSet=optExtractFileSet.get();
		
		assertEquals(1, extractFileSet.libraryFiles().size());
		
		File extractedExeFile = fileOf(extractFileSet.baseDir(),extractFileSet.executable());
		assertTrue(extractedExeFile.exists());
		
		assertTrue("Remove extracted exe", extractedExeFile.delete());
		for (File f : extractFileSet.libraryFiles()) {
			assertTrue("Remove extracted file "+f, fileOf(extractFileSet.baseDir(), f).delete());
		}
		assertFalse(extractedExeFile.exists());

		// 
		optExtractFileSet = store.extractFileSet(distribution);
		extractFileSet=optExtractFileSet.get();
		extractedExeFile = fileOf(extractFileSet.baseDir(),extractFileSet.executable());
		assertTrue(""+extractedExeFile+".exists()",extractedExeFile.exists());
		assertTrue("Remove extracted exe", extractedExeFile.delete());
		assertFalse(extractedExeFile.exists());
	}

	private static File fileOf(File base, File relative) {
		return de.flapdoodle.embed.process.io.file.Files.fileOf(base,relative);
	}

	private static String artefactName(Distribution distribution) {
		return ExtractedArtifactStore.asPath(distribution)+".zip";
	}
	
	private static Downloader failingDownloader() {
		return (runtime, url) -> {
			throw new RuntimeException("should not be called ("+url+")");
		};
	}

	private DownloadConfig downloadConfig(Directory artifactDir) {
		return DownloadConfig.builder()
			.downloadPath(__ -> "foo")
			.packageResolver(packageResolver())
			.artifactStorePath(artifactDir)
			.fileNaming(new UUIDTempNaming())
			.progressListener(new StandardConsoleProgressListener())
			.userAgent("foo-bar")
			.build();
	}

	private PackageResolver packageResolver() {
		return new PackageResolver() {
			
			@Override
			public DistributionPackage packageFor(Distribution distribution) {
				return DistributionPackage.of(getArchiveType(distribution), getFileSet(distribution), getPath(distribution));
			}
			
			public String getPath(Distribution distribution) {
				return artefactName(distribution);
			}
			
			public FileSet getFileSet(Distribution distribution) {
				return new FileSet.Builder()
					.addEntry(FileType.Executable, "my-prog.bat")
					.addEntry(FileType.Library, "lib/my-lib.txt", ".*my-lib.txt")
					.build();
			}
			
			public ArchiveType getArchiveType(Distribution distribution) {
				return ArchiveType.ZIP;
			}
		};
	}
}
