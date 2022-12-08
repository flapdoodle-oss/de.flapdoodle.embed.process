package de.flapdoodle.embed.process.io.directories;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentDirTest {

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
		Path userHome = PersistentDir.userHome(systemGetProperty);

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
		Path userHome = PersistentDir.userHome(systemGetProperty);

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
		Assertions.assertThatThrownBy(() ->  PersistentDir.userHome(systemGetProperty))
			.isInstanceOf(IllegalArgumentException.class);
	}
}