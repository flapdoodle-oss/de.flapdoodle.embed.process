package de.flapdoodle.embed.processg.store;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.processg.extract.ArchiveType;
import de.flapdoodle.os.Architecture;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

public class LocalArchiveStore implements ArchiveStore {

	private final Path baseDir;

	public LocalArchiveStore(Path baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public Optional<Path> archiveFor(String name, Distribution distribution, ArchiveType archiveType) {
		Path arcFile = resolve(baseDir.resolve(name), distribution, archiveType);
		return Files.isReadable(arcFile)
			? Optional.of(arcFile)
			: Optional.empty();
	}
	
	@Override
	public Path store(String name, Distribution distribution, ArchiveType archiveType, Path archive) throws IOException {
		Path arcFile = resolve(baseDir.resolve(name), distribution, archiveType);
		Path arcDirectory = arcFile.getParent();
		Preconditions.checkArgument(arcDirectory!=null,"no parent directory for %s",arcFile);
		if (!Files.exists(arcDirectory)) {
			Files.createDirectories(arcDirectory);
		}
		Preconditions.checkArgument(!Files.exists(arcFile),"archive for %s:%s already exists (%s)",distribution,archiveType, arcFile);
		return Files.copy(archive, arcFile, StandardCopyOption.COPY_ATTRIBUTES);
	}

	private static Path resolve(Path base, Distribution distribution, ArchiveType archiveType) {
		return resolve(base, distribution.platform()).resolve(asPath(distribution.version(), archiveType));
	}

	private static Path resolve(Path base, Platform platform) {
		return base.resolve(asPath(platform.operatingSystem())).resolve(asPath(platform.architecture(), platform.distribution(), platform.version()));
	}
	private static String asPath(Architecture architecture, Optional<de.flapdoodle.os.Distribution> distribution, Optional<de.flapdoodle.os.Version> version) {
		return asPath(architecture)+distribution.map(de.flapdoodle.os.Distribution::name).orElse("-");
	}

	private static String asPath(Architecture architecture) {
		String arch;
		switch (architecture.cpuType()) {
			case X86:
				arch="x86";
				break;
			case ARM:
				arch="arm";
				break;
			default:
				throw new IllegalArgumentException("Unknown cpyType: "+architecture.cpuType());
		}

		String bits;
		switch (architecture.bitSize()) {
			case B32:
				bits="32";
				break;
			case B64:
				bits="64";
				break;
			default:
				throw new IllegalArgumentException("Unknown bitsize: "+architecture.bitSize());
		}

		return arch+bits;
	}

	private static String asPath(OS operatingSystem) {
		switch (operatingSystem){
			case Windows: return "win";
			case Linux: return "linux";
			case OS_X: return "osx";
			case FreeBSD:return "freebsd";
			case Solaris:return "solaris";
		}
		throw new IllegalArgumentException("Unknown os: "+operatingSystem);
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
