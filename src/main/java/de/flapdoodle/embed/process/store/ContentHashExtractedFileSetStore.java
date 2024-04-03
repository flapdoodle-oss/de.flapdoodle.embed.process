/*
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
package de.flapdoodle.embed.process.store;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.archives.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.io.Files;
import de.flapdoodle.hash.Hasher;
import de.flapdoodle.types.Try;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContentHashExtractedFileSetStore implements ExtractedFileSetStore {

	private static final int HASH_BUFFER_SIZE=1024*1024;

	private final Path basePath;
	private final Path cachePath;

	public ContentHashExtractedFileSetStore(Path basePath) {
		this.basePath = basePath;
		this.cachePath = basePath.resolve("hashes");
		if (!java.nio.file.Files.exists(basePath)) {
			Try.run(() -> java.nio.file.Files.createDirectory(basePath));
		}
		if (!java.nio.file.Files.exists(cachePath)) {
			Try.run(() -> java.nio.file.Files.createDirectory(cachePath));
		}
	}

	@Override
	public Optional<ExtractedFileSet> extractedFileSet(Path archive, FileSet fileSet) {
		String hash = archiveContentAndFileSetDescriptionHash(cachePath, archive, fileSet);
		Path fileSetBasePath = basePath.resolve(hash);
		if (java.nio.file.Files.isDirectory(fileSetBasePath)) {
			return Try.supplier(() -> Optional.of(readFileSet(fileSetBasePath, fileSet)))
				.fallbackTo(ex -> Optional.empty())
				.get();
		}
		
		return Optional.empty();
	}

	@Override
	public ExtractedFileSet store(Path archive, FileSet fileSet, ExtractedFileSet src) throws IOException {
		String hash = archiveContentAndFileSetDescriptionHash(cachePath, archive, fileSet);
		Path fileSetBasePath = basePath.resolve(hash);
		Path tempCopyDir = basePath.resolve("store-"+ UUID.randomUUID());
		Preconditions.checkArgument(!java.nio.file.Files.exists(tempCopyDir), "temp copy directory already exists: %s", tempCopyDir);

		try {
			java.nio.file.Files.createDirectory(tempCopyDir);
			makeCopyOf(tempCopyDir, fileSet, src);
			synchronized (this) {
				if (!java.nio.file.Files.exists(fileSetBasePath)) {
					java.nio.file.Files.move(tempCopyDir, fileSetBasePath, StandardCopyOption.ATOMIC_MOVE);
					return readFileSet(fileSetBasePath, fileSet);
				} else {
					return readFileSet(fileSetBasePath, fileSet);
				}
			}
		} finally {
			if (java.nio.file.Files.exists(tempCopyDir)) {
				Files.deleteAll(tempCopyDir);
			}
		}

//		Preconditions.checkArgument(!java.nio.file.Files.exists(fileSetBasePath),"hash collision for %s (hash=%s)",archive, hash);
//		java.nio.file.Files.createDirectory(fileSetBasePath);
//		return makeCopyOf(fileSetBasePath, fileSet, src);
	}
	
	private static ExtractedFileSet makeCopyOf(Path fileSetBasePath, FileSet fileSet, ExtractedFileSet src) throws IOException {
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

	// VisibleForTesting
	static String archiveContentAndFileSetDescriptionHash(Path cachePath, Path archive, FileSet fileSet) {
		Preconditions.checkArgument(java.nio.file.Files.exists(cachePath, LinkOption.NOFOLLOW_LINKS),"cache does not exsist: %s", cachePath);
		Preconditions.checkArgument(java.nio.file.Files.isDirectory(cachePath, LinkOption.NOFOLLOW_LINKS),"cache is not a directory: %s", cachePath);

		Optional<String> cacheKey = Try.supplier(() -> archiveAndFileSetDescriptionHash(archive, fileSet))
			.mapException(ex -> new IOException("could not create cache key for "+archive, ex))
			.onCheckedException(Throwable::printStackTrace)
			.get();

		return readOrCreateArchiveContentAndFileSetDescriptionHash(cacheKey.map(cachePath::resolve), archive, fileSet);
	}

	private static synchronized String readOrCreateArchiveContentAndFileSetDescriptionHash(Optional<Path> optCachedHashPath, Path archive, FileSet fileSet) {
		if (optCachedHashPath.isPresent()) {
			Path cachedHashPath = optCachedHashPath.get();

			if (java.nio.file.Files.exists(cachedHashPath)) {
				return readHash(cachedHashPath);
			} else {
				String hash = archiveContentAndFileSetDescriptionHash(archive, fileSet, HASH_BUFFER_SIZE);
				Try.run(() -> java.nio.file.Files.write(cachedHashPath, hash.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW));
				return hash;
			}
		}

		return archiveContentAndFileSetDescriptionHash(archive, fileSet, HASH_BUFFER_SIZE);
	}

	private static String readHash(Path cachedHashPath) {
		byte[] hashBytes = Try.supplier(() -> java.nio.file.Files.readAllBytes(cachedHashPath))
			.mapToUncheckedException(ex -> new RuntimeException("could not read cached key from " + cachedHashPath, ex))
			.get();
		return new String(hashBytes, StandardCharsets.UTF_8);
	}

	// VisibleForTesting
	static String archiveAndFileSetDescriptionHash(Path archive, FileSet fileSet) throws IOException {
		Hasher hasher = Hasher.instance();
		fileSet.entries().forEach(entry -> {
			hasher.update(entry.type().name().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.destination().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.matchingPattern().toString().getBytes(StandardCharsets.UTF_8));
		});
		hasher.update("--".getBytes(StandardCharsets.UTF_8));
		hasher.update(archive.toString());
		hasher.update(java.nio.file.Files.getLastModifiedTime(archive, LinkOption.NOFOLLOW_LINKS).toString());
		return hasher.hashAsString();
	}

	// VisibleForTesting
	static String archiveContentAndFileSetDescriptionHash(Path archive, FileSet fileSet, int blocksize) {
		Hasher hasher = Hasher.instance();
		fileSet.entries().forEach(entry -> {
			hasher.update(entry.type().name().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.destination().getBytes(StandardCharsets.UTF_8));
			hasher.update(entry.matchingPattern().toString().getBytes(StandardCharsets.UTF_8));
		});
		hasher.update("--".getBytes(StandardCharsets.UTF_8));
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

	private static ExtractedFileSet readFileSet(Path fileSetBasePath, FileSet fileSet) {
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
