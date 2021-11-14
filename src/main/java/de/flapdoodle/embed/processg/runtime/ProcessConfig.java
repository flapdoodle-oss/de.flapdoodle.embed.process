package de.flapdoodle.embed.processg.runtime;

import org.immutables.value.Value;

import java.util.OptionalLong;

@Value.Immutable
public interface ProcessConfig {
		@Value.Default
		default boolean daemonProcess() {
				return false;
		}

		@Value.Default
		default long stopTimeoutInMillis() {
				return 5000;
		}

		static ImmutableProcessConfig defaults() {
				return ImmutableProcessConfig.builder().build();
		}
}
