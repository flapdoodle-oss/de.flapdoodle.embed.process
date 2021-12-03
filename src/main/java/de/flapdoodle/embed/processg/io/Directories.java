package de.flapdoodle.embed.processg.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public abstract class Directories {
	private Directories() {
		// no instance
	}

	public static void deleteAll(Path rootPath) throws IOException {
		try (Stream<Path> walk = Files.walk(rootPath)) {
			walk.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
	}
}
