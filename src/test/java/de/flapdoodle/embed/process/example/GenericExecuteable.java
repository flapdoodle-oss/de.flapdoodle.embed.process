package de.flapdoodle.embed.process.example;

import java.io.File;
import java.io.IOException;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.Executable;

public class GenericExecuteable extends Executable<GenericProcessConfig, GenericProcess> {

	public GenericExecuteable(Distribution distribution, GenericProcessConfig config, IRuntimeConfig runtimeConfig,
			File executable) {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected GenericProcess start(Distribution distribution, GenericProcessConfig config, IRuntimeConfig runtime)
			throws IOException {
		return new GenericProcess(distribution,config,runtime,this);
	}
	
}