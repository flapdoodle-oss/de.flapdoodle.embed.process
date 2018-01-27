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

import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

/**
 *
 */
public class Files {

	private static Logger logger = LoggerFactory.getLogger(Files.class);
	public static final int BYTE_BUFFER_LENGTH = 1024 * 16;

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
		File tempFile =  fileOf(tempDir, tempFileName);
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

	public static boolean forceDelete(File fileOrDir) {
		boolean ret = false;

		try {
			if ((fileOrDir != null) && (fileOrDir.exists())) {
				FileUtils.forceDelete(fileOrDir);
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

	public static void write(InputStream in, long size, File output) throws IOException {
		try (FileOutputStream out = new FileOutputStream(output)) {
			byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			int read;
			int left = buf.length;
			if (left > size)
				left = (int) size;
			while ((read = in.read(buf, 0, left)) > 0) {

				out.write(buf, 0, read);

				size = size - read;
				if (left > size)
					left = (int) size;
			}
		}
	}

	public static void write(InputStream in, File output) throws IOException {
		try (FileOutputStream out = new FileOutputStream(output)) {
			byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			int read;
			while ((read = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, read);
			}
		}
	}

	public static void write(String content, File output) throws IOException {
		try (final FileOutputStream out = new FileOutputStream(output);
			 final OutputStreamWriter w = new OutputStreamWriter(out)) {
			w.write(content);
			w.flush();
		}
	}

	public static boolean moveFile(File source, File destination) {
		if (!source.renameTo(destination)) {
			// move konnte evtl. nicht durchgeführt werden
			try {
				java.nio.file.Files.copy(source.toPath(), destination.toPath());
				return source.delete();
			} catch (IOException iox) {
				return false;
			}
		}
		return true;
	}

	public static File fileOf(File base, File relative) {
		return base.toPath().resolve(relative.toPath()).toFile();
	}
	
	public static File fileOf(File base, String relative) {
		return base.toPath().resolve(relative).toFile();
	}
}
