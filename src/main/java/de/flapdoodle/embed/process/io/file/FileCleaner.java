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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileCleaner {

	private static Logger logger = LoggerFactory.getLogger(FileCleaner.class);

	private static final int MAX_FILES_TO_CLEAN = 10000;
	private static final int MAX_RETRIES = 100;
	private static Cleaner cleaner;

	public synchronized static void forceDeleteOnExit(File fileOrDir) {
		//		FileUtils.forceDeleteOnExit(file);
		if (cleaner == null) {
			cleaner = new Cleaner();

			Thread cleanerThread = new Thread(new CleanerThreadRunner(cleaner));
			cleanerThread.setDaemon(true);
			cleanerThread.start();

			Runtime.getRuntime().addShutdownHook(new Thread(new CleanerShutdownHook(cleaner)));
		}

		cleaner.forceDelete(fileOrDir);
	}

	static class CleanerThreadRunner implements Runnable {

		private final Cleaner _cleaner;

		CleanerThreadRunner(Cleaner cleaner) {
			_cleaner = cleaner;
		}

		@Override
		public void run() {
			_cleaner.clean();
		}
	}

	static class CleanerShutdownHook implements Runnable {

		private final Cleaner _cleaner;

		CleanerShutdownHook(Cleaner cleaner) {
			_cleaner = cleaner;
		}

		@Override
		public void run() {
			_cleaner.deleteFiles();
		}

	}

	static class Cleaner {

		private final Map<File, Integer> fileToClean = new HashMap<>();

		public void clean() {
			while (true)
				try {
					synchronized (fileToClean) {
						fileToClean.wait(1000);
						deleteFiles();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
		}

		private void deleteFiles() {
			synchronized (fileToClean) {
				Map<File, Integer> copy = new HashMap<>(fileToClean);
				for (File f : copy.keySet()) {
					try {
						Files.forceDelete(f.toPath());
						fileToClean.remove(f);
						logger.info("Could delete " + f);
					} catch (IOException iox) {
						int newCounter = fileToClean.get(f) + 1;
						if (newCounter > MAX_RETRIES) {
							logger.error("Could not delete {} after {} retries, leave it unchanged", f, newCounter);
							fileToClean.remove(f);
						} else {
							fileToClean.put(f, newCounter);
						}
					}
				}
			}
		}

		public void forceDelete(File fileOrDir) {
			if (fileOrDir.exists()) {
				synchronized (fileToClean) {
					if (fileToClean.size() < MAX_FILES_TO_CLEAN) {
						Integer oldValue = fileToClean.put(fileOrDir, 0);
						if (oldValue!=null) logger.error("forceDelete {}, but already in list with {} tries.", fileOrDir, oldValue);
						fileToClean.notify();
					} else {
						throw new RuntimeException("filesToClean exceeded " + MAX_FILES_TO_CLEAN
								+ ", something is wrong here (tried to delete " + fileOrDir + ")");
					}
				}
			}
		}
	}

}
