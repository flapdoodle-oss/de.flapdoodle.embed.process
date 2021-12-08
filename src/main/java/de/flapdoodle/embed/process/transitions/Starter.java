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

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import org.immutables.builder.Builder;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public abstract class Starter<T extends RunningProcess> implements Transition<RunningProcess>, HasLabel {

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
	public StateID<RunningProcess> destination() {
		return StateID.of(RunningProcess.class);
	}

	@Builder.Parameter
	protected abstract RunningProcessFactory<T> runningProcessFactory();

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
	public State<RunningProcess> result(StateLookup lookup) {
		ExtractedFileSet fileSet = lookup.of(processExecutable());
		List<String> arguments = lookup.of(arguments()).value();
		Map<String, String> environment = lookup.of(processEnv()).value();
		ProcessConfig processConfig = lookup.of(processConfig());
		ProcessOutput processOutput = lookup.of(processOutput());
		SupportConfig supportConfig = lookup.of(supportConfig());

		try {
			RunningProcess running = start(fileSet.executable(), arguments, environment, processConfig, processOutput, supportConfig);
			return State.of(running, RunningProcess::stop);
		}
		catch (IOException ix) {
			throw new RuntimeException("could not start process", ix);
		}
	}

	private T start(
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

			T running = runningProcessFactory().startedWith(process, outputConfig, pidFile, processConfig.stopTimeoutInMillis());
			
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
		return executableFile.getParent().resolve(executableBaseName(executableFile.getFileName().toString())+".pid");
	}

	private static void writePidFile(Path pidFile, long pid) throws IOException {
		Files.write(pidFile, Collections.singletonList("" + pid));
	}

	public static <T extends RunningProcess> ImmutableStarter.Builder<T> with(RunningProcessFactory<T> runningProcessFactory) {
		return ImmutableStarter.builder(runningProcessFactory);
	}

	public static ImmutableStarter<RunningProcess> withDefaults() {
		return with(RunningProcess::withConnectedOutput).build();
	}
}
