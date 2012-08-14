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
package de.flapdoodle.embed.mongo.examples;

import junit.framework.TestCase;

import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public abstract class AbstractMongoDBTest extends TestCase {

	private MongodExecutable _mongodExe;
	private MongodProcess _mongod;

	private Mongo _mongo;
	@Override
	protected void setUp() throws Exception {

		MongodStarter runtime = MongodStarter.getDefaultInstance();
		_mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, 12345, Network.localhostIsIPv6()));
		_mongod = _mongodExe.start();

		super.setUp();

		_mongo = new Mongo("localhost", 12345);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		_mongod.stop();
		_mongodExe.cleanup();
	}

	public Mongo getMongo() {
		return _mongo;
	}

}
