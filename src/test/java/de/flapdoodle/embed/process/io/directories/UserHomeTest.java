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
package de.flapdoodle.embed.process.io.directories;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class UserHomeTest {
	@Test
	void justUserHomeIfValidAndSet(@TempDir Path tempDir) {
		Function<String, String> systemGetProperty=key -> {
			switch (key) {
				case "user.home":
					return tempDir.toString();
				default:
					throw new IllegalArgumentException("should not happen");
			}
		};
		Path userHome = UserHome.userHome(systemGetProperty);

		assertThat(userHome)
			.isEqualTo(tempDir);
	}

	@Test
	void userUserDirAsFallbackIfUserHomeIsSetToQuestionMark(@TempDir Path tempDir) {
		Function<String, String> systemGetProperty=key -> {
			switch (key) {
				case "user.home":
					return "?";
				case "user.dir":
					return tempDir.toString();
				default:
					throw new IllegalArgumentException("should not happen");
			}
		};
		Path userHome = UserHome.userHome(systemGetProperty);

		assertThat(userHome)
			.isEqualTo(tempDir);
	}

	@Test
	void failIfUserHomeAndUserDirIsSetToQuestionMark() {
		Function<String, String> systemGetProperty=key -> {
			switch (key) {
				case "user.home":
				case "user.dir":
					return "?";
				default:
					throw new IllegalArgumentException("should not happen");
			}
		};
		Assertions.assertThatThrownBy(() ->  UserHome.userHome(systemGetProperty))
			.isInstanceOf(IllegalArgumentException.class);
	}

}