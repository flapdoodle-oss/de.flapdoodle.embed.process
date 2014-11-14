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
package de.flapdoodle.embed.process.config;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.IArtifactStore;

public class RuntimeConfigBuilder extends AbstractBuilder<IRuntimeConfig> { 
	
	private static final TypedProperty<IArtifactStore> ARTIFACT_STORE = TypedProperty.with("ArtifactStore", IArtifactStore.class);
	private static final TypedProperty<ProcessOutput> PROCESS_OUTPUT = TypedProperty.with("ProcessOutput", ProcessOutput.class);
	private static final TypedProperty<ICommandLinePostProcessor> CMD_POSTPROCESSOR = TypedProperty.with("CommandLinePostProcessor", ICommandLinePostProcessor.class);
	private static final TypedProperty<Boolean> DAEMON_PROCESS = TypedProperty.with("DaemonProcess", Boolean.class);

	public RuntimeConfigBuilder artifactStore(AbstractBuilder<IArtifactStore> artifactStoreBuilder) {
		return artifactStore(artifactStoreBuilder.build());
	}
	
	public RuntimeConfigBuilder artifactStore(IArtifactStore artifactStore) {
		set(ARTIFACT_STORE, artifactStore);
		return this;
	}

	protected IProperty<IArtifactStore> artifactStore() {
		return property(ARTIFACT_STORE);
	}
	
	public RuntimeConfigBuilder processOutput(ProcessOutput processOutput) {
		set(PROCESS_OUTPUT, processOutput);
		return this;
	}

	protected IProperty<ProcessOutput> processOutput() {
		return property(PROCESS_OUTPUT);
	}
	

	public RuntimeConfigBuilder commandLinePostProcessor(ICommandLinePostProcessor commandLinePostProcessor) {
		set(CMD_POSTPROCESSOR, commandLinePostProcessor);
		return this;
	}

	protected IProperty<ICommandLinePostProcessor> commandLinePostProcessor() {
		return property(CMD_POSTPROCESSOR);
	}
	
	public RuntimeConfigBuilder daemonProcess(boolean daemonProcess) {
		set(DAEMON_PROCESS, daemonProcess);
		return this;
	}
	
	protected IProperty<Boolean> daemonProcess() {
		return property(DAEMON_PROCESS);
	}
	

	@Override
	public IRuntimeConfig build() {
		IArtifactStore artifactStore = get(ARTIFACT_STORE);
		ProcessOutput processOutput = get(PROCESS_OUTPUT);
		ICommandLinePostProcessor commandLinePostProcessor = get(CMD_POSTPROCESSOR);
		Boolean daemonProcess = get(DAEMON_PROCESS, true);

		return new ImmutableRuntimeConfig(artifactStore, processOutput, commandLinePostProcessor, daemonProcess);
	}

	static class ImmutableRuntimeConfig implements IRuntimeConfig {

		private final ProcessOutput _processOutput;
		private final ICommandLinePostProcessor _commandLinePostProcessor;
		private final IArtifactStore _artifactStore;
		private final boolean _daemonProcess;

		public ImmutableRuntimeConfig(IArtifactStore artifactStore, ProcessOutput processOutput,
				ICommandLinePostProcessor commandLinePostProcessor, boolean daemonProcess) {
			super();
			_artifactStore = artifactStore;
			_processOutput = processOutput;
			_commandLinePostProcessor = commandLinePostProcessor;
			_daemonProcess = daemonProcess;
		}

		@Override
		public ProcessOutput getProcessOutput() {
			return _processOutput;
		}

		@Override
		public ICommandLinePostProcessor getCommandLinePostProcessor() {
			return _commandLinePostProcessor;
		}

		@Override
		public IArtifactStore getArtifactStore() {
			return _artifactStore;
		}

		@Override
		public boolean isDaemonProcess() {
			return _daemonProcess;
		}

	}
}
