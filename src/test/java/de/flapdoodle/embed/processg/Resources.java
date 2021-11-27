package de.flapdoodle.embed.processg;

import de.flapdoodle.types.Try;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {
	public static Path resourcePath(Class<?> clazz, String resourceName) throws URISyntaxException {
		return Paths.get(clazz.getResource(resourceName).toURI());
	}
}
