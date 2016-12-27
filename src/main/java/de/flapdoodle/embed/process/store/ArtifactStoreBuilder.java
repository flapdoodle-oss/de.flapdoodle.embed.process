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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;

public class ArtifactStoreBuilder extends AbstractBuilder<IArtifactStore> {
	private static Logger logger = LoggerFactory.getLogger(ArtifactStoreBuilder.class);

	protected static final TypedProperty<ITempNaming> EXECUTABLE_NAMING = TypedProperty.with("ExecutableNaming",ITempNaming.class);
	protected static final TypedProperty<Directory> TEMP_DIR_FACTORY = TypedProperty.with("TempDir",Directory.class);
	protected static final TypedProperty<DownloadConfig> DOWNLOAD_CONFIG = TypedProperty.with("DownloadConfig",DownloadConfig.class);
	protected static final TypedProperty<Boolean> USE_CACHE = TypedProperty.with("UseCache",Boolean.class);
	protected static final TypedProperty<IDownloader> DOWNLOADER = TypedProperty.with("Downloader",IDownloader.class);
	
	@Deprecated
	public ArtifactStoreBuilder download(AbstractBuilder<DownloadConfig> downloadConfigBuilder) {
		return download(downloadConfigBuilder.build());
	}
	
	public ArtifactStoreBuilder download(DownloadConfig downloadConfig) {
		set(DOWNLOAD_CONFIG, downloadConfig);
		return this;
	}

	protected IProperty<DownloadConfig> download() {
		return property(DOWNLOAD_CONFIG);
	}

	public ArtifactStoreBuilder tempDir(Directory tempDirFactory) {
		set(TEMP_DIR_FACTORY, tempDirFactory);
		return this;
	}
	
	protected IProperty<Directory> tempDir() {
		return property(TEMP_DIR_FACTORY);
	}

	public ArtifactStoreBuilder executableNaming(ITempNaming execNaming) {
		set(EXECUTABLE_NAMING,execNaming);
		return this;
	}
	
	protected IProperty<ITempNaming> executableNaming() {
		return property(EXECUTABLE_NAMING);
	}

	public ArtifactStoreBuilder useCache(boolean cache) {
		set(USE_CACHE, cache);
		return this;
	}
	
	protected IProperty<Boolean> useCache() {
		return property(USE_CACHE);
	}
	
	/**
	 * @see ArtifactStoreBuilder#useCache(boolean)
	 */
	@Deprecated
	public ArtifactStoreBuilder cache(boolean cache) {
		return useCache(cache);
	}

	public ArtifactStoreBuilder downloader(IDownloader downloader) {
		set(DOWNLOADER, downloader);
		return this;
	}

	protected IProperty<IDownloader> downloader() {
		return property(DOWNLOADER);
	}


	@Override
	public IArtifactStore build() {
		boolean useCache = get(USE_CACHE, true);

		logger.debug("Build ArtifactStore(useCache: {})", useCache);
		
		IArtifactStore artifactStore;

		artifactStore = new ArtifactStore(get(DOWNLOAD_CONFIG), get(TEMP_DIR_FACTORY), get(EXECUTABLE_NAMING), get(DOWNLOADER));

		if (useCache) {
			artifactStore = new CachingArtifactStore(artifactStore);
		}

		return artifactStore;
	}
}
