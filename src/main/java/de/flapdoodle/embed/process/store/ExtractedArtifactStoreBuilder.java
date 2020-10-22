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

import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.TempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;


@Deprecated
public class ExtractedArtifactStoreBuilder extends ArtifactStoreBuilder {
	
	protected static final TypedProperty<Directory> EXTRACT_DIR_FACTORY = TypedProperty.with("ExtractDir",Directory.class);
	protected static final TypedProperty<TempNaming> EXTRACT_EXECUTABLE_NAMING = TypedProperty.with("ExtractExecutableNaming",TempNaming.class);

	@Override
	public ExtractedArtifactStoreBuilder tempDir(Directory tempDirFactory) {
		super.tempDir(tempDirFactory);
		return this;
	}
	
	@Override
	@Deprecated
	public ArtifactStoreBuilder useCache(boolean cache) {
		throw new RuntimeException("no need to cache anything");
	}
	
	@Override
	public ExtractedArtifactStoreBuilder download(DownloadConfig downloadConfig) {
		super.download(downloadConfig);
		return this;
	}
	
	@Override
	public ExtractedArtifactStoreBuilder downloader(Downloader downloader) {
		super.downloader(downloader);
		return this;
	}
	
	@Override
	public ExtractedArtifactStoreBuilder executableNaming(TempNaming execNaming) {
		super.executableNaming(execNaming);
		return this;
	}
	
	public ExtractedArtifactStoreBuilder extractExecutableNaming(TempNaming execNaming) {
		set(EXTRACT_EXECUTABLE_NAMING, execNaming);
		return this;
	}
	
	public ExtractedArtifactStoreBuilder extractDir(Directory tempDirFactory) {
		set(EXTRACT_DIR_FACTORY, tempDirFactory);
		return this;
	}
	
	protected IProperty<Directory> extractDir() {
		return property(EXTRACT_DIR_FACTORY);
	}
	
	protected IProperty<TempNaming> extractExecutableNaming() {
		return property(EXTRACT_EXECUTABLE_NAMING);
	}
	

	@Override
	public IArtifactStore build() {
		DirectoryAndExecutableNaming extract = DirectoryAndExecutableNaming.of(get(EXTRACT_DIR_FACTORY),get(EXTRACT_EXECUTABLE_NAMING));
		DirectoryAndExecutableNaming temp = DirectoryAndExecutableNaming.of(tempDir().get(),executableNaming().get());
		return ExtractedArtifactStore.of(get(DOWNLOAD_CONFIG), get(DOWNLOADER),extract,temp);
	}
}
