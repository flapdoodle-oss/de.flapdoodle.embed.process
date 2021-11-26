package de.flapdoodle.embed.processg.store;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.processg.extract.ArchiveType;
import de.flapdoodle.os.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LocalArchiveStoreTest {

	@Test
	public void storeAndReadBack(@TempDir Path baseDir) throws URISyntaxException, IOException {
		LocalArchiveStore testee = new LocalArchiveStore(baseDir);

		Distribution distribution = Distribution.of(Version.of("foo"), Platform.detect());
		ArchiveType zip = ArchiveType.ZIP;
		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path storedArchive = testee.store("test", distribution, zip, archive);

		System.out.println("--> "+storedArchive);

		Optional<Path> readBack = testee.archiveFor("test", distribution, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}
}