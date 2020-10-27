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

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.runtime.ProcessControl;

public class CachingArtifactStore implements IArtifactStore {

	private static final Logger logger = LoggerFactory.getLogger(CachingArtifactStore.class);

	private final IArtifactStore delegate;

	private final Object lock = new Object();

	private final HashMap<Distribution, FilesWithCounter> distributionFiles = new HashMap<>();

	private final ScheduledExecutorService executor;

	public CachingArtifactStore(IArtifactStore delegate) {
		this.delegate = delegate;
		ProcessControl.addShutdownHook(new CacheCleaner());

		executor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory());
		executor.scheduleAtFixedRate(new RemoveUnused(), 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public Optional<ExtractedFileSet> extractFileSet(Distribution distribution) throws IOException {

		FilesWithCounter fileWithCounter;

		synchronized (lock) {
			fileWithCounter = distributionFiles.get(distribution);
			if (fileWithCounter == null) {
				logger.debug("cache NOT found for {}", distribution);
				fileWithCounter = new FilesWithCounter(distribution);
				distributionFiles.put(distribution, fileWithCounter);
			} else {
				logger.debug("cache found for {}", distribution);
			}
		}

		return fileWithCounter.use();
	}

	@Override
	public void removeFileSet(Distribution distribution, ExtractedFileSet executable) {
		FilesWithCounter fileWithCounter;
		synchronized (lock) {
			fileWithCounter = distributionFiles.get(distribution);
		}
		if (fileWithCounter != null) {
			fileWithCounter.free(executable);
		} else {
			logger.warn("Already removed {} for {}, emergency shutdown?", executable, distribution);
		}
	}

	protected void removeAll() {
		synchronized (lock) {
			for (FilesWithCounter fc : distributionFiles.values()) {
				fc.forceDelete();
			}
			distributionFiles.clear();
		}
	}

	public void removeUnused() {
		synchronized (lock) {
			for (FilesWithCounter fc : distributionFiles.values()) {
				fc.cleanup();
			}
		}
	}

	protected void shutdownExecutor() {
		executor.shutdown();
		try	{
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				for (Runnable r : executor.shutdownNow()) {
					logger.warn("Terminated job of type {}", r.getClass().getName());
				}
				if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
					logger.error("Executor did not terminate.");
				}
			}
			if (!executor.isShutdown()) {
				executor.shutdownNow();
			}
		} catch (InterruptedException ie)	{
			executor.shutdownNow();
		}
	}


	class FilesWithCounter {

		private Optional<ExtractedFileSet> file;
		private int counter =0;
		private final Distribution distribution;

		public FilesWithCounter(Distribution distribution) {
			this.distribution = distribution;
		}

		public synchronized void free(ExtractedFileSet executable) {
			if (file==null) throw new RuntimeException("nothing to free");
			if (executable!= file.orElse(null)) throw new RuntimeException("Files does not match: "+ file +" != "+executable);
			logger.debug("Free {} {}", counter, file);
			counter--;
		}

		public synchronized Optional<ExtractedFileSet> use() throws IOException {
			counter++;
			
			if (file ==null) {
				file = delegate.extractFileSet(distribution);
				logger.debug("Not Cached {} {}", counter, file);
			} else {
				logger.debug("Cached {} {}", counter, file);
			}
			return file;
		}
		
		public synchronized void cleanup() {
			if (counter <=0) {
				if (counter <0) logger.warn("Counter < 0 for {} and {}", distribution, file);
				if (file !=null) {
					logger.debug("cleanup for {} and {}", distribution, file);
					if (file.isPresent()) delegate.removeFileSet(distribution, file.get());
					file =null;
				}
			}
		}
		
		public synchronized void forceDelete() {
			if (file !=null) {
				logger.debug("force delete for {} and {}", distribution, file);
				if (file.isPresent()) delegate.removeFileSet(distribution, file.get());
				file =null;
				counter =0;
			}
		}
	}

	class RemoveUnused implements Runnable {

		@Override
		public void run() {
			CachingArtifactStore.this.removeUnused();
		}
		
	}
	
	class CacheCleaner implements Runnable {

		@Override
		public void run() {
			CachingArtifactStore.this.removeAll();
			CachingArtifactStore.this.shutdownExecutor();
		}
	}

	class CustomThreadFactory implements ThreadFactory {

		private final ThreadFactory factory=Executors.defaultThreadFactory();
		
		@Override
		public Thread newThread(Runnable runnable) {
			Thread ret = factory.newThread(runnable);
			ret.setDaemon(true);
			return ret;
		}
		
	}
}
