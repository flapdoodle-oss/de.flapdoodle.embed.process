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
package de.flapdoodle.embed.process.io.directories;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.types.Wrapper;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;

@Value.Immutable
public abstract class PersistentDir extends Wrapper<Path> {
	private static Logger logger= LoggerFactory.getLogger(PersistentDir.class);

	@Value.Check
	protected void check() {
		Preconditions.checkArgument(Files.exists(value()),"does not exist: %s", value());
		Preconditions.checkArgument(Files.isDirectory(value()),"is not a directory: %s", value());
	}

	public static PersistentDir of(Path path) {
		return ImmutablePersistentDir.of(path);
	}

	public static Path userHome() {
		return userHome(System::getProperty);
	}

	public static ThrowingSupplier<PersistentDir, IOException> inUserHome(String subDir) {
		return inUserHome(System::getProperty, subDir);
	}

	public static ThrowingSupplier<PersistentDir, IOException> inWorkingDir(String subDir) {
		return relativeTo(Paths.get(""), subDir);
	}

	/**
	 * @see PersistentDir#inUserHome(String)
	 */
	@Deprecated
	public static Supplier<PersistentDir> userHome(String subDir) {
		return inUserHome(subDir).mapToUncheckedException(RuntimeException::new);
	}

	// VisibleForTesting
	static ThrowingSupplier<PersistentDir, IOException> relativeTo(Path base, String subDir) {
		return () -> {
			Path current = base.resolve(subDir);
			if (!Files.exists(current)) {
				Files.createDirectory(current);
			}
			return PersistentDir.of(current);
		};
	}

	// VisibleForTesting
	static ThrowingSupplier<PersistentDir, IOException> inUserHome(Function<String, String> systemGetProperty, String subDir) {
		return () -> {
			Path resolved = userHome(systemGetProperty).resolve(subDir);
			if (!Files.exists(resolved)) Files.createDirectory(resolved);
			return of(resolved);
		};
	}

	// VisibleForTesting
	static Path userHome(Function<String, String> systemGetProperty) {
		String userHome = Preconditions.checkNotNull(systemGetProperty.apply("user.home"), "user.home is null");
		if ("?".equals(userHome)) {
			logger.warn("user.home is set to '?', maybe this is running inside a docker container");
			logger.warn("use fallback to user.dir");
			String userDir = Preconditions.checkNotNull(systemGetProperty.apply("user.dir"),"user.dir is null");
			Preconditions.checkArgument(!userDir.equals("?"),"user.dir is set to '?'");
			logger.warn("use user.dir('{}') as fallback for user.home('{}')",userDir, userHome);
			userHome = userDir;
		}
		Path path = Paths.get(userHome);
		Preconditions.checkArgument(Files.isDirectory(path),"%s is not a directory", path);
		return path;
	}
}
