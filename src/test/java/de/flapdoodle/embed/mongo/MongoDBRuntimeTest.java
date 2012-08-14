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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.MongodProcessOutputConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// CHECKSTYLE:OFF
public class MongoDBRuntimeTest extends TestCase {

	public void testNothing() {

	}

	public void testDistributions() throws IOException {
		MongodStarter runtime = MongodStarter.getDefaultInstance();
		check(runtime, new Distribution(Version.Main.V1_8, Platform.Linux, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V1_8, Platform.Windows, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V1_8, Platform.OS_X, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_0, Platform.Linux, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_0, Platform.Windows, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_0, Platform.OS_X, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_1, Platform.Linux, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_1, Platform.Windows, BitSize.B32));
		check(runtime, new Distribution(Version.Main.V2_1, Platform.OS_X, BitSize.B32));
	}

	private void check(MongodStarter runtime, Distribution distribution) throws IOException {
		assertTrue("Check", runtime.checkDistribution(distribution));
		File mongod = runtime.extractExe(distribution);
		assertNotNull("Extracted", mongod);
		assertTrue("Delete", mongod.delete());
	}

	public void testCheck() throws IOException, InterruptedException {

		Timer timer = new Timer();

		int port = 12345;
		MongodProcess mongodProcess = null;
		MongodExecutable mongod = null;
		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.setProcessOutput(MongodProcessOutputConfig.getDefaultInstance());
		//		runtimeConfig.setExecutableNaming(new UserTempNaming());
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

		timer.check("After Runtime");

		try {
			mongod = runtime.prepare(new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6()));
			timer.check("After mongod");
			assertNotNull("Mongod", mongod);
			mongodProcess = mongod.start();
			timer.check("After mongodProcess");

			Mongo mongo = new Mongo("localhost", port);
			timer.check("After Mongo");
			DB db = mongo.getDB("test");
			timer.check("After DB test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			timer.check("After Collection testCol");
			col.save(new BasicDBObject("testDoc", new Date()));
			timer.check("After save");

		} finally {
			if (mongodProcess != null)
				mongodProcess.stop();
			timer.check("After mongodProcess stop");
			if (mongod != null)
				mongod.cleanup();
			timer.check("After mongod cleanup");
		}
		timer.log();
	}

	static class Timer {

		long _start = System.currentTimeMillis();
		long _last = _start;

		List<String> _log = new ArrayList<String>();

		void check(String label) {
			long current = System.currentTimeMillis();
			long diff = current - _last;
			_last = current;

			_log.add(label + ": " + diff + "ms");
		}

		void log() {
			for (String line : _log) {
				System.out.println(line);
			}
		}
	}

}
