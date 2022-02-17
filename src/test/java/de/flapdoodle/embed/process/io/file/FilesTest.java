package de.flapdoodle.embed.process.io.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FilesTest {

	@Test
	public void sameContentMustBeTrue(@TempDir Path tempDir) throws IOException {
		Path source = tempDir.resolve("source");
		Path dest = tempDir.resolve("dest");

		java.nio.file.Files.write(source,"TEST".getBytes(StandardCharsets.UTF_8));
		java.nio.file.Files.write(dest,"TEST".getBytes(StandardCharsets.UTF_8));

		assertThat(Files.sameContent(source, dest)).isTrue();
	}

	@Test
	public void sameContentIsFalseBCDifferentContent(@TempDir Path tempDir) throws IOException {
		Path source = tempDir.resolve("source");
		Path dest = tempDir.resolve("dest");

		java.nio.file.Files.write(source,"TESTA".getBytes(StandardCharsets.UTF_8));
		java.nio.file.Files.write(dest,"TESTB".getBytes(StandardCharsets.UTF_8));

		assertThat(Files.sameContent(source, dest)).isFalse();
	}

	@Test
	public void sameContentIsFalseBCSourceIsShorter(@TempDir Path tempDir) throws IOException {
		Path source = tempDir.resolve("source");
		Path dest = tempDir.resolve("dest");

		java.nio.file.Files.write(source,"TEST".getBytes(StandardCharsets.UTF_8));
		java.nio.file.Files.write(dest,"TESTA".getBytes(StandardCharsets.UTF_8));

		assertThat(Files.sameContent(source, dest)).isFalse();
	}

	@Test
	public void sameContentIsFalseBCDestIsShorter(@TempDir Path tempDir) throws IOException {
		Path source = tempDir.resolve("source");
		Path dest = tempDir.resolve("dest");

		java.nio.file.Files.write(source,"TESTA".getBytes(StandardCharsets.UTF_8));
		java.nio.file.Files.write(dest,"TEST".getBytes(StandardCharsets.UTF_8));

		assertThat(Files.sameContent(source, dest)).isFalse();
	}
}