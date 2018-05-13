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
package de.flapdoodle.embed.process.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;

public abstract class Executable<T extends IExecutableProcessConfig, P extends IStopable> implements IStopable {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final T config;
	private final RuntimeConfig runtimeConfig;
	private final ExtractedFileSet executable;
	private boolean stopped;
	private boolean registeredJobKiller;
	
	List<IStopable> stopables = new ArrayList<>();

	private final Distribution distribution;

	public Executable(Distribution distribution, T config, RuntimeConfig runtimeConfig, ExtractedFileSet executable) {
		this.distribution = distribution;
		this.config = config;
		this.runtimeConfig = runtimeConfig;
		this.executable = executable;
		// only add shutdown hook for daemon processes,
		// clis being invoked will usually die by themselves
		if (runtimeConfig.isDaemonProcess()) {
			ProcessControl.addShutdownHook(new JobKiller());
			registeredJobKiller = true;
		}
	}
	
	@Override
    public boolean isRegisteredJobKiller() {
        return registeredJobKiller;
    }

    /**
	 * use stop (this calls stop anyway)
	 */
	@Deprecated
	public synchronized void cleanup() {
		stop();
	}

	public synchronized void stop() {
		if (!stopped) {
			for (IStopable s : stopables) {
				s.stop();
			}
			stopables = new ArrayList<>();

			runtimeConfig.getArtifactStore().removeFileSet(distribution, executable);

			stopped = true;
		}
	}

	/**
	 *
	 */
	class JobKiller implements Runnable {

		@Override
		public void run() {
			stop();
		}
	}

	public ExtractedFileSet getFile() {
		return executable;
	}

	public synchronized P start() throws IOException {
		if (stopped) throw new RuntimeException("Already stopped");

		P start = start(distribution, config, runtimeConfig);
		logger.info("start {}", config);
		addStopable(start);
		return start;
	}

	private void addStopable(P start) {
		stopables.add(start);
	}

	protected abstract P start(Distribution distribution, T config, RuntimeConfig runtime) throws IOException;

}
