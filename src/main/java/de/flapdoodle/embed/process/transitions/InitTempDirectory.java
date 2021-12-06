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

import de.flapdoodle.embed.process.types.TempDirectory;
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
	@Deprecated
	/**
	 * @see de.flapdoodle.embed.process.nio.directories.TempDir
	 */
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
