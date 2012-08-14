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
package de.flapdoodle.embed.nodejs;

import java.io.File;
import java.io.IOException;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.Executable;


public class NodejsExecutable extends Executable<NodejsConfig, NodejsProcess>{

	public NodejsExecutable(Distribution distribution, NodejsConfig config, IRuntimeConfig runtimeConfig, File executable) {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected NodejsProcess start(Distribution distribution, NodejsConfig config, IRuntimeConfig runtime)
			throws IOException {
		return new NodejsProcess(distribution, config, runtime, this);
	}

}
