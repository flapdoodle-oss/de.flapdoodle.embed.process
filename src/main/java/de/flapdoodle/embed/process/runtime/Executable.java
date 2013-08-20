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
package de.flapdoodle.embed.process.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;

public abstract class Executable<T extends IExecutableProcessConfig, P extends IStopable> implements IStopable {

	private static Logger logger = Logger.getLogger(Executable.class.getName());

	private final T config;
	private final IRuntimeConfig runtimeConfig;
	private final File executable;
	private boolean stopped;

	List<IStopable> stopables = new ArrayList<IStopable>();

	private final Distribution distribution;

	public Executable(Distribution distribution, T config, IRuntimeConfig runtimeConfig, File executable) {
		this.distribution = distribution;
		this.config = config;
		this.runtimeConfig = runtimeConfig;
		this.executable = executable;
		ProcessControl.addShutdownHook(new JobKiller());
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
			stopables = new ArrayList<IStopable>();

			runtimeConfig.getArtifactStore().removeExecutable(distribution, executable);

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

	public File getFile() {
		return executable;
	}

	public synchronized P start() throws IOException {
		if (stopped)
			throw new RuntimeException("Allready stopped");

		P start = start(distribution, config, runtimeConfig);
		addStopable(start);
		return start;
	}

	private void addStopable(P start) {
		stopables.add(start);
	}

	protected abstract P start(Distribution distribution, T config, IRuntimeConfig runtime) throws IOException;

}
