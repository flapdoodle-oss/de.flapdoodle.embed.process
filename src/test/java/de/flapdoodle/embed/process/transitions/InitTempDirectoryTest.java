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

import de.flapdoodle.embed.process.io.directories.TempDir;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TearDownException;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InitTempDirectoryTest {

	@Test
	void deleteTempDirectoryOnTearDown() {
		TempDir tempDir;

		try (TransitionWalker.ReachedState<TempDir> reachedState = Transitions.from(InitTempDirectory.withPlatformTempRandomSubDir())
			.walker().initState(StateID.of(TempDir.class))) {

			TempDir tempDirInScope = reachedState.current();
			assertThat(tempDirInScope.value()).exists()
				.isDirectory()
				.isEmptyDirectory();

			tempDir = tempDirInScope;
		}

		assertThat(tempDir.value()).doesNotExist();
	}

	@Test
	void dontDeleteTempDirectoryOnTearDown() throws IOException {
		TempDir tempDir;

		try (TransitionWalker.ReachedState<TempDir> reachedState = Transitions.from(InitTempDirectory.withPlatformTempRandomSubDir().withDeleteIfEmpty(false))
			.walker().initState(StateID.of(TempDir.class))) {

			TempDir tempDirInScope = reachedState.current();
			assertThat(tempDirInScope.value()).exists()
				.isDirectory()
				.isEmptyDirectory();

			tempDir = tempDirInScope;
		}

		assertThat(tempDir.value()).exists()
			.isDirectory()
			.isEmptyDirectory();

		de.flapdoodle.embed.process.io.Files.deleteAll(tempDir.value());
	}

	@Test
	void failIfDirectoryIsNotEmpty() throws IOException {
		AtomicReference<TempDir> tempDir = new AtomicReference<>();

		assertThatThrownBy(() -> {
			try (TransitionWalker.ReachedState<TempDir> reachedState = Transitions.from(InitTempDirectory.withPlatformTempRandomSubDir())
				.walker().initState(StateID.of(TempDir.class))) {

				TempDir tempDirInScope = reachedState.current();
				assertThat(tempDirInScope.value()).exists()
					.isDirectory()
					.isEmptyDirectory();

				Files.write(tempDirInScope.value().resolve("file"), new byte[10]);

				tempDir.set(tempDirInScope);
			}
		}).isInstanceOf(TearDownException.class)
			.hasCauseInstanceOf(IllegalArgumentException.class)
			.cause()
			.hasMessageContaining("is not empty, (forceDelete=false)");

		assertThat(tempDir.get().value()).exists()
			.isDirectory()
			.isNotEmptyDirectory();

		de.flapdoodle.embed.process.io.Files.deleteAll(tempDir.get().value());
	}

	@Test
	void forceDeleteDirectoryEvenIfItIsNotEmpty() throws IOException {
		AtomicReference<TempDir> tempDir = new AtomicReference<>();

		try (TransitionWalker.ReachedState<TempDir> reachedState = Transitions.from(InitTempDirectory.withPlatformTempRandomSubDir().withForceDelete(true))
			.walker().initState(StateID.of(TempDir.class))) {

			TempDir tempDirInScope = reachedState.current();
			assertThat(tempDirInScope.value()).exists()
				.isDirectory()
				.isEmptyDirectory();

			Files.write(tempDirInScope.value().resolve("file"), new byte[10]);

			tempDir.set(tempDirInScope);
		}

		assertThat(tempDir.get().value()).doesNotExist();
	}
}