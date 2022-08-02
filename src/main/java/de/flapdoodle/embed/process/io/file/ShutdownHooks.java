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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * see @{@link Files}
 */
@Deprecated
abstract class ShutdownHooks {
	private ShutdownHooks() {
		// no instance
	}

	public static final AtomicReference<Cleaner> cleanerRef=new AtomicReference<>();

	private synchronized static Cleaner lazyGetCleaner() {
		Cleaner ret = cleanerRef.get();
		if (ret==null) {
			ret=new Cleaner();
			startThreadAndAddHook(ret);
			cleanerRef.set(ret);
		}
		return ret;
	}

	private static void startThreadAndAddHook(Cleaner cleaner) {
		Thread thread = new Thread(() -> cleaner.clean());
		thread.setDaemon(true);
		thread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> cleaner.deleteFiles()));
	}

	public static void forceDeleteOnExit(Path path) {
		Cleaner cleaner = lazyGetCleaner();
		cleaner.add(path);
	}

	static class Cleaner {
		private static final int MAX_FILES_TO_CLEAN = 10000;
		private static final int MAX_RETRIES = 10;
		private static Logger logger = LoggerFactory.getLogger(Cleaner.class);

		private final Map<Path, Integer> fileToClean = new ConcurrentHashMap<>();

		public void clean() {
			while (true)
				try {
					synchronized (fileToClean) {
						fileToClean.wait(1000);
						deleteFiles();
					}
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
		}

		private void deleteFiles() {
			synchronized (fileToClean) {
				HashMap<Path, Integer> copy = new HashMap<>(fileToClean);
				for (Path f : copy.keySet()) {
					try {
						Files.forceDelete(f);
						fileToClean.remove(f);
						logger.info("Could delete " + f);
					}
					catch (IOException iox) {
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

		public void add(Path fileOrDir) {
			if (fileOrDir.toFile().exists()) {
				synchronized (fileToClean) {
					if (fileToClean.size() < MAX_FILES_TO_CLEAN) {
						Integer oldValue = fileToClean.putIfAbsent(fileOrDir, 0);
						if (oldValue != null) logger.error("forceDelete {}, but already in list with {} tries.", fileOrDir, oldValue);
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
