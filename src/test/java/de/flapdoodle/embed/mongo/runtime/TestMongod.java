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
package de.flapdoodle.embed.mongo.runtime;

import de.flapdoodle.embed.mongo.runtime.Mongod;
import junit.framework.TestCase;

//CHECKSTYLE:OFF
public class TestMongod extends TestCase {

	public void testGetPID() {
		String consoleOutput = "Fri Apr 27 08:08:55 BackgroundJob starting: DataFileSync\n" +
				"Fri Apr 27 08:08:55 versionCmpTest passed\n" +
				"Fri Apr 27 08:08:55 versionArrayTest passed\n" +
				"Fri Apr 27 08:08:55 [initandlisten] MongoDB starting : pid=11026 port=12345 dbpath=/tmp/embedmongo-db-78b0fc63-31d2-4741-aa15-8fdc4deeda68 32-bit host=mub001\n" +
				"Fri Apr 27 08:08:55 [initandlisten]\n" +
				"Fri Apr 27 08:08:55 [initandlisten] ** NOTE: when using MongoDB 32 bit, you are limited to about 2 gigabytes of data\n";

		assertEquals("PID", 11026, Mongod.getMongodProcessId(consoleOutput, -1));
	}
}
