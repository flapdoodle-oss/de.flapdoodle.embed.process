package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.config.store.PackageResolver;
import de.flapdoodle.embed.processg.runtime.ImmutableExecutable;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class PackageOfDistribution implements Transition<Package> {
	protected abstract PackageResolver packageResolver();

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Override
	@Value.Default
	public StateID<Package> destination() {
		return StateID.of(Package.class);
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(distribution());
	}

	@Override
	public State<Package> result(StateLookup lookup) {
		return State.of(packageResolver().packageFor(lookup.of(distribution())));
	}

	public static ImmutablePackageOfDistribution with(PackageResolver packageResolver) {
		return ImmutablePackageOfDistribution.builder().packageResolver(packageResolver).build();
	}
}
