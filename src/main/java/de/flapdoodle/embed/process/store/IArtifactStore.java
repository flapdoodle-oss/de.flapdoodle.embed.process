package de.flapdoodle.embed.process.store;

import java.io.File;
import java.io.IOException;

import de.flapdoodle.embed.process.distribution.Distribution;


public interface IArtifactStore {

	boolean checkDistribution(Distribution distribution) throws IOException;

	File extractExe(Distribution distribution) throws IOException;

	void removeExecutable(Distribution distribution, File executable);
}
