package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;


public class ExtractedArtifactStoreBuilder extends ArtifactStoreBuilder {
	
	protected static final TypedProperty<IDirectory> EXTRACT_DIR_FACTORY = TypedProperty.with("ExtractDir",IDirectory.class);
	
	@Override
	@Deprecated
	public ArtifactStoreBuilder tempDir(IDirectory tempDirFactory) {
		throw new RuntimeException("tempDir not used, use extractDir");
	}
	
	@Override
	@Deprecated
	public ArtifactStoreBuilder useCache(boolean cache) {
		throw new RuntimeException("no need to cache anything");
	}
	
	@Override
	public ExtractedArtifactStoreBuilder download(IDownloadConfig downloadConfig) {
		super.download(downloadConfig);
		return this;
	}
	
	@Override
	public ExtractedArtifactStoreBuilder downloader(IDownloader downloader) {
		super.downloader(downloader);
		return this;
	}
	
	@Override
	public ExtractedArtifactStoreBuilder executableNaming(ITempNaming execNaming) {
		super.executableNaming(execNaming);
		return this;
	}
	
	public ArtifactStoreBuilder extractDir(IDirectory tempDirFactory) {
		set(EXTRACT_DIR_FACTORY, tempDirFactory);
		return this;
	}
	
	protected IProperty<IDirectory> extractDir() {
		return property(EXTRACT_DIR_FACTORY);
	}

	@Override
	public IArtifactStore build() {
		return new ExtractedArtifactStore(get(DOWNLOAD_CONFIG), get(DOWNLOADER),get(EXTRACT_DIR_FACTORY),get(EXECUTABLE_NAMING));
	}
}
