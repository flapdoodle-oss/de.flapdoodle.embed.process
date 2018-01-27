/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;

public class StaticArtifactStore implements IArtifactStore {

	private final Map<Distribution, ExtractedFileSet> distributionFileSet;

	public StaticArtifactStore(Map<Distribution, ExtractedFileSet> distributionFileSet) {
		this.distributionFileSet = new HashMap<>(distributionFileSet);
	}
	
	@Override
	public boolean checkDistribution(Distribution distribution) {
		return distributionFileSet.containsKey(distribution);
	}

	@Override
	public ExtractedFileSet extractFileSet(Distribution distribution) {
		return distributionFileSet.get(distribution);
	}

	@Override
	public void removeFileSet(Distribution distribution, ExtractedFileSet files) {
		// dont remove any files
	}

}
