package de.flapdoodle.embed.process.store;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.archives.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.io.Files;
import de.flapdoodle.hash.Hasher;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class ExtractedFileSets {
	private static Logger logger = LoggerFactory.getLogger(ExtractedFileSets.class);

	private ExtractedFileSets() {
		// no instance
	}

	@Deprecated
	static Optional<String> archiveAndFileSetDescriptionHashOrEmpty(Path archive, FileSet fileSet) {
		Optional<String> cacheKey = Try.supplier(() -> ExtractedFileSets.archiveAndFileSetDescriptionHash(archive, fileSet))
			.mapException(ex -> new IOException("could not create cache key for "+archive, ex))
			.onCheckedException(ex -> logger.warn("hash for "+archive, ex))
			.get();
		return cacheKey;
	}

	static Hasher hasherWithFileSetHead(FileSet fileSet) {
		Hasher hasher = Hasher.instance();
		fileSet.entries().forEach(entry -> {
			hasher.update(entry.type().name().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.destination().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.matchingPattern().toString().getBytes(StandardCharsets.UTF_8));
		});
		hasher.update("--".getBytes(StandardCharsets.UTF_8));
		return hasher;
	}

	static String archiveAndFileSetDescriptionHashOrException(Path archive, FileSet fileSet) {
		try {
			return ExtractedFileSets.archiveAndFileSetDescriptionHash(archive, fileSet);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// VisibleForTesting
	static String archiveAndFileSetDescriptionHash(Path archive, FileSet fileSet) throws IOException {
		Hasher hasher = hasherWithFileSetHead(fileSet);
		hasher.update(archive.toString());
		hasher.update(java.nio.file.Files.getLastModifiedTime(archive, LinkOption.NOFOLLOW_LINKS).toString());
		return hasher.hashAsString();
	}

	static String archiveContentAndFileSetDescriptionHash(Path archive, FileSet fileSet, int blocksize) {
		Hasher hasher = hasherWithFileSetHead(fileSet);
		Try.run(() -> {
			ByteBuffer buffer = ByteBuffer.allocate(blocksize);

			try(SeekableByteChannel channel = java.nio.file.Files.newByteChannel(archive)) {
				while (channel.read(buffer) > 0) {
					buffer.flip();
					hasher.update(buffer);
					buffer.clear();
				}
			}
		});
		return hasher.hashAsString();
	}

	static ExtractedFileSet makeCopyOf(Path fileSetBasePath, FileSet fileSet, ExtractedFileSet src) throws IOException {
		try {
			Map<String, Path> nameMap = src.libraryFiles()
				.stream()
				.collect(Collectors.toMap(it -> src.baseDir().relativize(it).toString(), Function.identity()));

			ImmutableExtractedFileSet.Builder builder = ExtractedFileSet.builder(fileSetBasePath);
			for (FileSet.Entry entry : fileSet.entries()) {
				Path dest = fileSetBasePath.resolve(entry.destination());
				switch (entry.type()) {
					case Executable:
						if (!java.nio.file.Files.exists(dest.getParent())) java.nio.file.Files.createDirectory(dest.getParent());
						java.nio.file.Files.copy(src.executable(), dest, StandardCopyOption.COPY_ATTRIBUTES);
						Preconditions.checkArgument(dest.toFile().setExecutable(true),"could not make %s executable", dest);
						builder.executable(dest);
						break;
					case Library:
						Path srcPath = nameMap.get(entry.destination());
						if (srcPath==null) throw new IOException("could not find entry for "+entry.destination()+" in "+nameMap);
						if (!java.nio.file.Files.exists(dest.getParent())) java.nio.file.Files.createDirectory(dest.getParent());
						java.nio.file.Files.copy(srcPath, dest, StandardCopyOption.COPY_ATTRIBUTES);
						builder.addLibraryFiles(dest);
						break;
				}
			}
			return builder.build();
		} catch (IOException iox) {
			Files.deleteAll(fileSetBasePath);
			throw iox;
		}
	}
	static ExtractedFileSet readFileSet(Path fileSetBasePath, FileSet fileSet) {
		ImmutableExtractedFileSet.Builder builder = ExtractedFileSet.builder(fileSetBasePath);
		fileSet.entries().forEach(entry -> {
			Path entryPath = fileSetBasePath.resolve(entry.destination());
			Preconditions.checkArgument(java.nio.file.Files.exists(entryPath),"could not find matching file: %s", entryPath);
			switch (entry.type()) {
				case Executable:
					builder.executable(entryPath);
					break;
				case Library:
					builder.addLibraryFiles(entryPath);
					break;
			}
		});
		return builder.build();
	}
}
