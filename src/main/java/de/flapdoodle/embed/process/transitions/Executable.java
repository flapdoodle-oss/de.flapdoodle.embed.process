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
package de.flapdoodle.embed.process.transitions;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;
import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Deprecated
public abstract class Executable implements Transition<ExtractedFileSet> {
	private static Logger logger = LoggerFactory.getLogger(Starter.class);

	protected abstract IArtifactStore artifactStore();

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	protected StateID<SupportConfig> supportConfig() {
		return StateID.of(SupportConfig.class);
	}

	@Override
	@Value.Default
	public StateID<ExtractedFileSet> destination() {
		return StateID.of(ExtractedFileSet.class);
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(supportConfig(), distribution());
	}

	@Override
	public State<ExtractedFileSet> result(StateLookup lookup) {
		Distribution distribution=lookup.of(distribution());
		SupportConfig supportConfig=lookup.of(supportConfig());

		try {
			IArtifactStore artifactStore = artifactStore();

			Optional<ExtractedFileSet> files = artifactStore.extractFileSet(distribution);
			if (files.isPresent()) {
				return State.of(
					files.get(),
					fileSet -> artifactStore.removeFileSet(distribution, fileSet)
				);
			} else {
				throw new DistributionException("could not find Distribution",distribution);
			}
		} catch (IOException iox) {
			String messageOnException = supportConfig.messageOnException().apply(getClass(), iox);
			if (messageOnException==null) {
				messageOnException="prepare executable";
			}
			logger.error(messageOnException, iox);
			throw new DistributionException(messageOnException, distribution,iox);
		}
	}

	public static ImmutableExecutable with(IArtifactStore artifactStore) {
		return ImmutableExecutable.builder().artifactStore(artifactStore).build();
	}
}
