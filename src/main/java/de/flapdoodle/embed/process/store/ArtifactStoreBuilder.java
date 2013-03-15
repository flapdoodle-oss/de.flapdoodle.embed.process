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

import java.util.logging.Logger;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;

public class ArtifactStoreBuilder extends AbstractBuilder<IArtifactStore> {
	private static Logger logger = Logger.getLogger(ArtifactStoreBuilder.class.getName());

	private static final String EXECUTABLE_NAMING = "ExecutableNaming";
	private static final String TEMP_DIR_FACTORY = "TempDir";
	private static final String DOWNLOAD_CONFIG = "DownloadConfig";
	
	public ArtifactStoreBuilder download(AbstractBuilder<IDownloadConfig> downloadConfigBuilder) {
		return download(downloadConfigBuilder.build());
	}
	
	public ArtifactStoreBuilder download(IDownloadConfig downloadConfig) {
		set(DOWNLOAD_CONFIG, IDownloadConfig.class, downloadConfig);
		return this;
	}
	
	public ArtifactStoreBuilder tempDir(IDirectory tempDirFactory) {
		set(TEMP_DIR_FACTORY, IDirectory.class, tempDirFactory);
		return this;
	}
	
	public ArtifactStoreBuilder executableNaming(ITempNaming execNaming) {
		set(EXECUTABLE_NAMING, ITempNaming.class, execNaming);
		return this;
	}
	
	public ArtifactStoreBuilder cache(boolean cache) {
		set("Cache", Boolean.class, cache);
		return this;
	}
	
	@Override
	public IArtifactStore build() {
		boolean useCache = get(Boolean.class,true);
		
		logger.severe("Build ArtifactStore(useCache:"+useCache+")");
		
		IArtifactStore artifactStore = new ArtifactStore(get(IDownloadConfig.class),get(IDirectory.class), get(ITempNaming.class));
		if (useCache) {
			artifactStore=new CachingArtifactStore(artifactStore);
		}
		return artifactStore;
	}
}
