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

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.Extractors;
import de.flapdoodle.embed.process.extract.FilesToExtract;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.extract.IExtractor;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;

public class ExtractedArtifactStore implements IArtifactStore {
	private final IDownloadConfig _downloadConfig;
	private final IDownloader _downloader;

	private final IDirectory _directory;
	private final IPackageResolver _packageResolver;

	public ExtractedArtifactStore(IDownloadConfig downloadConfig, IDownloader downloader, IDirectory directory) {
		_downloadConfig = downloadConfig;
		_packageResolver = downloadConfig.getPackageResolver();

		_downloader = downloader;
		_directory = directory;
	}

	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		return LocalArtifactStore.checkArtifact(_downloadConfig, distribution)
			|| LocalArtifactStore.store(_downloadConfig, distribution, _downloader.download(_downloadConfig, distribution));
	}

	@Override
	public IExtractedFileSet extractFileSet(Distribution distribution) throws IOException {
		File artifact = LocalArtifactStore.getArtifact(_downloadConfig, distribution);
		File directory = getTargetDirectoryForExtractedFiles(artifact);

		FileSet fileSet = _packageResolver.getFileSet(distribution);

		Map<FileSet.Entry, File> matchingFiles = findMatchingFiles(fileSet, FileUtils.listFiles(directory, null, true));
		List<FileSet.Entry> missingEntries = getMissingEntries(fileSet, matchingFiles.keySet());

		if (missingEntries.isEmpty()) {
			return extractFileSet(matchingFiles, directory);
		}

		IExtractor extractor = Extractors.getExtractor(_packageResolver.getArchiveType(distribution));
		return extractFileSet(artifact, directory, fileSet, extractor);
	}

	@Override
	public void removeFileSet(Distribution distribution, IExtractedFileSet files) {
		// We are not going to drop these
	}

	private File getTargetDirectoryForExtractedFiles(File artifact) throws IOException {
		if (_directory != null) {
			return _directory.asFile();
		}

		File directory = new File(FilenameUtils.removeExtension(artifact.getAbsolutePath()));
		FileUtils.forceMkdir(directory);

		return directory;
	}

	private IExtractedFileSet extractFileSet(Map<FileSet.Entry, File> matchingFiles, File directory) {
		ImmutableExtractedFileSet.Builder builder = ImmutableExtractedFileSet.builder(directory);

		for (Map.Entry<FileSet.Entry, File> entry : matchingFiles.entrySet()) {
			if (entry.getKey().type() == FileType.Executable) {
				builder.executable(entry.getValue());
			} else {
				builder.file(entry.getKey().type(), entry.getValue());
			}
		}

		return builder.build();
	}

	private IExtractedFileSet extractFileSet(File artifact, File directory, FileSet fileSet, IExtractor extractor) throws IOException {
		ImmutableExtractedFileSet.Builder builder = ImmutableExtractedFileSet.builder(directory);

		IExtractedFileSet extractedFileSet = extractor.extract(_downloadConfig, artifact, new FilesToExtract(
				new FixedPath(directory.getAbsolutePath()), new UUIDTempNaming(), fileSet));

		if (extractedFileSet.executable() != null) {
			builder.file(FileType.Executable, extractedFileSet.executable());
		}

		for (FileSet.Entry entry : fileSet.entries()) {
			for (File file : extractedFileSet.files(entry.type())) {
				builder.file(entry.type(), file);
			}
		}

		return builder.build();
	}

	private Map<FileSet.Entry, File> findMatchingFiles(FileSet fileSet, Collection<File> files) {
		Map<FileSet.Entry, File> matchingEntries = new HashMap<FileSet.Entry, File>();

		for (FileSet.Entry entry : fileSet.entries()) {
			Map.Entry<FileSet.Entry, File> matchingFile = findMatchingFile(files, entry);

			if (matchingFile != null) {
				matchingEntries.put(matchingFile.getKey(), matchingFile.getValue());
			}
		}

		return matchingEntries;
	}

	private Map.Entry<FileSet.Entry, File> findMatchingFile(Collection<File> files, FileSet.Entry entry) {
		for (File file : files) {
			if (entry.matchingPattern().matcher(file.getName()).matches()) {
				return new AbstractMap.SimpleEntry<FileSet.Entry, File>(entry, file);
			}
		}

		return null;
	}

	private List<FileSet.Entry> getMissingEntries(FileSet fileSet, Collection<FileSet.Entry> existingEntries) {
		List<FileSet.Entry> missingEntries = new ArrayList<FileSet.Entry>();

		for (FileSet.Entry entry : fileSet.entries()) {
			if (!existingEntries.contains(entry)) {
				missingEntries.add(entry);
			}
		}

		return missingEntries;
	}
}
