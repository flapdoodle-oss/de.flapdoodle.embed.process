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

	public RuntimeConfigBuilder commandLinePostProcessor(ICommandLinePostProcessor commandLinePostProcessor) {
		set(ICommandLinePostProcessor.class, commandLinePostProcessor);
		return this;
	}

	@Override
	public IRuntimeConfig build() {
		IArtifactStore artifactStore = get(IArtifactStore.class);
		ProcessOutput processOutput = get(ProcessOutput.class);
		ICommandLinePostProcessor commandLinePostProcessor = get(ICommandLinePostProcessor.class);

		return new ImmutableRuntimeConfig(artifactStore, processOutput, commandLinePostProcessor);
	}

	static class ImmutableRuntimeConfig implements IRuntimeConfig {

		private final ProcessOutput _processOutput;
		private final ICommandLinePostProcessor _commandLinePostProcessor;
		private final IArtifactStore _artifactStore;

		public ImmutableRuntimeConfig(IArtifactStore artifactStore, ProcessOutput processOutput,
				ICommandLinePostProcessor commandLinePostProcessor) {
			super();
			_artifactStore = artifactStore;
			_processOutput = processOutput;
			_commandLinePostProcessor = commandLinePostProcessor;
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

	}
}
