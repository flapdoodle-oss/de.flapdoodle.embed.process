package de.flapdoodle.embed.process.example;

import de.flapdoodle.embed.process.config.ISupportConfig;

public class GenericSupportConfig implements ISupportConfig {

	@Override
	public String getName() {
		return "generic";
	}

	@Override
	public String getSupportUrl() {
		return "https://github.com/flapdoodle-oss/de.flapdoodle.embed.process";
	}
	
}