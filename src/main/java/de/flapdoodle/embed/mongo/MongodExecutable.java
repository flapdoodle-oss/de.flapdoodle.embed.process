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
package de.flapdoodle.embed.mongo;

import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.MongodProcessOutputConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.Executable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 */
public class MongodExecutable extends Executable<MongodConfig, MongodProcess> {

	public MongodExecutable(Distribution distribution, MongodConfig mongodConfig, IRuntimeConfig runtimeConfig,
			File mongodExecutable) {
		super(distribution, mongodConfig, runtimeConfig, mongodExecutable);
	}

	private static Logger logger = Logger.getLogger(MongodExecutable.class.getName());

	@Override
	protected MongodProcess start(Distribution distribution, MongodConfig config, IRuntimeConfig runtime)
			throws IOException {
		return new MongodProcess(distribution, config, runtime, this);
	}

}
