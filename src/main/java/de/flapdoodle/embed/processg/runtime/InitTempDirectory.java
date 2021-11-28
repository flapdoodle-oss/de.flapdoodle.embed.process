package de.flapdoodle.embed.processg.runtime;

import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

@Value.Immutable
public abstract class InitTempDirectory implements Transition<TempDirectory>, HasLabel {

	@Override public String transitionLabel() {
		return "InitTempDirectory";
	}

	@Value.Default
	protected Path tempDirectory() {
		return Paths.get(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public StateID<TempDirectory> destination() {
		return StateID.of(TempDirectory.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return Collections.emptySet();
	}

	@Override
	public State<TempDirectory> result(StateLookup lookup) {
		return State.of(TempDirectory.of(tempDirectory()));
	}

	public static ImmutableInitTempDirectory withPlatformTemp() {
		return builder().build();
	}

	public static ImmutableInitTempDirectory.Builder builder() {
		return ImmutableInitTempDirectory.builder();
	}

	public static ImmutableInitTempDirectory with(Path path) {
		return builder().tempDirectory(path).build();
	}
}
