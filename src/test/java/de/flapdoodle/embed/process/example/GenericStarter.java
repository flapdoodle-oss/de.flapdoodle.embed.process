package de.flapdoodle.embed.process.example;

import java.io.File;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.Starter;

public class GenericStarter extends Starter<GenericProcessConfig, GenericExecuteable, GenericProcess> {

	GenericStarter(IRuntimeConfig config) {
		super(config);
	}

	@Override
	protected GenericExecuteable newExecutable(GenericProcessConfig config, Distribution distribution,
			IRuntimeConfig runtimeConfig, File executable) {
		return new GenericExecuteable(distribution, config, runtimeConfig, executable);
	}
}