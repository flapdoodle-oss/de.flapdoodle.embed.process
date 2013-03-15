package de.flapdoodle.embed.process.store;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
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
	
	public ArtifactStore(IDownloadConfig downloadConfig,IDirectory tempDireFactory,ITempNaming executableNaming) {
		_downloadConfig=downloadConfig;
		_tempDireFactory = tempDireFactory;
		_executableNaming = executableNaming;
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
		return exe;
	}

	@Override
	public void removeExecutable(Distribution distribution, File executable) {
		if (executable.exists() && !Files.forceDelete(executable))
			logger.warning("Could not delete executable NOW: " + executable);
	}
}
