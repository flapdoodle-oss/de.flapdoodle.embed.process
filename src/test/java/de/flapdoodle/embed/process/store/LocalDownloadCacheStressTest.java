package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

		ExecutorService executorService = Executors.newFixedThreadPool(4);
		Future<Path> first = executorService.submit(() -> testee.store(url, zip, archive));
		Future<Path> second = executorService.submit(() -> testee.store(url, zip, archive));
		Future<Path> third = executorService.submit(() -> testee.store(url, zip, archive));

//		Path storedArchive = testee.store(url, zip, archive);
		Path storedArchive = first.get();
		Path storedSecondCall = second.get();
		Path storedThirdCall = third.get();

		Optional<Path> readBack = testee.archiveFor(url, zip);

		assertThat(readBack)
			.isPresent()
			.contains(storedArchive)
			.contains(storedSecondCall)
			.contains(storedThirdCall);

		assertThat(storedArchive.toFile())
			.hasSameBinaryContentAs(archive.toFile());
	}

	@Test
	public void copyAndMoveMustCleanup(@TempDir Path baseDir) throws IOException, URISyntaxException {
		Path store = Files.createDirectory(baseDir.resolve("store"));
		Path archive = Paths.get(this.getClass().getResource("/archives/sample.zip").toURI());

		Path destination = LocalDownloadCache.copyAndMove(archive, store.resolve("archive.zip"));

		assertThat(destination.toFile())
			.hasSameBinaryContentAs(archive.toFile());

		assertThatThrownBy(() -> LocalDownloadCache.copyAndMove(archive, store.resolve("archive.zip")))
			.isInstanceOf(FileAlreadyExistsException.class);

		try(Stream<Path> storedFiles = Files.list(store)) {
			Set<String> names = storedFiles.map(it -> it.getFileName().toString()).collect(Collectors.toSet());
			assertThat(names)
				.hasSize(1)
				.containsExactly("archive.zip");
		}
	}

	@Test
	public void moveDoesNotFailIfFileAlreadyExistsOnAtomicMove(@TempDir Path baseDir) throws IOException, URISyntaxException {
		Path source = baseDir.resolve("source");
		Path destination = baseDir.resolve("destination");

		Files.write(source, "hello".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(destination, "world".getBytes(), StandardOpenOption.CREATE_NEW);

		assertThat(source).exists();
		assertThat(destination).exists()
			.hasContent("world");

		Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);

		assertThat(source).doesNotExist();
		assertThat(destination).exists()
			.hasContent("hello");
	}
}
