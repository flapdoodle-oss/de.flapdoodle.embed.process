package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDownloadCacheStressTest {

	@Test
	public void tryToStoreBigFileMultipleTimes(@TempDir Path baseDir) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
		Path store = Files.createDirectory(baseDir.resolve("store"));
		Path bigFiles = Files.createDirectory(baseDir.resolve("bigFile"));
		Path archive = bigFiles.resolve("big");

		byte[] segment=new byte[1024*1024];
		ThreadLocalRandom.current().nextBytes(segment);

		try (OutputStream buffer = Files.newOutputStream(archive, StandardOpenOption.CREATE_NEW)) {
			for (int i=0;i<10;i++) {
				buffer.write(segment);
			}
		}

		LocalDownloadCache testee = new LocalDownloadCache(store);

		URL url=new URL("http://foo/downloads/archive?latest=true");
		ArchiveType zip = ArchiveType.ZIP;

		ExecutorService executorService = Executors.newFixedThreadPool(2);
		Future<Path> first = executorService.submit(() -> testee.store(url, zip, archive));
		Future<Path> second = executorService.submit(() -> testee.store(url, zip, archive));

//		Path storedArchive = testee.store(url, zip, archive);
		Path storedArchive = first.get();
		Path storedSecondCall = second.get();

		Optional<Path> readBack = testee.archiveFor(url, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive)
			.contains(storedSecondCall);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());

	}
}
