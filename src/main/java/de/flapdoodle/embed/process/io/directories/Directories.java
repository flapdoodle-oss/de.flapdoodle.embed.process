package de.flapdoodle.embed.process.io.directories;

import java.io.File;

import de.flapdoodle.embed.process.io.file.Files;

public abstract class Directories {

	private Directories() {
		// no instance
	}
	
	public static IDirectory join(final IDirectory left, final IDirectory right) {
		return new IDirectory() {
			
			@Override
			public boolean isGenerated() {
				return left.isGenerated() || right.isGenerated();
			}
			
			@Override
			public File asFile() {
				return Files.fileOf(left.asFile(), right.asFile());
			}
		};
	}
}
