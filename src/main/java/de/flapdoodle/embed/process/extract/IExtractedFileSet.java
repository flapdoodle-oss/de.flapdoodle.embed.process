package de.flapdoodle.embed.process.extract;

import java.io.File;
import java.util.List;

import de.flapdoodle.embed.process.config.store.FileType;


public interface IExtractedFileSet {

	File executable();
	
	List<File> files(FileType type);

}
