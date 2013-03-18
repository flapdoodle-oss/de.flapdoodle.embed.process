package de.flapdoodle.embed.process.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.AbstractProcess;

public class GenericProcess extends AbstractProcess<GenericProcessConfig, GenericExecuteable, GenericProcess> {

	public GenericProcess(Distribution distribution, GenericProcessConfig config, IRuntimeConfig runtime, GenericExecuteable genericExecuteable) throws IOException {
		super(distribution,config,runtime,genericExecuteable);
	}

	@Override
	public void stop() {
		stopProcess();
	}

	@Override
	protected List<String> getCommandLine(Distribution distribution, GenericProcessConfig config, File exe)
			throws IOException {
		// TODO how to config this?
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(exe.getAbsolutePath());
		ret.add("--help");
		return ret;
	}

	@Override
	protected ISupportConfig supportConfig() {
		return new GenericSupportConfig();
	}
}