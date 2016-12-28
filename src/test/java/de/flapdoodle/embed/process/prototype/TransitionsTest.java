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
package de.flapdoodle.embed.process.prototype;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TransitionsTest {

	@Test
	public void sample() {
		TransitionRuntime system = TransitionRuntimeBuilder.builderOf("test")
			.transitionInto(String.class)
				.with(() -> State.of("simple",TransitionsTest::tearDown))
			.transitionInto("dep",String.class)
				.with(String.class, s -> s.map(t -> "depends on "+t, TransitionsTest::tearDown))
			.transitionInto("join", String.class)
				.with("", String.class, "dep", String.class, (a,b) -> State.merge(a, b, (l, r)  -> "["+l+"]:["+r+"]", TransitionsTest::tearDown))
			.build();
		
		System.out.println("sample: simple");
		system.withStateOf(NamedType.of(String.class), s -> {
			assertEquals("simple", s);
		});
		
		System.out.println("sample: with dependency");
		system.withStateOf(NamedType.of("dep", String.class), s -> {
			assertEquals("depends on simple", s);
		});
		
		System.out.println("sample: joining");
		system.withStateOf(NamedType.of("join", String.class), s -> {
			assertEquals("[simple]:[depends on simple]", s);
		});
	}
	
	public static <T> void tearDown(T value) {
		System.out.println("tear down '"+value+"'");
	}
}
