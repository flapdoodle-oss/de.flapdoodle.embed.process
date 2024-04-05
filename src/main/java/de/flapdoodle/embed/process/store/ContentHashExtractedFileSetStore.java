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
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.io.Files;
import de.flapdoodle.types.Try;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

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
			return Try.supplier(() -> Optional.of(ExtractedFileSets.readFileSet(fileSetBasePath, fileSet)))
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
			ExtractedFileSets.makeCopyOf(tempCopyDir, fileSet, src);
			if (!java.nio.file.Files.exists(fileSetBasePath)) {
				try {
					java.nio.file.Files.move(tempCopyDir, fileSetBasePath, StandardCopyOption.ATOMIC_MOVE);
				} catch (FileSystemException fx) {
					if (!java.nio.file.Files.exists(fileSetBasePath)) {
						throw new IOException("move "+tempCopyDir+" to "+fileSetBasePath+" failed, but "+fileSetBasePath+" still does not exist");
					}
				}
			}
			return ExtractedFileSets.readFileSet(fileSetBasePath, fileSet);
		} finally {
			if (java.nio.file.Files.exists(tempCopyDir)) {
				Files.deleteAll(tempCopyDir);
			}
		}

//		Preconditions.checkArgument(!java.nio.file.Files.exists(fileSetBasePath),"hash collision for %s (hash=%s)",archive, hash);
//		java.nio.file.Files.createDirectory(fileSetBasePath);
//		return makeCopyOf(fileSetBasePath, fileSet, src);
	}

	// VisibleForTesting
	static String archiveContentAndFileSetDescriptionHash(Path cachePath, Path archive, FileSet fileSet) {
		Preconditions.checkArgument(java.nio.file.Files.exists(cachePath, LinkOption.NOFOLLOW_LINKS),"cache does not exsist: %s", cachePath);
		Preconditions.checkArgument(java.nio.file.Files.isDirectory(cachePath, LinkOption.NOFOLLOW_LINKS),"cache is not a directory: %s", cachePath);

		String cacheKey = ExtractedFileSets.archiveAndFileSetDescriptionHashOrException(archive, fileSet);

		return readOrCreateArchiveContentAndFileSetDescriptionHash(cachePath.resolve(cacheKey), archive, fileSet);
	}

	private static String readOrCreateArchiveContentAndFileSetDescriptionHash(Path cachedHashPath, Path archive, FileSet fileSet) {
		if (java.nio.file.Files.exists(cachedHashPath)) {
			return readHash(cachedHashPath);
		} else {
			String hash = ExtractedFileSets.archiveContentAndFileSetDescriptionHash(archive, fileSet, HASH_BUFFER_SIZE);
			Try.run(() -> java.nio.file.Files.write(cachedHashPath, hash.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
			return hash;
		}
	}

	private static String readHash(Path cachedHashPath) {
		byte[] hashBytes = Try.supplier(() -> java.nio.file.Files.readAllBytes(cachedHashPath))
			.mapToUncheckedException(ex -> new RuntimeException("could not read cached key from " + cachedHashPath, ex))
			.get();
		return new String(hashBytes, StandardCharsets.UTF_8);
	}

}
