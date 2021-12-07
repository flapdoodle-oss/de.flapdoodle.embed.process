/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
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
package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.config.store.FileSet.Entry;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class FileSetTest {

	@Test
	public void ensureHashcodeAndEqualsForEntries() {
		Entry entryA = FileSet.Entry.of(FileType.Executable,"foo",Pattern.compile("foo"));
		Entry entryAlike = FileSet.Entry.of(FileType.Executable,"foo",Pattern.compile("foo"));
		Entry entryB = FileSet.Entry.of(FileType.Library,"foo",Pattern.compile("foo"));
		
		assertEqualsAndHashCode(entryA, entryAlike);
		assertFalse(entryA.equals(entryB));
		assertFalse(entryB.equals(entryA));
	}

	private <T> void assertEqualsAndHashCode(T a, T b) {
		assertTrue("a==b",a.equals(b));		
		assertTrue("b==a",b.equals(a));
		assertEquals("a.hash==b.hash",a.hashCode(), b.hashCode());
	}
}
