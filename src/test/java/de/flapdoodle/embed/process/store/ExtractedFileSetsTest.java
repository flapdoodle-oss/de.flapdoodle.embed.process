package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.ImmutableFileSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractedFileSetsTest {

	@Test
	public void hashShouldNotChangeIfArchiveIsNotReadInOneGo(@TempDir Path tempDir) throws IOException {
		byte[] content = new byte[1024 * 256 + 123];
		ThreadLocalRandom.current().nextBytes(content);

		Path archive = tempDir.resolve("archive");

		Files.write(archive, content, StandardOpenOption.CREATE_NEW);

		ImmutableFileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo")
			.build();

		String newHash1024 = ExtractedFileSets.archiveContentAndFileSetDescriptionHash(archive, fileSet, 1024);
		String newHash123 = ExtractedFileSets.archiveContentAndFileSetDescriptionHash(archive, fileSet, 123);

		assertThat(newHash1024).isEqualTo(newHash123);
	}
}