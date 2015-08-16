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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.flapdoodle.embed.process.TempDir;
import de.flapdoodle.embed.process.config.store.DownloadConfigBuilder;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.extract.NoopTempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;


public class ExtractedArtifactStoreTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
	@Test
	public void askingForArtifactShouldExtractAndKeepFiles() throws IOException {
		Distribution distribution = Distribution.detectFor(new GenericVersion("1.0.37"));
		
		IDirectory artifactDir=new TempDir(tempFolder);
		IDirectory extractedArtifactDir=new TempDir(tempFolder);
		
		File source = new File(this.getClass().getResource("/mocks/mocked-artifact.zip").getPath());
		Path copiedFile = Files.copy(source.toPath(), artifactDir.asFile().toPath().resolve(artefactName(distribution)), StandardCopyOption.REPLACE_EXISTING);
		assertNotNull(copiedFile);
		
		IArtifactStore store = new ExtractedArtifactStoreBuilder()
			.download(downloadConfig(artifactDir))
			.executableNaming(new NoopTempNaming())
			.downloader(failingDownloader())
			.extractDir(extractedArtifactDir)
			.build();
		
		assertTrue("checkDistribution ("+distribution+")", store.checkDistribution(distribution));
		
		// extract files if not exists
		IExtractedFileSet extractFileSet = store.extractFileSet(distribution);
		assertNotNull(extractFileSet);
		assertEquals(1, extractFileSet.files(FileType.Library).size());
		
		File extractedExeFile = fileOf(extractFileSet.generatedBaseDir(),extractFileSet.executable());
		assertTrue(extractedExeFile.exists());
		
		assertTrue("Remove extracted exe", extractedExeFile.delete());
		for (File f : extractFileSet.files(FileType.Library)) {
			assertTrue("Remove extracted file "+f, fileOf(extractFileSet.generatedBaseDir(), f).delete());
		}
		assertFalse(extractedExeFile.exists());

		// 
		extractFileSet = store.extractFileSet(distribution);
		assertTrue(extractedExeFile.exists());
		assertTrue("Remove extracted exe", extractedExeFile.delete());
		assertFalse(extractedExeFile.exists());
	}

	private static File fileOf(File base, File relative) {
		return de.flapdoodle.embed.process.io.file.Files.fileOf(base,relative);
	}

	private static String artefactName(Distribution distribution) {
		return ExtractedArtifactStore.asPath(distribution)+".zip";
	}
	
	private static IDownloader failingDownloader() {
		return new IDownloader() {
			
			@Override
			public String getDownloadUrl(IDownloadConfig runtime,
					Distribution distribution) {
				throw new RuntimeException("should not be called ("+distribution+")");
			}
			
			@Override
			public File download(IDownloadConfig runtime, Distribution distribution)
					throws IOException {
				throw new RuntimeException("should not be called ("+distribution+")");
			}
		};
	}

	private IDownloadConfig downloadConfig(IDirectory artifactDir) {
		return new DownloadConfigBuilder()
			.downloadPrefix("prefix")
			.downloadPath("foo")
			.packageResolver(packageResolver())
			.artifactStorePath(artifactDir)
			.fileNaming(new UUIDTempNaming())
			.progressListener(new StandardConsoleProgressListener())
			.userAgent("foo-bar")
			.build();
	}

	private IPackageResolver packageResolver() {
		return new IPackageResolver() {
			
			@Override
			public String getPath(Distribution distribution) {
				return artefactName(distribution);
			}
			
			@Override
			public FileSet getFileSet(Distribution distribution) {
				return new FileSet.Builder()
					.addEntry(FileType.Executable, "my-prog.bat")
					.addEntry(FileType.Library, "lib/my-lib.txt", ".*my-lib.txt")
					.build();
			}
			
			@Override
			public ArchiveType getArchiveType(Distribution distribution) {
				return ArchiveType.ZIP;
			}
		};
	}
}
