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
		System system = SystemBuilder.builderOf("test")
			.transitionInto(String.class)
				.with(() -> State.of("foo"))
			.transitionInto("dep",String.class)
				.with(String.class, s -> s.map(t -> "-> "+t))
			.build();
		
		system.withStateOf(NamedType.of(String.class), s -> {
			assertEquals("foo", s);
		});
		
		system.withStateOf(NamedType.of("dep", String.class), s -> {
			assertEquals("-> foo", s);
		});
	}
}
