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

import java.util.Map;
import java.util.logging.Logger;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.ILibraryStore;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;

public class ArtifactStoreBuilder extends AbstractBuilder<IArtifactStore> {
	private static Logger logger = Logger.getLogger(ArtifactStoreBuilder.class.getName());

	private static final TypedProperty<ITempNaming> EXECUTABLE_NAMING = TypedProperty.with("ExecutableNaming",ITempNaming.class);
	private static final TypedProperty<IDirectory> TEMP_DIR_FACTORY = TypedProperty.with("TempDir",IDirectory.class);
	private static final TypedProperty<IDownloadConfig> DOWNLOAD_CONFIG = TypedProperty.with("DownloadConfig",IDownloadConfig.class);
	private static final TypedProperty<Boolean> USE_CACHE = TypedProperty.with("UseCache",Boolean.class);
	private static final TypedProperty<ILibraryStore> LIBRARIES = TypedProperty.with("Libraries", ILibraryStore.class);
	
	public ArtifactStoreBuilder download(AbstractBuilder<IDownloadConfig> downloadConfigBuilder) {
		return download(downloadConfigBuilder.build());
	}
	
	public ArtifactStoreBuilder download(IDownloadConfig downloadConfig) {
		set(DOWNLOAD_CONFIG, downloadConfig);
		return this;
	}

	protected IProperty<IDownloadConfig> download() {
		return property(DOWNLOAD_CONFIG);
	}

	public ArtifactStoreBuilder tempDir(IDirectory tempDirFactory) {
		set(TEMP_DIR_FACTORY, tempDirFactory);
		return this;
	}
	
	protected IProperty<IDirectory> tempDir() {
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
	
	public ArtifactStoreBuilder libraries(ILibraryStore libraries) {
		set(LIBRARIES, libraries);
		return this;
	}
	
	protected IProperty<ILibraryStore> libraries() {
		return property(LIBRARIES);
	}

	
	@Override
	public IArtifactStore build() {
		boolean useCache = get(USE_CACHE, true);
		
		logger.fine("Build ArtifactStore(useCache:"+useCache+")");
		
		IArtifactStore artifactStore = new ArtifactStore(get(DOWNLOAD_CONFIG),get(TEMP_DIR_FACTORY), get(EXECUTABLE_NAMING), get(LIBRARIES, null));
		if (useCache) {
			artifactStore=new CachingArtifactStore(artifactStore);
		}
		return artifactStore;
	}
}
