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

import java.io.IOException;

import de.flapdoodle.embed.process.distribution.IVersion;

public class Nodejs {

	private Nodejs() {
		// singleton
	}

	public static void call(IVersion version, String filename, String workingDirectory) throws IOException {
		NodejsRuntimeConfig runtimeConfig = new NodejsRuntimeConfig();
		
		call(version, runtimeConfig, filename, workingDirectory);
	}

	public static void call(IVersion version, NodejsRuntimeConfig runtimeConfig, String filename, String workingDirectory)
			throws IOException {
		NodejsProcess node = null;
		NodejsConfig nodejsConfig = new NodejsConfig(version, filename, workingDirectory);
		NodejsStarter runtime = new NodejsStarter(runtimeConfig);

		try {
			NodejsExecutable nodeExecutable = runtime.prepare(nodejsConfig);
			node = nodeExecutable.start();
			node.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (node != null)
				node.stop();
		}
	}
}
