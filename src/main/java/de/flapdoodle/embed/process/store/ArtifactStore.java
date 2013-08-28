/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.ILibraryStore;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.Extractors;
import de.flapdoodle.embed.process.extract.IExtractor;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.file.Files;


public class ArtifactStore implements IArtifactStore {
	private static Logger logger = Logger.getLogger(ArtifactStore.class.getName());

	private IDownloadConfig _downloadConfig;
	private IDirectory _tempDireFactory;
	private ITempNaming _executableNaming;
	private ILibraryStore _libraries;
	
	public ArtifactStore(IDownloadConfig downloadConfig,IDirectory tempDireFactory,ITempNaming executableNaming,ILibraryStore libraries) {
		_downloadConfig=downloadConfig;
		_tempDireFactory = tempDireFactory;
		_executableNaming = executableNaming;
		_libraries = libraries;
	}
	
	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		if (!LocalArtifactStore.checkArtifact(_downloadConfig, distribution)) {
			return LocalArtifactStore.store(_downloadConfig, distribution, Downloader.download(_downloadConfig, distribution));
		}
		return true;
	}

	@Override
	public File extractExe(Distribution distribution) throws IOException {
		IPackageResolver packageResolver = _downloadConfig.getPackageResolver();
		File artifact = LocalArtifactStore.getArtifact(_downloadConfig, distribution);
		IExtractor extractor = Extractors.getExtractor(packageResolver.getArchiveType(distribution));

		File exe = Files.createTempFile(_tempDireFactory,
				_executableNaming.nameFor("extract", packageResolver.executableFilename(distribution)));
		extractor.extract(_downloadConfig, artifact, exe, packageResolver.executeablePattern(distribution));

		// extract extra libraries, if any
		if (_libraries != null) {
			for (String lib : _libraries.getLibrary(distribution.getPlatform())) {
				File tempDir = _tempDireFactory.asFile();
				File libFile = new File(tempDir, lib);
						libFile.createNewFile();
					extractor.extract(_downloadConfig, artifact,
							libFile, libraryPattern(distribution,lib));
			}
		}
		return exe;
	}

	private Pattern libraryPattern(Distribution distribution, String libname) {
		return Pattern.compile(".*" + libname, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void removeExecutable(Distribution distribution, File executable) {
		if (_libraries != null) {
			for (String lib : _libraries.getLibrary(distribution.getPlatform())) {
				File library = new File(_tempDireFactory.asFile(), lib);
				if (library.exists() && !Files.forceDelete(library))
					logger.warning("Could not delete library NOW: " + library);				
			}
		}
		if (executable.exists() && !Files.forceDelete(executable))
			logger.warning("Could not delete executable NOW: " + executable);
	}
}
