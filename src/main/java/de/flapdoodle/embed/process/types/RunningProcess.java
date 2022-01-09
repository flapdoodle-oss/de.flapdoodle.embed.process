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
package de.flapdoodle.embed.process.types;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.ReaderProcessor;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.types.Try;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunningProcess {
	private final ProcessControl process;
	private final Path pidFile;
	private final long timeout;
	private final Runnable onStop;

	public RunningProcess(ProcessControl process, Path pidFile, long timeout, Runnable onStop) {
		this.process = process;
		this.pidFile = pidFile;
		this.timeout = timeout;
		this.onStop = onStop;
	}

	public RunningProcess(ProcessControl process, ProcessOutput processOutput, Path pidFile, long timeout) {
		this(process, pidFile, timeout, connectIOTo(process, processOutput));
	}

	private static Runnable connectIOTo(ProcessControl process, ProcessOutput processOutput) {
		ReaderProcessor outputReader = Processors.connect(process.getReader(), processOutput.output());
		ReaderProcessor errorReader = Processors.connect(process.getError(), StreamToLineProcessor.wrap(processOutput.error()));

		return () -> ReaderProcessor.abortAll(outputReader, errorReader);
	}

	public void stop() {
		try {
			process.stop(timeout);
		} finally {
			try {
				onStop.run();
			} finally {
				Try.runable(() -> Files.delete(pidFile))
					.mapCheckedException(RuntimeException::new)
					.run();
			}
		}
	}

	public static <T extends RunningProcess> T start(
		RunningProcessFactory<T> runningProcessFactory,
		Path executable,
		List<String> arguments,
		Map<String, String> environment,
		ProcessConfig processConfig,
		ProcessOutput outputConfig,
		SupportConfig supportConfig
	)
		throws IOException {
		Path pidFile = pidFile(executable);

		List<String> commandLine = Stream
			.concat(Stream.of(executable.toFile().getAbsolutePath()), arguments.stream())
			.collect(Collectors.toList());

		ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(commandLine, environment, true);
		ProcessControl process = ProcessControl.start(supportConfig, processBuilder);

		try {
			if (process.getPid() != null) {
				writePidFile(pidFile, process.getPid());
			}

			T running = runningProcessFactory.startedWith(process, outputConfig, pidFile, processConfig.stopTimeoutInMillis());

			if (processConfig.daemonProcess()) {
				ProcessControl.addShutdownHook(running::stop);
			}
			return running;
		}
		catch (IOException iox) {
			Files.delete(pidFile);
			process.stop(processConfig.stopTimeoutInMillis());
			throw iox;
		}
	}

	private static String executableBaseName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			return name.substring(0, idx);
		}
		return name;
	}

	private static Path pidFile(Path executableFile) {
		return executableFile.getParent().resolve(executableBaseName(executableFile.getFileName().toString()) + ".pid");
	}

	private static void writePidFile(Path pidFile, long pid) throws IOException {
		Files.write(pidFile, Collections.singletonList("" + pid));
	}
}
