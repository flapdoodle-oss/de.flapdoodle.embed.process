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
package de.flapdoodle.embed.process.transitions;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.types.ProcessArguments;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.ProcessEnv;
import de.flapdoodle.embed.process.types.RunningProcess;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
	public StateID<ExtractedFileSet> processExecutable() {
		return StateID.of(ExtractedFileSet.class);
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
		ExtractedFileSet fileSet = lookup.of(processExecutable());
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());

		try {
			Running running = start(fileSet.executable(), arguments, environment, processConfig, processOutput, supportConfig);
			return State.of(running, Running::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}

	private Running start(
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

			if (processConfig.daemonProcess()) {
				ProcessControl.addShutdownHook(() -> process.stop(processConfig.stopTimeoutInMillis()));
			}

			Processors.connect(process.getReader(), outputConfig.output());
			Processors.connect(process.getError(), StreamToLineProcessor.wrap(outputConfig.error()));

			return new Running(process, pidFile, processConfig.stopTimeoutInMillis());
		}
		catch (IOException iox) {
			Files.delete(pidFile);
			process.stop(processConfig.stopTimeoutInMillis());
			throw iox;
		}
	}

	public static class Running implements RunningProcess {

		private final ProcessControl process;
		private final Path pidFile;
		private final long timeout;
		public Running(ProcessControl process, Path pidFile, long timeout) {
			this.process = process;
			this.pidFile = pidFile;
			this.timeout = timeout;
		}

		protected void stop() {
			process.stop(timeout);
			Try.runable(() -> Files.delete(pidFile))
				.mapCheckedException(RuntimeException::new)
				.run();
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
		return executableFile.getParent().resolve(executableFile.getFileName().toString()+".pid");
	}

	private static File pidFile(File executableFile) {
		return new File(executableFile.getParentFile(), executableBaseName(executableFile.getName()) + ".pid");
	}

	private static void writePidFile(Path pidFile, long pid) throws IOException {
		Files.write(pidFile, Collections.singletonList("" + pid));
	}

	public static ImmutableStarter withDefaults() {
		return ImmutableStarter.builder().build();
	}
}
