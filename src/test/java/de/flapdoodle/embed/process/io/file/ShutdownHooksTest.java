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

import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShutdownHooksTest {

	private static Logger logger = LoggerFactory.getLogger(ShutdownHooksTest.class.getName());

	String prefix = UUID.randomUUID().toString();

	@Test
	public void testCleanup(@TempDir Path tempDir) throws IOException, InterruptedException {

		boolean runsOnWindows = Platform.detect().operatingSystem() == OS.Windows;

		List<Path> files = new ArrayList<>();

		logger.info("create temp files");

		for (int i = 0; i < 10; i++) {
			files.add(createTempFile(tempDir, "fileCleanerTest-" + prefix + "-some-" + i));
		}

		List<FileLock> locks = new ArrayList<FileLock>();

		logger.info("lock temp files");

		for (Path file : files) {
			FileLock lock = lock(file);
			if (lock == null)
				throw new RuntimeException("Could not get lock for " + file);
			locks.add(lock);
		}

		logger.info("try to delete temp files");

		for (Path file : files) {
			Thread.sleep(100);
			ShutdownHooks.forceDeleteOnExit(file);
		}

		logger.info("after try to delete temp files (wait a little)");

		if (runsOnWindows) {

			Thread.sleep(1000);

			logger.info("check if temp files there (should be)");

			for (Path file : files) {
				assertTrue(file.toFile().exists());
			}
		}

		logger.info("release lock ");

		for (FileLock lock : locks) {
			release(lock);
		}

		logger.info("after release lock (wait a little)");

		Thread.sleep(1000);

		logger.info("check if temp files there (should NOT be)");

		for (Path file : files) {
			assertFalse(file.toFile().exists());
		}
	}

	@Test
	public void testMultipleFiles(@TempDir Path tempDir) throws IOException {
		
		List<Path> files=new ArrayList<>();

		Path lastFile = createTempFile(tempDir, "fileCleanerTest-" + prefix + "-some-final");
		for (int i=0;i<10000;i++) {
			ShutdownHooks.forceDeleteOnExit(lastFile);
		}
		
		for (int i = 0; i < 100; i++) {
			files.add(createTempFile(tempDir, "fileCleanerTest-" + prefix + "-some-" + i));
		}
		
		for (Path file : files) {
			ShutdownHooks.forceDeleteOnExit(file);
		}

		assertThat(lastFile).doesNotExist();
		for (Path file : files) {
			assertThat(file).doesNotExist();
		}
	}

	public FileLock lock(Path file) throws IOException {
		FileChannel channel = new RandomAccessFile(file.toFile(), "rw").getChannel();
		return channel.tryLock();
	}

	public void release(FileLock lock) throws IOException {
		lock.release();
		lock.channel().close();
	}

	public static Path createTempFile(Path tempDir, String tempFileName) throws IOException {
		Path tempFile = tempDir.resolve(tempFileName + "--" + UUID.randomUUID());
		java.nio.file.Files.createDirectories(tempDir);
		return java.nio.file.Files.createFile(tempFile);
	}
}
