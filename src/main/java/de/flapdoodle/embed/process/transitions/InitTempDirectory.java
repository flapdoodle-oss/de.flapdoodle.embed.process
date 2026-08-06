/*
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

import de.flapdoodle.commons.checks.Preconditions;
import de.flapdoodle.embed.process.io.Files;
import de.flapdoodle.embed.process.io.directories.TempDir;
import de.flapdoodle.embed.process.io.directories.UUIDNaming;
import de.flapdoodle.commons.reverse.State;
import de.flapdoodle.commons.reverse.StateID;
import de.flapdoodle.commons.reverse.StateLookup;
import de.flapdoodle.commons.reverse.Transition;
import de.flapdoodle.commons.reverse.naming.HasLabel;
import de.flapdoodle.commons.types.Try;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

@Value.Immutable
public abstract class InitTempDirectory implements Transition<TempDir>, HasLabel {

	@Override public String transitionLabel() {
		return "InitTempDirectory";
	}

	@Value.Default
	protected Supplier<TempDir> tempDirFactory() {
		return () -> TempDir.platformTempDir().get();
	}

	@Value.Default
	protected boolean deleteIfEmpty() {
		return false;
	}

	@Value.Default
	protected boolean forceDelete() {
		return false;
	}

	@Override
	public StateID<TempDir> destination() {
		return StateID.of(TempDir.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return Collections.emptySet();
	}

	@Override
	public State<TempDir> result(StateLookup lookup) {
		return State.of(tempDirFactory().get(), it -> {
			Try.run(() -> {
				if (forceDelete()) {
					Files.deleteAll(it.value());
				} else {
					if (deleteIfEmpty()) {
						Preconditions.checkArgument(Files.isEmptyDirectory(it.value()), "temp directory %s is not empty, (forceDelete=%s)", it.value(), forceDelete());
						Files.deleteAll(it.value());
					}
				}
			});
		});
	}

	public static ImmutableInitTempDirectory withPlatformTemp() {
		return builder().build();
	}

	public static ImmutableInitTempDirectory.Builder builder() {
		return ImmutableInitTempDirectory.builder();
	}

	public static ImmutableInitTempDirectory with(Path path) {
		return builder()
			.tempDirFactory(() -> TempDir.of(path))
			.build();
	}

	public static ImmutableInitTempDirectory withPlatformTempRandomSubDir() {
		return builder()
			.tempDirFactory(TempDir.platformTempSubDir(new UUIDNaming()))
			.deleteIfEmpty(true)
			.build();
	}
}
