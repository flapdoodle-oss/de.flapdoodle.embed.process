/**
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
import de.flapdoodle.embed.process.hash.Hasher;
import org.jheaps.annotations.VisibleForTesting;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.regex.Pattern;

public class LocalDownloadCache implements DownloadCache {

	private final Path baseDir;

	public LocalDownloadCache(Path baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public Optional<Path> archiveFor(URL url, ArchiveType archiveType) {
		Path arcFile = resolve(baseDir, url, archiveType);
		return Files.isReadable(arcFile)
			? Optional.of(arcFile)
			: Optional.empty();
	}
	
	@Override
	public Path store(URL url, ArchiveType archiveType, Path archive) throws IOException {
		Path arcFile = resolve(baseDir, url, archiveType);
		Path arcDirectory = arcFile.getParent();
		Preconditions.checkArgument(arcDirectory!=null,"no parent directory for %s",arcFile);
		if (!Files.exists(arcDirectory)) {
			Files.createDirectories(arcDirectory);
		}
		Preconditions.checkArgument(!Files.exists(arcFile),"archive for %s:%s already exists (%s)",url,archiveType, arcFile);
		return Files.copy(archive, arcFile, StandardCopyOption.COPY_ATTRIBUTES);
	}

	@VisibleForTesting
	static Path resolve(Path base, URL url, ArchiveType archiveType) {
		String serverPart = serverPart(url);
		String pathPart = pathPart(url);

		Preconditions.checkArgument(url.toString().equals(serverPart+pathPart),"parts missing: '%s' != '%s'+'%s'",url,serverPart,pathPart);

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

	private static String serverPart(URL url) {
		boolean portIsPartOfTheUrl=url.getPort()!=-1 && url.getPort()!=url.getDefaultPort();
		return url.getProtocol() + "://" + url.getHost() + (portIsPartOfTheUrl ? ":"+url.getPort() : "");
	}

	private static String pathPart(URL url) {
		return url.getPath()+(url.getQuery()!= null ?  "?"+url.getQuery() : "");
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

}
