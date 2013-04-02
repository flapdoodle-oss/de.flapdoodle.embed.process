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
package de.flapdoodle.embed.process.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.ProcessControl;

public class CachingArtifactStore implements IArtifactStore {

	private static Logger _logger = Logger.getLogger(CachingArtifactStore.class.getName());

	private final IArtifactStore _delegate;

	Object _lock=new Object();
	
	HashMap<Distribution, FileWithCounter> _distributionFiles = new HashMap<Distribution, FileWithCounter>();

	public CachingArtifactStore(IArtifactStore delegate) {
		_delegate = delegate;
		ProcessControl.addShutdownHook(new CacheCleaner());
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new RemoveUnused(), 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		return _delegate.checkDistribution(distribution);
	}

	@Override
	public File extractExe(Distribution distribution) throws IOException {
		
		FileWithCounter fileWithCounter;
		
		synchronized (_lock) {
			fileWithCounter = _distributionFiles.get(distribution);
			if (fileWithCounter == null) {
				_logger.fine("cache NOT found for "+distribution);
				fileWithCounter=new FileWithCounter(distribution);
				_distributionFiles.put(distribution, fileWithCounter);
			} else {
				_logger.fine("cache found for "+distribution);
			}
		}
		
		return fileWithCounter.use();
	}

	@Override
	public void removeExecutable(Distribution distribution, File executable) {
		FileWithCounter fileWithCounter;
		synchronized (_lock) {
			fileWithCounter = _distributionFiles.get(distribution);
		}
		if (fileWithCounter!=null) {
			fileWithCounter.free(executable);
		} else {
			_logger.warning("Allready removed "+executable+" for "+distribution+", emergency shutdown?");
		}
	}

	protected void removeAll() {
		synchronized (_lock) {
			for (FileWithCounter fc : _distributionFiles.values()) {
				fc.forceDelete();
			}
			_distributionFiles.clear();
		}
	}
	
	public void removeUnused() {
		synchronized (_lock) {
			for (FileWithCounter fc : _distributionFiles.values()) {
				fc.cleanup();
			}
		}
	}


	class FileWithCounter {

		private File _file;
		int _counter=0;
		private final Distribution _distribution;

		public FileWithCounter(Distribution distribution) {
			_distribution = distribution;
		}

		public synchronized void free(File executable) {
			if (executable!=_file) throw new RuntimeException("Files does not match: "+_file+" != "+executable);
			_logger.fine("Free "+_counter+" "+_file);
			_counter--;
		}

		public synchronized File use() throws IOException {
			_counter++;
			
			if (_file==null) {
				_file=_delegate.extractExe(_distribution);
				_logger.fine("Not Cached "+_counter+" "+_file);
			} else {
				_logger.fine("Cached "+_counter+" "+_file);
			}
			return _file;
		}
		
		public synchronized void cleanup() {
			if (_counter<=0) {
				if (_counter<0) _logger.warning("Counter < 0 for "+_distribution+" and "+_file);
				if (_file!=null) {
					_logger.fine("cleanup for "+_distribution+" and "+_file);
					_delegate.removeExecutable(_distribution, _file);
					_file=null;
				}
			}
		}
		
		public synchronized void forceDelete() {
			if (_file!=null) {
				_logger.fine("force delete for "+_distribution+" and "+_file);
				_delegate.removeExecutable(_distribution, _file);
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
		}
	}


}
