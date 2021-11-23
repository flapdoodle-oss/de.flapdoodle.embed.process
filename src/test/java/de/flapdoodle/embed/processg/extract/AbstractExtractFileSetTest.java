package de.flapdoodle.embed.processg.extract;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.Archive;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

class AbstractExtractFileSetTest {

	@TempDir
	Path tempDir;

	@Test
	public void testForExceptionHint() {
		FileSet fileSet = FileSet.builder()
			.addEntry(FileType.Executable, "foo-bar.exe", Pattern.compile("."))
			.build();

		AbstractExtractFileSet testee = new AbstractExtractFileSet() {

			@Override
			protected Archive.Wrapper archiveStream(Path source) throws IOException {
				throw new IOException("foo");
			}
		};

		Assertions.assertThatThrownBy(() -> testee.extract(tempDir, Paths.get("bar"), fileSet))
			.isInstanceOf(IOException.class);
	}

}