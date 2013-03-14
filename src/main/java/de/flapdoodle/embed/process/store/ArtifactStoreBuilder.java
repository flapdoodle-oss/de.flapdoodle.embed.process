package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;

public class ArtifactStoreBuilder extends AbstractBuilder<IArtifactStore> {

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
	
	@Override
	public IArtifactStore build() {
		return new ArtifactStore(get(IDownloadConfig.class),get(IDirectory.class), get(ITempNaming.class));
	}
}
