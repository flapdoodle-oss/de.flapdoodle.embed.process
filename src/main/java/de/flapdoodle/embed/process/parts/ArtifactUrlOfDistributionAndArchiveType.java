package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;

import java.util.Set;

public interface ArtifactUrlOfDistributionAndArchiveType extends Transition<ArtifactUrl> {
		StateID<BaseUrl> baseUrl();
		StateID<Distribution> distribution();
		StateID<ArchiveType> archiveType();

		@Override
		default Set<StateID<?>> sources() {
				return StateID.setOf(baseUrl(), distribution(), archiveType());
		}
}
