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

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.ProcessControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class CachingArtifactStore implements IArtifactStore {

	private static Logger _logger = LoggerFactory.getLogger(CachingArtifactStore.class);

	private final IArtifactStore _delegate;

	Object _lock = new Object();

	HashMap<Distribution, FilesWithCounter> _distributionFiles = new HashMap<Distribution, FilesWithCounter>();

	private final ScheduledExecutorService executor;

	public CachingArtifactStore(IArtifactStore delegate) {
		_delegate = delegate;
		ProcessControl.addShutdownHook(new CacheCleaner());

		executor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory());
		executor.scheduleAtFixedRate(new RemoveUnused(), 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		return _delegate.checkDistribution(distribution);
	}

	@Override
	public IExtractedFileSet extractFileSet(Distribution distribution) throws IOException {

		FilesWithCounter fileWithCounter;

		synchronized (_lock) {
			fileWithCounter = _distributionFiles.get(distribution);
			if (fileWithCounter == null) {
				_logger.debug("cache NOT found for {}", distribution);
				fileWithCounter = new FilesWithCounter(distribution);
				_distributionFiles.put(distribution, fileWithCounter);
			} else {
				_logger.debug("cache found for {}", distribution);
			}
		}

		return fileWithCounter.use();
	}

	@Override
	public void removeFileSet(Distribution distribution, IExtractedFileSet executable) {
		FilesWithCounter fileWithCounter;
		synchronized (_lock) {
			fileWithCounter = _distributionFiles.get(distribution);
		}
		if (fileWithCounter != null) {
			fileWithCounter.free(executable);
		} else {
			_logger.warn("Already removed {} for {}, emergency shutdown?", executable, distribution);
		}
	}

	protected void removeAll() {
		synchronized (_lock) {
			for (FilesWithCounter fc : _distributionFiles.values()) {
				fc.forceDelete();
			}
			_distributionFiles.clear();
		}
	}

	public void removeUnused() {
		synchronized (_lock) {
			for (FilesWithCounter fc : _distributionFiles.values()) {
				fc.cleanup();
			}
		}
	}

	protected void shutdownExecutor() {
		executor.shutdown();
		try	{
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				for (Runnable r : executor.shutdownNow()) {
					_logger.warn("Terminated job of type {}", r.getClass().getName());
				}
				if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
					_logger.error("Executor did not terminate.");
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

		private IExtractedFileSet _file;
		int _counter=0;
		private final Distribution _distribution;

		public FilesWithCounter(Distribution distribution) {
			_distribution = distribution;
		}

		public synchronized void free(IExtractedFileSet executable) {
			if (executable!=_file) throw new RuntimeException("Files does not match: "+_file+" != "+executable);
			_logger.debug("Free {} {}", _counter, _file);
			_counter--;
		}

		public synchronized IExtractedFileSet use() throws IOException {
			_counter++;
			
			if (_file==null) {
				_file=_delegate.extractFileSet(_distribution);
				_logger.debug("Not Cached {} {}", _counter, _file);
			} else {
				_logger.debug("Cached {} {}", _counter, _file);
			}
			return _file;
		}
		
		public synchronized void cleanup() {
			if (_counter<=0) {
				if (_counter<0) _logger.warn("Counter < 0 for {} and {}", _distribution, _file);
				if (_file!=null) {
					_logger.debug("cleanup for {} and {}", _distribution, _file);
					_delegate.removeFileSet(_distribution, _file);
					_file=null;
				}
			}
		}
		
		public synchronized void forceDelete() {
			if (_file!=null) {
				_logger.debug("force delete for {} and {}", _distribution, _file);
				_delegate.removeFileSet(_distribution, _file);
				_file=null;
				_counter=0;
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

		ThreadFactory factory=Executors.defaultThreadFactory();
		
		@Override
		public Thread newThread(Runnable runnable) {
			Thread ret = factory.newThread(runnable);
			ret.setDaemon(true);
			return ret;
		}
		
	}
}
