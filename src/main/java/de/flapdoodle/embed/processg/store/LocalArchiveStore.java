package de.flapdoodle.embed.processg.store;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.processg.extract.ArchiveType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class LocalArchiveStore implements ArchiveStore {

	private final Path baseDir;
	private final PlatformPathResolver platformPathResolver;

	public LocalArchiveStore(Path baseDir, PlatformPathResolver platformPathResolver) {
		this.baseDir = baseDir;
		this.platformPathResolver = platformPathResolver;
	}

	public LocalArchiveStore(Path baseDir) {
		this(baseDir, PlatformPathResolver.withOperatingSystemAsDirectory());
	}

	@Override
	public Optional<Path> archiveFor(String name, Distribution distribution, ArchiveType archiveType) {
		Path arcFile = resolve(baseDir, name, distribution, archiveType);
		return Files.isReadable(arcFile)
			? Optional.of(arcFile)
			: Optional.empty();
	}
	
	@Override
	public Path store(String name, Distribution distribution, ArchiveType archiveType, Path archive) throws IOException {
		Path arcFile = resolve(baseDir, name, distribution, archiveType);
		Path arcDirectory = arcFile.getParent();
		Preconditions.checkArgument(arcDirectory!=null,"no parent directory for %s",arcFile);
		if (!Files.exists(arcDirectory)) {
			Files.createDirectories(arcDirectory);
		}
		Preconditions.checkArgument(!Files.exists(arcFile),"archive for %s:%s already exists (%s)",distribution,archiveType, arcFile);
		return Files.copy(archive, arcFile, StandardCopyOption.COPY_ATTRIBUTES);
	}

	private Path resolve(Path base, String name, Distribution distribution, ArchiveType archiveType) {
		return platformPathResolver.resolve(base.resolve(name), distribution.platform()).resolve(asPath(distribution.version(), archiveType));
	}

	private static String asPath(Version version, ArchiveType archiveType) {
		return version.asInDownloadPath()+"."+asExtension(archiveType);
	}

	private static String asExtension(ArchiveType archiveType) {
		switch (archiveType) {
			case ZIP: return "zip";
			case TGZ: return "tgz";
			case TBZ2: return "tbz2";
			case TXZ: return "txz";
			case EXE: return "exe";
		}
		throw new IllegalArgumentException("Unknown archiveType: "+archiveType);
	}

}
