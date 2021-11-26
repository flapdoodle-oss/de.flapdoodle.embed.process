package de.flapdoodle.embed.processg.store;

import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.processg.extract.ArchiveType;
import de.flapdoodle.os.Architecture;
import de.flapdoodle.os.Distribution;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;

import java.nio.file.Path;
import java.util.Optional;

public class OsAsDirectoryPlatformPathResolver implements PlatformPathResolver {

	@Override
	public Path resolve(Path base, Platform platform) {
		return base.resolve(asPath(platform.operatingSystem())).resolve(asPath(platform.architecture(), platform.distribution(), platform.version()));
	}

	private static String asPath(Architecture architecture, Optional<Distribution> distribution, Optional<de.flapdoodle.os.Version> version) {
		return asPath(architecture) +
			"--" +
			distribution
				.map(de.flapdoodle.os.Distribution::name)
				.map(OsAsDirectoryPlatformPathResolver::asPath)
				.orElse("") +
			"--"+
			version.map(de.flapdoodle.os.Version::name)
				.map(OsAsDirectoryPlatformPathResolver::asPath)
				.orElse("");
	}

	private static String asPath(String src) {
		return src
			.replace("/","--")
			.replace("'","--")
			.replace("\\","--");
	}

	private static String asPath(Architecture architecture) {
		String arch;
		switch (architecture.cpuType()) {
			case X86:
				arch = "x86";
				break;
			case ARM:
				arch = "arm";
				break;
			default:
				throw new IllegalArgumentException("Unknown cpyType: " + architecture.cpuType());
		}

		String bits;
		switch (architecture.bitSize()) {
			case B32:
				bits = "32";
				break;
			case B64:
				bits = "64";
				break;
			default:
				throw new IllegalArgumentException("Unknown bitsize: " + architecture.bitSize());
		}

		return arch + "-" + bits;
	}

	private static String asPath(OS operatingSystem) {
		switch (operatingSystem) {
			case Windows:
				return "win";
			case Linux:
				return "linux";
			case OS_X:
				return "osx";
			case FreeBSD:
				return "freebsd";
			case Solaris:
				return "solaris";
		}
		throw new IllegalArgumentException("Unknown os: " + operatingSystem);
	}
}
