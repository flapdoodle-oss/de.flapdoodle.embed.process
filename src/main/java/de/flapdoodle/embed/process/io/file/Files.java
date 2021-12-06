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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;

/**
 *
 */
@Deprecated
public class Files {

	private static Logger logger = LoggerFactory.getLogger(Files.class);
	private static final int BYTE_BUFFER_LENGTH = 1024 * 16;
	/**
	 * Instance to force loading {@link DeleteDirVisitor} class to avoid
	 * {@link NoClassDefFoundError} in shutdown hook.
	 */
	private static final SimpleFileVisitor<Path> DELETE_DIR_VISITOR = new DeleteDirVisitor();

	private Files() {

	}

	@Deprecated
	public static File createTempFile(String tempFileName) throws IOException {
		return createTempFile(PropertyOrPlatformTempDir.defaultInstance(), tempFileName);
	}

	public static File createTempFile(Directory directory, String tempFileName) throws IOException {
		File tempDir = directory.asFile();
		return createTempFile(tempDir, tempFileName);
	}

	public static File createTempFile(File tempDir, String tempFileName) throws IOException {
		File tempFile =  fileOf(tempDir, tempFileName+"--"+ UUID.randomUUID());
		createOrCheckDir(tempFile.getParentFile());
		if (!tempFile.createNewFile())
			throw new FileAlreadyExistsException("could not create",tempFile);
		return tempFile;
	}

	public static File createOrCheckDir(String dir) throws IOException {
		File tempFile = new File(dir);
		return createOrCheckDir(tempFile);
	}

	public static File createOrCheckDir(File dir) throws IOException {
		if ((dir.exists()) && (dir.isDirectory()))
			return dir;
		return createDir(dir);
	}

	public static File createOrCheckUserDir(String prefix) throws IOException {
		File tempDir = new File(System.getProperty("user.home"));
		File tempFile = new File(tempDir, prefix);
		return createOrCheckDir(tempFile);
	}

	@Deprecated
	public static File createTempDir(String prefix) throws IOException {
		return createTempDir(PropertyOrPlatformTempDir.defaultInstance(), prefix);
	}

	public static File createTempDir(Directory directory, String prefix) throws IOException {
		File tempDir = directory.asFile();
		return createTempDir(tempDir, prefix);
	}

	public static File createTempDir(File tempDir, String prefix) throws IOException {
		File tempFile = new File(tempDir, prefix + "-" + UUID.randomUUID().toString());
		return createDir(tempFile);
	}

	public static File createDir(File tempFile) throws IOException {
		if (!tempFile.mkdirs())
			throw new IOException("could not create dirs: " + tempFile);
		return tempFile;
	}

	public static boolean forceDelete(final File fileOrDir) {
		boolean ret = false;

		try {
			if ((fileOrDir != null) && (fileOrDir.exists())) {
				forceDelete(fileOrDir.toPath());
				logger.debug("could delete {}", fileOrDir);
				ret = true;
			}
		} catch (IOException e) {
			logger.warn("could not delete {}. Will try to delete it again when program exits.", fileOrDir);
			FileCleaner.forceDeleteOnExit(fileOrDir);
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

	public static void write(final InputStream in, long size, final File output)
			throws IOException {
		try (final OutputStream out = java.nio.file.Files.newOutputStream(output.toPath())) {
			final byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			int read;
			int left = buf.length;
			if (left > size) {
				left = (int) size;
			}
			while ((read = in.read(buf, 0, left)) > 0) {

				out.write(buf, 0, read);

				size = size - read;
				if (left > size)
					left = (int) size;
			}
		}
	}

	public static void write(final InputStream in, final File output) throws IOException {
		java.nio.file.Files.copy(in, output.toPath());
	}

	public static void write(final String content, final File output) throws IOException {
		java.nio.file.Files.write(output.toPath(), content.getBytes());
	}

	public static void moveFile(final File source, final File destination) throws IOException {
    java.nio.file.Files.move(source.toPath(), destination.toPath());
	}

	public static File fileOf(File base, File relative) {
		return base.toPath().resolve(relative.toPath()).toFile();
	}

	public static File fileOf(File base, String relative) {
		return base.toPath().resolve(relative).toFile();
	}
}
