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
import de.flapdoodle.embed.process.config.process.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.ReaderProcessor;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RunningProcess {
	int stop();

	static Runnable connectIOTo(ProcessControl process, ProcessOutput processOutput) {
		ReaderProcessor outputReader = Processors.connect(process.getReader(), processOutput.output());
		ReaderProcessor errorReader = Processors.connect(process.getError(), StreamToLineProcessor.wrap(processOutput.error()));

		return () -> ReaderProcessor.abortAll(outputReader, errorReader);
	}

	static <T extends RunningProcess> T start(
		RunningProcessFactory<T> runningProcessFactory,
		Path workingDir,
		Path executable,
		List<String> arguments,
		Map<String, String> environment,
		ProcessConfig processConfig,
		ProcessOutput outputConfig,
		SupportConfig supportConfig
	)
		throws IOException {
		Path pidFile = pidFile(workingDir, executable);

		List<String> commandLine = Stream
			.concat(Stream.of(executable.toFile().getAbsolutePath()), arguments.stream())
			.collect(Collectors.toList());

		ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(commandLine, environment, true)
			.directory(workingDir.toFile());
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

	static String executableBaseName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			return name.substring(0, idx);
		}
		return name;
	}

	static Path pidFile(Path workingDir, Path executableFile) {
		return workingDir.resolve(executableBaseName(executableFile.getFileName().toString()) + ".pid");
	}

	static void writePidFile(Path pidFile, long pid) throws IOException {
		Files.write(pidFile, Collections.singletonList("" + pid));
	}
}
