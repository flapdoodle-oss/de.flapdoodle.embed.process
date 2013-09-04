package de.flapdoodle.embed.process.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.flapdoodle.embed.process.config.store.FileType;

public interface IExtractionMatch {

	File write(InputStream source, long size) throws IOException;

	FileType type();

}
