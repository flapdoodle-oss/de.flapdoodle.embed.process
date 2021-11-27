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
package de.flapdoodle.embed.processg.runtime;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public abstract class Starter implements Transition<Starter.Running>, HasLabel {

	@Override
	@Value.Default
	public String transitionLabel() {
		return "Starter";
	}

	@Value.Default
	public StateID<ProcessExecutable> processExecutable() {
		return StateID.of(ProcessExecutable.class);
	}

	@Value.Default
	public StateID<ProcessConfig> processConfig() {
		return StateID.of(ProcessConfig.class);
	}

	@Value.Default
	public StateID<ProcessEnv> processEnv() {
		return StateID.of(ProcessEnv.class);
	}

	@Value.Default
	public StateID<ProcessArguments> arguments() {
		return StateID.of(ProcessArguments.class);
	}

	@Value.Default
	public StateID<ProcessOutput> processOutput() {
		return StateID.of(ProcessOutput.class);
	}

	@Value.Default
	public StateID<SupportConfig> supportConfig() {
		return StateID.of(SupportConfig.class);
	}

	@Override
	@Value.Default
	public StateID<Running> destination() {
		return StateID.of(Running.class);
	}

	@Override
	public Set<StateID<?>> sources() {
		return StateID.setOf(
			processExecutable(),
			processConfig(),
			processEnv(),
			arguments(),
			processOutput(),
			supportConfig()
		);
	}

	@Override
	public State<Running> result(StateLookup lookup) {
		File executable = lookup.of(processExecutable()).value();
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());

		try {
			ProcessControl process = start(executable, arguments, environment, processConfig, processOutput, supportConfig);
			return State.of(new Running(process, processConfig.stopTimeoutInMillis()), Running::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}

	private ProcessControl start(
		File executable,
		List<String> arguments,
		Map<String, String> environment,
		ProcessConfig processConfig,
		ProcessOutput outputConfig,
		SupportConfig supportConfig
	)
		throws IOException {
		File pidFile = pidFile(executable);

		List<String> commandLine = Stream
			.concat(Stream.of(executable.getAbsolutePath()), arguments.stream())
			.collect(Collectors.toList());

		ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(commandLine, environment, true);
		ProcessControl process = ProcessControl.start(supportConfig, processBuilder);

		try {
			if (process.getPid() != null) {
				writePidFile(pidFile, process.getPid());
			}

			if (processConfig.daemonProcess()) {
				ProcessControl.addShutdownHook(() -> process.stop(processConfig.stopTimeoutInMillis()));
			}

			Processors.connect(process.getReader(), outputConfig.output());
			Processors.connect(process.getError(), StreamToLineProcessor.wrap(outputConfig.error()));
			return process;
		}
		catch (IOException iox) {
			process.stop(processConfig.stopTimeoutInMillis());
			throw iox;
		}
	}

	public static class Running implements RunningProcess {

		private final ProcessControl process;
		private final long timeout;
		public Running(ProcessControl process, long timeout) {
			this.process = process;
			this.timeout = timeout;
		}

		protected void stop() {
			process.stop(timeout);
		}
	}

	private static String executableBaseName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			return name.substring(0, idx);
		}
		return name;
	}

	private static File pidFile(File executableFile) {
		return new File(executableFile.getParentFile(), executableBaseName(executableFile.getName()) + ".pid");
	}

	private static void writePidFile(File pidFile, long pid) throws IOException {
		Files.write(pid + "\n", pidFile);
	}

	public static ImmutableStarter withDefaults() {
		return ImmutableStarter.builder().build();
	}
}
