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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.directories.PlatformTempDir;

public class TestFileCleaner extends TestCase {

	private static Logger logger = LoggerFactory.getLogger(TestFileCleaner.class.getName());

	String prefix = UUID.randomUUID().toString();
	
	public void testCleanup() throws IOException, InterruptedException {

		boolean runsOnWindows = Platform.detect() == Platform.Windows;

		List<File> files = new ArrayList<File>();

		logger.info("create temp files");

		for (int i = 0; i < 10; i++) {
			files.add(Files.createTempFile(new PlatformTempDir(), "fileCleanerTest-" + prefix + "-some-" + i));
		}

		List<FileLock> locks = new ArrayList<FileLock>();

		logger.info("lock temp files");

		for (File file : files) {
			FileLock lock = lock(file);
			if (lock == null)
				throw new RuntimeException("Could not get lock for " + file);
			locks.add(lock);
		}

		logger.info("try to delete temp files");

		for (File file : files) {
			Thread.sleep(100);
			FileCleaner.forceDeleteOnExit(file);
		}

		logger.info("after try to delete temp files (wait a little)");

		if (runsOnWindows) {

			Thread.sleep(1000);

			logger.info("check if temp files there (should be)");

			for (File file : files) {
				assertTrue("File " + file + " exists", file.exists());
			}
		}

		logger.info("release lock ");

		for (FileLock lock : locks) {
			release(lock);
		}

		logger.info("after release lock (wait a little)");

		Thread.sleep(2000);

		logger.info("check if temp files there (should NOT be)");

		for (File file : files) {
			assertFalse("File " + file + " exists", file.exists());
		}
	}
	
	public void testMultipleFiles() throws IOException {
		
		List<File> files=new ArrayList<File>();
		
		File lastFile = Files.createTempFile(new PlatformTempDir(), "fileCleanerTest-" + prefix + "-some-final");
		for (int i=0;i<10000;i++) {
			FileCleaner.forceDeleteOnExit(lastFile);
		}
		
		for (int i = 0; i < 100; i++) {
			files.add(Files.createTempFile(new PlatformTempDir(), "fileCleanerTest-" + prefix + "-some-" + i));
		}
		
		for (File file : files) {
			FileCleaner.forceDeleteOnExit(file);
		}
	}

	public FileLock lock(File file) throws IOException {
		FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
		return channel.tryLock();
	}

	public void release(FileLock lock) throws IOException {
		lock.release();
		lock.channel().close();
	}

}
