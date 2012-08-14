/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 */
public class Files {

	private static Logger logger = Logger.getLogger(Files.class.getName());
	public static final int BYTE_BUFFER_LENGTH = 1024 * 16;

	private Files() {

	}

	public static File createTempFile(String tempFileName) throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFile = new File(tempDir, tempFileName);
		if (!tempFile.createNewFile())
			throw new IOException("Could not create Tempfile: " + tempFile);
		return tempFile;
	}

	public static File createOrCheckDir(String dir) throws IOException {
		File tempFile = new File(dir);
		if ((tempFile.exists()) && (tempFile.isDirectory()))
			return tempFile;
		if (!tempFile.mkdir())
			throw new IOException("Could not create Tempdir: " + tempFile);
		return tempFile;
	}

	public static File createOrCheckUserDir(String prefix) throws IOException {
		File tempDir = new File(System.getProperty("user.home"));
		File tempFile = new File(tempDir, prefix);
		if ((tempFile.exists()) && (tempFile.isDirectory()))
			return tempFile;
		if (!tempFile.mkdir())
			throw new IOException("Could not create Tempdir: " + tempFile);
		return tempFile;
	}

	public static File createTempDir(String prefix) throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFile = new File(tempDir, prefix + "-" + UUID.randomUUID().toString());
		if (!tempFile.mkdir())
			throw new IOException("Could not create Tempdir: " + tempFile);
		return tempFile;
	}

	public static boolean forceDelete(File fileOrDir) {
		boolean ret = false;

		try {
			if ((fileOrDir != null) && (fileOrDir.exists())) {
				FileUtils.forceDelete(fileOrDir);
				logger.info("Could delete " + fileOrDir);
				ret = true;
			}
		} catch (IOException e) {
			logger.warning("Could not delete " + fileOrDir + ". Will try to delete it again when program exits.");
			try {
				FileUtils.forceDeleteOnExit(fileOrDir);
				ret = true;
			} catch (IOException ioe) {
				logger.severe("Could not delete " + fileOrDir);
				throw new IllegalStateException("Could not delete " + fileOrDir);
			}
		}

		return ret;
	}

	public static void write(InputStream in, long size, File output) throws IOException {
		FileOutputStream out = new FileOutputStream(output);

		try {
			byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			int read;
			int left = buf.length;
			if (left > size)
				left = (int) size;
			while ((read = in.read(buf, 0, left)) != -1) {

				out.write(buf, 0, read);

				size = size - read;
				if (left > size)
					left = (int) size;
			}
		} finally {
			out.close();
		}
	}
	
	public static void write(String content, File output) throws IOException {
		FileOutputStream out = new FileOutputStream(output);
		OutputStreamWriter w=new OutputStreamWriter(out);
		
		try {
			w.write(content);
			w.flush();
		} finally {
			out.close();
		}
	}

	public static boolean moveFile(File source, File destination) {
		if (!source.renameTo(destination)) {
			// move konnte evtl. nicht durchgeführt werden
			try {
				copyFile(source, destination);
				return source.delete();
			} catch (IOException iox) {
				return false;
			}
		}
		return true;
	}

	private static void copyFile(File source, File destination) throws IOException {
		FileInputStream reader = null;
		FileOutputStream writer = null;
		try {
			reader = new FileInputStream(source);
			writer = new FileOutputStream(destination);

			int read;
			byte[] buf = new byte[BYTE_BUFFER_LENGTH];
			while ((read = reader.read(buf)) != -1) {
				writer.write(buf, 0, read);
			}

		} finally {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
		}
	}

}
