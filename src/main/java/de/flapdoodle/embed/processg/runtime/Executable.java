package de.flapdoodle.embed.processg.runtime;

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
	public Set<StateID<?>> sources() {
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
