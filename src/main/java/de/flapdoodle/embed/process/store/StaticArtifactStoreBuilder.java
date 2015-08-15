package de.flapdoodle.embed.process.store;

import java.util.LinkedHashMap;
import java.util.Map;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

public class StaticArtifactStoreBuilder extends AbstractBuilder<IArtifactStore> {

	Map<Distribution, IExtractedFileSet> distributionFileSet=new LinkedHashMap<Distribution, IExtractedFileSet>();
	
	public StaticArtifactStoreBuilder fileSet(Distribution distribution, IExtractedFileSet fileSet) {
		IExtractedFileSet old = distributionFileSet.put(distribution, fileSet);
		if (old != null) {
			throw new RuntimeException("" + distribution + " already set to " + old);
		}
		return this;
	}
	
	@Override
	public IArtifactStore build() {
		return new StaticArtifactStore(distributionFileSet);
	}

}
