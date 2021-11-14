package de.flapdoodle.embed.processg.runtime;

import org.immutables.value.Value;

@Value.Immutable
public interface RunningProcess {

		static ImmutableRunningProcess.Builder builder() {
				return ImmutableRunningProcess.builder();
		}
}
