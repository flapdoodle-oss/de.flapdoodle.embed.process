package de.flapdoodle.embed.process.config.store;

import org.immutables.value.Value;
import org.immutables.value.Value.Parameter;

import de.flapdoodle.embed.process.distribution.ArchiveType;

@Value.Immutable
public interface DistributionPackage {
	
	@Parameter
	ArchiveType archiveType();
	
	@Parameter
	FileSet fileSet();
	
	@Parameter
	String archivePath();
	
	public static DistributionPackage of(ArchiveType archiveType, FileSet fileSet, String path) {
		return ImmutableDistributionPackage.of(archiveType, fileSet, path);
	}
}
