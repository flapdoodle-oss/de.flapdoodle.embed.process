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

import junit.framework.TestCase;
import de.flapdoodle.embed.process.io.file.Files;

public class NodejsStarterTest extends TestCase {

	public void testNodejs() throws IOException, InterruptedException {
		final File helloWorld = Files.createTempFile("node-hello-world.js");
		Files.write("console.log(\"Running Hello World inside NodeJS in \"+process.cwd());setTimeout(function(){},1000);", helloWorld);
		try {
			Nodejs.call(NodejsVersion.V0_8_6, helloWorld.getAbsolutePath(), "/tmp");
		} finally {
			Files.forceDelete(helloWorld);
		}

	}
}
