package de.flapdoodle.embed.processg.store;

import de.flapdoodle.os.Platform;

import java.nio.file.Path;

public interface PlatformPathResolver {
	Path resolve(Path base, Platform platform);

	static PlatformPathResolver withOperatingSystemAsDirectory() {
		return new OsAsDirectoryPlatformPathResolver();
	}
}
