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
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class PackageOfDistribution implements Transition<Package>, HasLabel {
	@Override
	@Value.Default
	public String transitionLabel() {
		return "PackageOfDistribution";
	}

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
