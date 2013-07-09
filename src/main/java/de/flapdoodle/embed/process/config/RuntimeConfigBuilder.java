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
package de.flapdoodle.embed.process.config;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.IArtifactStore;

public class RuntimeConfigBuilder extends AbstractBuilder<IRuntimeConfig> {

	public RuntimeConfigBuilder artifactStore(AbstractBuilder<IArtifactStore> artifactStoreBuilder) {
		return artifactStore(artifactStoreBuilder.build());
	}
	
	public RuntimeConfigBuilder artifactStore(IArtifactStore artifactStore) {
		set(IArtifactStore.class, artifactStore);
		return this;
	}

	public RuntimeConfigBuilder processOutput(ProcessOutput processOutput) {
		set(ProcessOutput.class, processOutput);
		return this;
	}

	public RuntimeConfigBuilder daemonProcess(boolean daemonProcess) {
		set(Boolean.class, daemonProcess);
		return this;
	}

	public RuntimeConfigBuilder commandLinePostProcessor(ICommandLinePostProcessor commandLinePostProcessor) {
		set(ICommandLinePostProcessor.class, commandLinePostProcessor);
		return this;
	}

	@Override
	public IRuntimeConfig build() {
		IArtifactStore artifactStore = get(IArtifactStore.class);
		ProcessOutput processOutput = get(ProcessOutput.class);
		ICommandLinePostProcessor commandLinePostProcessor = get(ICommandLinePostProcessor.class);
		Boolean daemonProcess = get(Boolean.class, true);

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
