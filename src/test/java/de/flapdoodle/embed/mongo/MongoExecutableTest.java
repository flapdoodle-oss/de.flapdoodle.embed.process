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
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Integration test for starting and stopping MongodExecutable
 *
 * @author m.joehren
 */
//CHECKSTYLE:OFF
public class MongoExecutableTest extends TestCase {

	private static final Logger _logger = Logger.getLogger(MongoExecutableTest.class.getName());

	@Test
	public void testStartStopTenTimesWithNewMongoExecutable() throws IOException {
		boolean useMongodb = true;
		int loops = 10;

		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, 12345,
				Network.localhostIsIPv6());

		for (int i = 0; i < loops; i++) {
			_logger.info("Loop: " + i);

			MongodExecutable mongodExe = MongodStarter.getDefaultInstance().prepare(mongodConfig);
			MongodProcess mongod = mongodExe.start();

			if (useMongodb) {
				Mongo mongo = new Mongo(new ServerAddress(Network.getLocalHost(), mongodConfig.getPort()));
				DB db = mongo.getDB("test");
				DBCollection col = db.createCollection("testCol", new BasicDBObject());
				col.save(new BasicDBObject("testDoc", new Date()));
			}

			mongod.stop();
			mongodExe.cleanup();
		}

	}


}
