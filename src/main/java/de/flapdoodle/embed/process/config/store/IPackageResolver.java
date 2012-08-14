package de.flapdoodle.embed.process.config.store;

import java.util.regex.Pattern;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;


public interface IPackageResolver {

	Pattern executeablePattern(Distribution distribution);

	String executableFilename(Distribution distribution);

	ArchiveType getArchiveType(Distribution distribution);

	String getPath(Distribution distribution);

}
