/*
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
package de.flapdoodle.embed.process.types;

import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.types.Try;

import java.nio.file.Files;
import java.nio.file.Path;

public class RunningProcessImpl implements RunningProcess {
	private final ProcessControl process;
	private final Path pidFile;
	private final long timeout;
	private final Runnable onStop;

	public RunningProcessImpl(ProcessControl process, Path pidFile, long timeout, Runnable onStop) {
		this.process = process;
		this.pidFile = pidFile;
		this.timeout = timeout;
		this.onStop = onStop;
	}

	public RunningProcessImpl(ProcessControl process, ProcessOutput processOutput, Path pidFile, long timeout) {
		this(process, pidFile, timeout, RunningProcess.connectIOTo(process, processOutput));
	}

	@Override
	public int stop() {
		try {
			return process.stop(timeout);
		} finally {
			try {
				onStop.run();
			} finally {
				Try.runable(() -> Files.delete(pidFile))
					.mapException(RuntimeException::new)
					.run();
			}
		}
	}

	public boolean isAlive() {
		return process.isAlive();
	}
}
