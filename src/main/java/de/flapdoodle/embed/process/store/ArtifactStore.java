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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.extract.ExtractedFileSets;
import de.flapdoodle.embed.process.extract.Extractor;
import de.flapdoodle.embed.process.extract.Extractors;
import de.flapdoodle.embed.process.extract.FilesToExtract;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;


public class ArtifactStore implements IArtifactStore {
	private static Logger logger = LoggerFactory.getLogger(ArtifactStore.class);

	private final DownloadConfig _downloadConfig;
	private final Directory _tempDirFactory;
	private final ITempNaming _executableNaming;
	private final IDownloader _downloader;
	
	public ArtifactStore(DownloadConfig downloadConfig,Directory tempDirFactory,ITempNaming executableNaming,IDownloader downloader) {
		_downloadConfig=downloadConfig;
		_tempDirFactory = tempDirFactory;
		_executableNaming = executableNaming;
		_downloader = downloader;
	}
	
	public ArtifactStore with(Directory tempDirFactory,ITempNaming executableNaming) {
		return new ArtifactStore(_downloadConfig, tempDirFactory, executableNaming, _downloader);
	}
	
	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		if (!LocalArtifactStore.checkArtifact(_downloadConfig, distribution)) {
			return LocalArtifactStore.store(_downloadConfig, distribution, _downloader.download(_downloadConfig, distribution));
		}
		return true;
	}

	@Override
	public ExtractedFileSet extractFileSet(Distribution distribution) throws IOException {
		PackageResolver packageResolver = _downloadConfig.getPackageResolver();
		FilesToExtract toExtract = filesToExtract(distribution);
		
		Extractor extractor = Extractors.getExtractor(packageResolver.packageFor(distribution).archiveType());

		File artifact = LocalArtifactStore.getArtifact(_downloadConfig, distribution);
		return extractor.extract(_downloadConfig, artifact, toExtract);
	}

	FilesToExtract filesToExtract(Distribution distribution) {
		return new FilesToExtract(_tempDirFactory, _executableNaming, _downloadConfig.getPackageResolver().packageFor(distribution).fileSet());
	}

	@Override
	public void removeFileSet(Distribution distribution, ExtractedFileSet all) {
		ExtractedFileSets.delete(all);
	}
}
