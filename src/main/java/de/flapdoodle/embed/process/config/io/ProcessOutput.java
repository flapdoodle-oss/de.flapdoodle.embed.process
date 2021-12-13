package de.flapdoodle.embed.process.config.io;

import de.flapdoodle.embed.process.io.StreamProcessor;

/**
 * @see de.flapdoodle.embed.process.config.process.ProcessOutput#builder()
 */
@Deprecated
public class ProcessOutput implements de.flapdoodle.embed.process.config.process.ProcessOutput {

	private final StreamProcessor output;
	private final StreamProcessor error;
	private final StreamProcessor commands;

	public ProcessOutput(StreamProcessor output, StreamProcessor error, StreamProcessor commands) {
		this.output = output;
		this.error = error;
		this.commands = commands;
	}

	@Override
	public StreamProcessor output() {
		return output;
	}

	@Override
	public StreamProcessor error() {
		return error;
	}
	
	@Override
	public StreamProcessor commands() {
		return commands;
	}
}
