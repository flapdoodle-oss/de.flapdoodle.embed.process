package de.flapdoodle.embed.process.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

public class StaticArtifactStore implements IArtifactStore {

	private Map<Distribution, IExtractedFileSet> distributionFileSet;

	public StaticArtifactStore(Map<Distribution, IExtractedFileSet> distributionFileSet) {
		this.distributionFileSet = new HashMap<Distribution, IExtractedFileSet>(distributionFileSet);
	}
	
	@Override
	public boolean checkDistribution(Distribution distribution)
			throws IOException {
		return distributionFileSet.containsKey(distribution);
	}

	@Override
	public IExtractedFileSet extractFileSet(Distribution distribution)
			throws IOException {
		return distributionFileSet.get(distribution);
	}

	@Override
	public void removeFileSet(Distribution distribution, IExtractedFileSet files) {
		// dont remove any files
	}

}
