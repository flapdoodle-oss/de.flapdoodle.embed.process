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

import de.flapdoodle.embed.process.io.Files;
import de.flapdoodle.reverse.State;
import de.flapdoodle.types.ThrowingFunction;
import de.flapdoodle.types.Try;

import java.nio.file.Path;
import java.util.function.Function;

public abstract class Directories {
	private Directories() {
		// no instance
	}

	public static <T, S> Function<S, State<T>> deleteOnTearDown(
		Function<S, Path> newPath,
		Function<Path, T> newInstance
	) {
		return s -> {
			Path path = newPath.apply(s);
			return State.of(newInstance.apply(path), it -> Try.run(() -> Files.deleteAll(path)));
		};
	}
}
