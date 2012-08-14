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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class MongodForTestsFactoryTest {
	private static MongodForTestsFactory testsFactory;

	@BeforeClass
	public static void setMongoDB() throws IOException {
		testsFactory = MongodForTestsFactory.with(Version.Main.V2_0);
	}

	@AfterClass
	public static void tearDownMongoDB() throws Exception {
		testsFactory.shutdown();
	}

	private DB db;

	/**
	 * Imports data into a collection
	 * 
	 * @param collection
	 * @param jsonStream
	 */
	private void importCollection(final DBCollection collection,
			final InputStream jsonStream) {
		@SuppressWarnings("unchecked")
		final List<DBObject> list = (List<DBObject>) JSON.parse(new Scanner(
				jsonStream).useDelimiter("\\A").next());
		collection.insert(list);
	}

	@Before
	public void setUpMongoDB() throws Exception {
		// create database
		final Mongo mongo = testsFactory.newMongo();
		db = mongo.getDB(UUID.randomUUID().toString());
	}

	public void testDatabaseCreated() {
		assertNotNull(db);
	}

	/**
	 * This tests based on an imported JSON data file.
	 */
	@Test
	public void testImport() throws Exception {
		// perform operations
		final DBCollection coll = db.getCollection("testCollection");
		importCollection(coll, Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("sample.json"));
		{
			final DBObject myDoc = coll.findOne(new BasicDBObject("name",
					"MongoDB"));
			assertEquals("MongoDB", myDoc.get("name"));
			assertEquals(203, ((BasicDBObject) myDoc.get("info")).get("x"));
		}
		{
			final DBObject myDoc = coll.findOne(new BasicDBObject("name",
					"Cassandra"));
			assertEquals("Cassandra", myDoc.get("name"));
			assertEquals(201, ((BasicDBObject) myDoc.get("info")).get("x"));
		}
	}

	/**
	 * This is an example based on
	 * http://www.mongodb.org/display/DOCS/Java+Tutorial to see if things work.
	 */
	@Test
	public void testSample() throws Exception {

		// perform operations
		final DBCollection coll = db.getCollection("testCollection");
		{
			final BasicDBObject doc = new BasicDBObject();

			doc.put("name", "MongoDB");
			doc.put("type", "database");
			doc.put("count", 1);

			final BasicDBObject info = new BasicDBObject();

			info.put("x", 203);
			info.put("y", 102);

			doc.put("info", info);

			coll.insert(doc);
		}
		{
			final DBObject myDoc = coll.findOne();
			assertEquals("MongoDB", myDoc.get("name"));
			assertEquals(203, ((BasicDBObject) myDoc.get("info")).get("x"));
		}
	}
}
