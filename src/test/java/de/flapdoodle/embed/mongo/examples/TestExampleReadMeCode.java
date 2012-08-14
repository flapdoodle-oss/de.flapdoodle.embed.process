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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.MongodProcessOutputConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.ArtifactStoreInFixedPath;
import de.flapdoodle.embed.process.config.store.IArtifactStoragePathNaming;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.progress.LoggingProgressListener;
import de.flapdoodle.embed.process.runtime.Network;

public class TestExampleReadMeCode extends TestCase {

	// ### Usage
	public void testStandard() throws UnknownHostException, IOException {
		int port = 12345;
		MongodProcess mongod = null;
		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());

		MongodStarter runtime = MongodStarter.getDefaultInstance();

		try {
			MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			Mongo mongo = new Mongo("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongod != null)
				mongod.stop();
		}
	}

	// ### Usage - custom mongod filename 
	public void testCustomMongodFilename() throws UnknownHostException, IOException {

		int port = 12345;
		MongodProcess mongod = null;
		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());

		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.setExecutableNaming(new UserTempNaming());
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

		try {
			MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			Mongo mongo = new Mongo("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongod != null)
				mongod.stop();
		}
	}

	// ### Unit Tests
	public void testUnitTests() {
		Class<?> see = AbstractMongoDBTest.class;
	}
	
	// #### ... with some more help
	public void testMongodForTests() throws IOException {
		MongodForTestsFactory factory = null;
		try {
			factory = MongodForTestsFactory.with(Version.Main.V2_0);

			Mongo mongo = factory.newMongo();
			DB db = mongo.getDB("test-" + UUID.randomUUID());
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (factory != null)
				factory.shutdown();
		}
	}


	// ### Customize Artifact Storage
	public void testCustomizeArtifactStorage() throws IOException {

		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, 12345, Network.localhostIsIPv6());

		/// - 8<- - - - 
		IArtifactStoragePathNaming artifactStorePath = new ArtifactStoreInFixedPath(System.getProperty("user.home")
				+ "/.embeddedMongodbCustomPath");
		ITempNaming executableNaming = new UUIDTempNaming();

		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.getDownloadConfig().setArtifactStorePathNaming(artifactStorePath);
		runtimeConfig.setExecutableNaming(executableNaming);

		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
		MongodExecutable mongodExe = runtime.prepare(mongodConfig);
		/// - >8- - - - 
		MongodProcess mongod = mongodExe.start();

		mongod.stop();
		mongodExe.cleanup();
	}

	// ### Usage - custom mongod process output
	// #### ... to console with line prefix
	public void testCustomOutputToConsolePrefix() {

		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.setProcessOutput(new ProcessOutput(Processors.namedConsole("[mongod>]"),
				Processors.namedConsole("[MONGOD>]"), Processors.namedConsole("[console>]")));
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

	}

	// #### ... to file
	public void testCustomOutputToFile() throws FileNotFoundException, IOException {
		RuntimeConfig runtimeConfig = new RuntimeConfig();
		IStreamProcessor mongodOutput = Processors.named("[mongod>]",
				new FileStreamProcessor(File.createTempFile("mongod", "log")));
		IStreamProcessor mongodError = new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
		IStreamProcessor commandsOutput = Processors.namedConsole("[console>]");

		runtimeConfig.setProcessOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput));
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
	}

	// #### ... to java logging
	public void testCustomOutputToLogging() throws FileNotFoundException, IOException {
		Logger logger = Logger.getLogger(getClass().getName());

		RuntimeConfig runtimeConfig = new RuntimeConfig();
		runtimeConfig.setProcessOutput(new ProcessOutput(Processors.logTo(logger, Level.INFO),
				Processors.logTo(logger, Level.SEVERE), Processors.named("[console>]", Processors.logTo(logger, Level.FINE))));
		runtimeConfig.getDownloadConfig().setProgressListener(new LoggingProgressListener(logger, Level.FINE));
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
	}

	// #### ... to default java logging (the easy way)
	public void testDefaultOutputToLogging() throws FileNotFoundException, IOException {
		Logger logger = Logger.getLogger(getClass().getName());
		RuntimeConfig runtimeConfig = RuntimeConfig.getInstance(logger);
		MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
	}

	/*
	 * Ist fürs Readme, deshalb nicht statisch und public
	 */
	public class FileStreamProcessor implements IStreamProcessor {

		private FileOutputStream outputStream;

		public FileStreamProcessor(File file) throws FileNotFoundException {
			outputStream = new FileOutputStream(file);
		}

		@Override
		public void process(String block) {
			try {
				outputStream.write(block.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onProcessed() {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// ### Custom Version
	public void testCustomVersion() throws UnknownHostException, IOException {
		int port = 12345;
		MongodProcess mongod = null;
		MongodConfig mongodConfig = new MongodConfig(new GenericVersion("2.0.7-rc1"), port, Network.localhostIsIPv6());

		MongodStarter runtime = MongodStarter.getDefaultInstance();

		try {
			MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			Mongo mongo = new Mongo("localhost", port);
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongod != null)
				mongod.stop();
		}

	}

	// ### Use Free Server Port
	// #### ... by hand
	public void testFreeServerPort() throws UnknownHostException, IOException {
		int port = Network.getFreeServerPort();
	}

	// ### ... automagic
	public void testFreeServerPortAuto() throws UnknownHostException, IOException {
		MongodProcess mongod = null;
		MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0);

		MongodStarter runtime = MongodStarter.getDefaultInstance();

		try {
			MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			Mongo mongo = new Mongo(new ServerAddress(Network.getLocalHost(), mongodConfig.getPort()));
			DB db = mongo.getDB("test");
			DBCollection col = db.createCollection("testCol", new BasicDBObject());
			col.save(new BasicDBObject("testDoc", new Date()));

		} finally {
			if (mongod != null)
				mongod.stop();
		}
	}

}
