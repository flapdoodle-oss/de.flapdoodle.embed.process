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
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.hash.Hasher;
import de.flapdoodle.types.Try;
import org.jheaps.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class LocalDownloadCache implements DownloadCache, DownloadCacheGuessStorePath {

	private static final Logger logger = LoggerFactory.getLogger(LocalDownloadCache.class);

	private final Path baseDir;

	public LocalDownloadCache(Path baseDir) {
		this.baseDir = baseDir;
		if (!java.nio.file.Files.exists(baseDir)) {
			Try.run(() -> java.nio.file.Files.createDirectory(baseDir));
		}
	}

	@Override
	public Path archivePath(URL url, ArchiveType archiveType) {
		return resolve(baseDir, url, archiveType);
	}

	@Override
	public Optional<Path> archiveFor(URL url, ArchiveType archiveType) {
		Path arcFile = archivePath(url, archiveType);
		return Files.isReadable(arcFile)
			? Optional.of(arcFile)
			: Optional.empty();
	}

	@Override
	public Path store(URL url, ArchiveType archiveType, Path archive) throws IOException {
		Path arcFile = archivePath(url, archiveType);
		Path arcDirectory = arcFile.getParent();
		checkArgument(arcDirectory!=null,"no parent directory for %s",arcFile);
		if (!Files.exists(arcDirectory)) {
			logger.debug("creating archive directory {}", arcDirectory);
			Files.createDirectories(arcDirectory);
		}
		if (Files.exists(arcFile)) {
			logger.debug("archive file already exist {}", arcFile);
			checkArgument(fileContentIsTheSame(archive, arcFile),"archive for %s:%s already exists with different content (%s)",url,archiveType, arcFile);
			logger.debug("archive file content matches {} == {}", arcFile, archive);
			return arcFile;
		} else {
			try {
				return copyAndMove(archive, arcFile);
			} catch (FileAlreadyExistsException fx) {
				logger.debug("copy failed, archive already exist: {}", arcFile);
				checkArgument(fileContentIsTheSame(archive, arcFile),"archive for %s:%s already exists with different content (%s)",url,archiveType, arcFile);
				logger.debug("archive file content matches {} == {}", arcFile, archive);
				return arcFile;
			}
		}
	}

	static Path copyAndMove(Path archive, Path destination) throws IOException {
		Path tmpFile = destination.getParent().resolve(UUID.randomUUID().toString());
		try {
			logger.debug("copy archive {} to temp file {}", archive, tmpFile);
			Files.copy(archive, tmpFile, StandardCopyOption.COPY_ATTRIBUTES);
			logger.debug("move temp file {} to store location {}", tmpFile, destination);
			if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
				throw new FileAlreadyExistsException("what?");
			}
			// Files.move does not always throw an exception if file already exists.. hmm
			return Files.move(tmpFile, destination, StandardCopyOption.ATOMIC_MOVE);
		} finally {
			logger.debug("remove temp file {} if exists", tmpFile);
			Files.deleteIfExists(tmpFile);
		}
	}

	static void checkArgument(boolean expression, String errorMessage, Object... args) throws IOException  {
		try {
			Preconditions.checkArgument(expression, errorMessage, args);
		} catch (RuntimeException ex) {
			throw new IOException(ex.getLocalizedMessage(), ex);
		}
	}

	@VisibleForTesting
	static Path resolve(Path base, URL url, ArchiveType archiveType) {
		UrlParts parts = partsOf(url);

		Preconditions.checkArgument(url.toString().equals(parts.asString()),"parts missing: '%s' != '%s'",url,parts);

		String serverPart = parts.serverPart();
		String pathPart = parts.pathPart();

		return base
			.resolve(sanitize(serverPart))
			.resolve(Hasher.md5Instance()
				.update(serverPart)
				.hashAsString())
			.resolve(sanitize(pathPart))
			.resolve(Hasher.instance()
				.update(pathPart)
				.hashAsString())
			.resolve("archive."+asExtension(archiveType));
	}

	private static final Pattern PATH_SEP_MATCHER =Pattern.compile("[/\\\\]+");
	private static final Pattern UNWANTED_CHARS_MATCHER =Pattern.compile("[^a-zA-Z0-9]+");

	@VisibleForTesting
	static String sanitize(String src) {
		String strippedFromPathSeparator = PATH_SEP_MATCHER.matcher(src).replaceAll("");
		return UNWANTED_CHARS_MATCHER.matcher(strippedFromPathSeparator).replaceAll("-");
	}

	private static String pathPart(URL url) {
		return url.getPath()+(url.getQuery()!= null ?  "?"+url.getQuery() : "");
	}

	@VisibleForTesting
	static UrlParts partsOf(URL url) {
		boolean portIsPartOfTheUrl = url.getPort() != -1 && url.getPort() != url.getDefaultPort();

		return UrlParts.of(
			url.getProtocol(),
			url.getUserInfo(),
			url.getHost().isEmpty() ? null : url.getHost() + (portIsPartOfTheUrl ? ":" + url.getPort() : ""),
			pathPart(url));
	}

	@VisibleForTesting
	static class UrlParts {
		final String protocol;
		final String userInfo;
		final String host;
		final String path;
		final String hashedUserInfo;

		private UrlParts(String protocol, String userInfo, String host, String path) {
			this.protocol = protocol;
			this.userInfo = userInfo;
			this.host = host;
			this.path = path;
			this.hashedUserInfo = userInfo != null
				? Hasher.md5Instance().update(userInfo).hashAsString()
				: null;
		}
		
		@VisibleForTesting
		static UrlParts of(String protocol, String userInfo, String host, String path) {
			return new UrlParts(protocol, userInfo, host, path);
		}

		@Override
		public String toString() {
			return "UrlParts{" +
				"protocol='" + protocol + '\'' +
				", userInfo='" + userInfo + '\'' +
				", host='" + host + '\'' +
				", path='" + path + '\'' +
				'}';
		}

		public String asString() {
			return protocol + (host == null ? ":" : "://" + (userInfo != null ? userInfo + "@" : "") + host) + path;
		}

		public String serverPart() {
			return protocol + (host == null ? ":" : "://" + (hashedUserInfo != null ? hashedUserInfo + "@" : "") + host);
		}

		public String pathPart() {
			return path;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UrlParts urlParts = (UrlParts) o;
			return Objects.equals(protocol, urlParts.protocol) && Objects.equals(userInfo, urlParts.userInfo) && Objects.equals(host,
				urlParts.host) && Objects.equals(path, urlParts.path);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(protocol, userInfo, host, path);
		}
	}

	private static String asExtension(ArchiveType archiveType) {
		switch (archiveType) {
			case ZIP: return "zip";
			case TGZ: return "tgz";
			case TBZ2: return "tbz2";
			case TXZ: return "txz";
			case EXE: return "exe";
		}
		throw new IllegalArgumentException("Unknown archiveType: "+archiveType);
	}

	private static boolean fileContentIsTheSame(Path first, Path second) throws IOException {
		try (RandomAccessFile firstFile = new RandomAccessFile(first.toFile(), "r");
			RandomAccessFile secondFild = new RandomAccessFile(second.toFile(), "r")) {

			FileChannel ch1 = firstFile.getChannel();
			FileChannel ch2 = secondFild.getChannel();
			if (ch1.size() != ch2.size()) {
				return false;
			}
			long size = ch1.size();
			MappedByteBuffer m1 = ch1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
			MappedByteBuffer m2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0L, size);

			return m1.equals(m2);
		}
	}
}
