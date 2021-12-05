package de.flapdoodle.embed.processg.io.directories;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class TempDirTest {

	@Test
	public void tempDirectoryIsPlatformTemp() {
		TempDir result = TempDir.platformTempDir().get();

		assertThat(result)
			.extracting(TempDir::value)
			.isEqualTo(Paths.get(System.getProperty("java.io.tmpdir")));
	}

	@Test
	public void parentOfSubDirIsPlatformTemp() {
		TempDir result = TempDir.platformTempSubDir(new UUIDNaming()).get();

		assertThat(result)
			.extracting(TempDir::value)
			.extracting(Path::getParent)
			.isEqualTo(Paths.get(System.getProperty("java.io.tmpdir")));
	}
}