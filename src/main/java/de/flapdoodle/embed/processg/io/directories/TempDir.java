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
package de.flapdoodle.embed.processg.io.directories;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.types.Wrapper;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Supplier;

@Value.Immutable
public abstract class TempDir extends Wrapper<Path> {

	public static TempDir of(Path path) {
		return ImmutableTempDir.of(path);
	}

	private static Path platform() {
		return Paths.get(System.getProperty("java.io.tmpdir"));
	}

	private static Path propertyOrPlatformTemp() {
		String custom = System.getProperty("de.flapdoodle.embed.io.tmpdir");
		return custom != null
			? Paths.get(custom)
			: platform();
	}

	private static Path createDirectoryIfNotExist(Path path) {
		Try.run(() -> {
			if (!Files.exists(path)) {
				Files.createDirectory(path);
			}
		});
		Preconditions.checkArgument(Files.isDirectory(path),"%s is not a directory",path);
		return path;
	}

	public static Supplier<TempDir> platformTempDir() {
		return () -> of(platform());
	}

	public static Supplier<TempDir> propertyOrPlatformTempDir() {
		return () -> {
			return of(propertyOrPlatformTemp());
		};
	}

	public static Supplier<TempDir> platformTempSubDir(Naming naming) {
		return () -> of(createDirectoryIfNotExist(platform()
			.resolve(naming.nameFor("temp-",""))));
	}

	public static Supplier<TempDir> propertyOrPlatformTempSubDir(Naming naming) {
		return () -> of(createDirectoryIfNotExist(propertyOrPlatformTemp()
			.resolve(naming.nameFor("temp-",""))));
	}
}
