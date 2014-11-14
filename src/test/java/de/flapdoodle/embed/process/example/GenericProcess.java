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
package de.flapdoodle.embed.process.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.AbstractProcess;

public class GenericProcess extends AbstractProcess<GenericProcessConfig, GenericExecuteable, GenericProcess> {

	public GenericProcess(Distribution distribution, GenericProcessConfig config, IRuntimeConfig runtime, GenericExecuteable genericExecuteable) throws IOException {
		super(distribution,config,runtime,genericExecuteable);
	}

	@Override
	public void stopInternal() {
		stopProcess();
	}
	
	@Override
	protected void cleanupInternal() {
		
	}

	@Override
	protected List<String> getCommandLine(Distribution distribution, GenericProcessConfig config, IExtractedFileSet files)
			throws IOException {
		// TODO how to config this?
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(files.executable().getAbsolutePath());
		ret.add("--help");
		return ret;
	}
}