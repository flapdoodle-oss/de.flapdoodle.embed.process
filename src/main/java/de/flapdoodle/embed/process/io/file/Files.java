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
package de.flapdoodle.embed.process.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * as we cleanup everything in tear down we may dont need this stuff anymore
 * @see de.flapdoodle.embed.process.io.Files#deleteAll(Path)
 */
@Deprecated
public abstract class Files {

	private static Logger logger = LoggerFactory.getLogger(Files.class);
	private static final int BYTE_BUFFER_LENGTH = 1024 * 16;
	/**
	 * Instance to force loading {@link DeleteDirVisitor} class to avoid
	 * {@link NoClassDefFoundError} in shutdown hook.
	 */
	private static final SimpleFileVisitor<Path> DELETE_DIR_VISITOR = new DeleteDirVisitor();

	private Files() {
		// no instance
	}

	public static boolean delete(final Path fileOrDir) {
		boolean ret = false;

		try {
			if ((fileOrDir != null) && (fileOrDir.toFile().exists())) {
				forceDelete(fileOrDir);
				logger.debug("could delete {}", fileOrDir);
				ret = true;
			}
		} catch (IOException e) {
			logger.warn("could not delete {}. Will try to delete it again when program exits.", fileOrDir);
			ShutdownHooks.forceDeleteOnExit(fileOrDir);
			ret = true;
		}

		return ret;
	}

	/**
	 * Deletes a path from the filesystem
	 *
	 * If the path is a directory its contents
	 * will be recursively deleted before it itself
	 * is deleted.
	 *
	 * Note that removal of a directory is not an atomic-operation
	 * and so if an error occurs during removal, some of the directories
	 * descendants may have already been removed
	 *
	 * @throws IOException if an error occurs whilst removing a file or directory
	 */
	public static void forceDelete(final Path path) throws IOException {
		if (!java.nio.file.Files.isDirectory(path)) {
			java.nio.file.Files.delete(path);
		} else {
			java.nio.file.Files.walkFileTree(path, DeleteDirVisitor.getInstance());
		}
	}
	
	private static class DeleteDirVisitor extends SimpleFileVisitor<Path> {
		public static SimpleFileVisitor<Path> getInstance() {
			return DELETE_DIR_VISITOR;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			java.nio.file.Files.deleteIfExists(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
			if (exc != null) {
				throw exc;
			}

			java.nio.file.Files.deleteIfExists(dir);
			return FileVisitResult.CONTINUE;
		}
	}

	public static void write(InputStream inputStream, long size, Path destination) throws IOException {
		try (final OutputStream out = java.nio.file.Files.newOutputStream(destination)) {
			final byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			int read;
			int left = buf.length;
			if (left > size) {
				left = (int) size;
			}
			while ((read = inputStream.read(buf, 0, left)) > 0) {

				out.write(buf, 0, read);

				size = size - read;
				if (left > size)
					left = (int) size;
			}
		}
	}
}
