package de.flapdoodle.embed.process.extract;

import de.flapdoodle.embed.process.io.directories.IDirectory;

public class DirectoryAndExecutableNaming {

	private final IDirectory directory;
	private final ITempNaming executableNaming;

	public DirectoryAndExecutableNaming(IDirectory directory, ITempNaming executableNaming) {
		this.directory = directory;
		this.executableNaming = executableNaming;
	}
	
	public IDirectory getDirectory() {
		return directory;
	}
	
	public ITempNaming getExecutableNaming() {
		return executableNaming;
	}
}
