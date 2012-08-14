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
package de.flapdoodle.embed.mongo.tests;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.distribution.IVersion;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * This class encapsulates everything that would be needed to do embedded
 * MongoDB testing.
 */
public class MongodForTestsFactory {

	private static Logger logger = Logger.getLogger(MongodForTestsFactory.class
			.getName());

	public static MongodForTestsFactory with(final IVersion version)
			throws IOException {
		return new MongodForTestsFactory(version);
	}

	private final MongodExecutable mongodExecutable;

	private final MongodProcess mongodProcess;

	/**
	 * Create the testing utility using the latest production version of
	 * MongoDB.
	 * 
	 * @throws IOException
	 */
	public MongodForTestsFactory() throws IOException {
		this(Version.Main.V2_0);
	}

	/**
	 * Create the testing utility using the specified version of MongoDB.
	 * 
	 * @param version
	 *            version of MongoDB.
	 */
	public MongodForTestsFactory(final IVersion version) throws IOException {

		final MongodStarter runtime = MongodStarter.getInstance(RuntimeConfig
				.getInstance(logger));
		mongodExecutable = runtime.prepare(new MongodConfig(version, Network
				.getFreeServerPort(), Network.localhostIsIPv6()));
		mongodProcess = mongodExecutable.start();

	}

	/**
	 * Creates a new Mongo connection.
	 * 
	 * @throws MongoException
	 * @throws UnknownHostException
	 */
	public Mongo newMongo() throws UnknownHostException, MongoException {
		return new Mongo(new ServerAddress(Network.getLocalHost(),
				mongodProcess.getConfig().getPort()));
	}
	
	/**
	 * Creates a new DB with unique name for connection.
	 */
	public DB newDB(Mongo mongo) {
		return mongo.getDB(UUID.randomUUID().toString());
	}

	/**
	 * Cleans up the resources created by the utility.
	 */
	public void shutdown() {
		mongodProcess.stop();
		mongodExecutable.cleanup();
	}
}
