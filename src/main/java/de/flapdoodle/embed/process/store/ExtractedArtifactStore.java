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

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableDownloadConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.*;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.file.FileAlreadyExistsException;
import org.immutables.value.Value.Immutable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Immutable
public abstract class ExtractedArtifactStore implements IArtifactStore {

	abstract DownloadConfig downloadConfig();
	abstract Downloader downloader();
	abstract DirectoryAndExecutableNaming extraction();
	abstract DirectoryAndExecutableNaming temp();

	private ArtifactStore store(Directory withDistribution, TempNaming naming) {
		return ArtifactStore.builder()
				.downloadConfig(downloadConfig())
				.tempDirFactory(withDistribution)
				.executableNaming(naming)
				.downloader(downloader())
				.build();
	}
	
	@Deprecated
	public ExtractedArtifactStore executableNaming(TempNaming tempNaming) {
		return ImmutableExtractedArtifactStore.copyOf(this)
				.withExtraction(ImmutableDirectoryAndExecutableNaming.copyOf(extraction()).withExecutableNaming(tempNaming));
	}
	
	@Deprecated
	public ExtractedArtifactStore download(ImmutableDownloadConfig.Builder downloadConfigBuilder) {
		return ImmutableExtractedArtifactStore.copyOf(this).withDownloadConfig(downloadConfigBuilder.build());
	}

	@Override
	public Optional<ExtractedFileSet> extractFileSet(Distribution distribution)
			throws IOException {
		
		Directory withDistribution = withDistribution(extraction().getDirectory(), distribution);
		ArtifactStore baseStore = store(withDistribution, extraction().getExecutableNaming());
		
		boolean foundExecutable=false;
		File destinationDir = withDistribution.asFile();
		
		Builder fileSetBuilder = ExtractedFileSet.builder(destinationDir)
				.baseDirIsGenerated(withDistribution.isGenerated());
		
		FilesToExtract filesToExtract = baseStore.filesToExtract(distribution);
		for (FileSet.Entry file : filesToExtract.files()) {
			if (file.type()==FileType.Executable) {
				String executableName = FilesToExtract.executableName(extraction().getExecutableNaming(), file);
				File executableFile = new File(executableName);
				File resolvedExecutableFile = new File(destinationDir, executableName);
				if (resolvedExecutableFile.isFile()) {
					foundExecutable=true;
				}
				fileSetBuilder.executable(executableFile);
			} else {
				fileSetBuilder.addLibraryFiles(new File(FilesToExtract.fileName(file)));
			}
		}

		ExtractedFileSet extractedFileSet;
		if (!foundExecutable) {
			// we found no executable, so we trigger extraction and hope for the best
			try {
				extractedFileSet = baseStore.extractFileSet(distribution).get();
			} catch (FileAlreadyExistsException fx) {
				throw new RuntimeException("extraction to "+destinationDir+" has failed", fx);
			}
		} else {
			extractedFileSet = fileSetBuilder.build();
		}
		return Optional.ofNullable(ExtractedFileSets.copy(extractedFileSet, temp().getDirectory(), temp().getExecutableNaming()));
	}

	private static Directory withDistribution(final Directory dir, final Distribution distribution) {
		return new Directory() {
			
			@Override
			public boolean isGenerated() {
				return dir.isGenerated();
			}
			
			@Override
			public File asFile() {
				File file = new File(dir.asFile(), asPath(distribution));
				if (!file.exists()) {
					if (!file.mkdirs()) {
						throw new RuntimeException("could not create dir "+file);
					}
				}
				return file;
			}

		};
	}

	static String asPath(Distribution distribution) {
		return distribution.platform().operatingSystem().name() + "-" +
				distribution.platform().architecture().bitSize() + "--" +
				distribution.version().asInDownloadPath();
	}
	
	@Override
	public void removeFileSet(Distribution distribution, ExtractedFileSet files) {
		ExtractedFileSets.delete(files);
	}
	
	public static ImmutableExtractedArtifactStore.Builder builder() {
		return ImmutableExtractedArtifactStore.builder();
	}

	public static ExtractedArtifactStore of(DownloadConfig downloadConfig, Downloader downloader, DirectoryAndExecutableNaming extract, DirectoryAndExecutableNaming temp) {
		return builder()
				.downloadConfig(downloadConfig)
				.downloader(downloader)
				.extraction(extract)
				.temp(temp)
				.build();
	}
}
