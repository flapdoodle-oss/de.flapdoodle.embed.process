/*
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
package de.flapdoodle.embed.process.io;

import de.flapdoodle.checks.Preconditions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class ListeningStreamProcessorTest {

	private final StreamCollector delegate = new StreamCollector();
	private final List<String> lines = new ArrayList<>();

	private final ListeningStreamProcessor testee = new ListeningStreamProcessor(delegate, new Consumer<String>() {
		@Override public void accept(String s) {
			lines.add(s);
		}
	});

	@Test
	void callListenerOnEachNewLine() {
		testee.process("\nsecond\nthird\n");

		assertThat(lines)
			.containsExactly("", "second", "third");
		assertThat(delegate.processed)
			.isFalse();

		testee.onProcessed();

		assertThat(lines)
			.containsExactly("", "second", "third", "");
		assertThat(delegate.processed)
			.isTrue();
		assertThat(delegate.processList)
			.containsExactly("\nsecond\nthird\n");
	}

	@Test
	void callListenerWithEmptyLines() {
		testee.process("\n\n\n");
		assertThat(lines)
			.containsExactly("", "", "");
	}

	@Test
	void callListenerInOnProcessedForTheLastPart() {
		testee.process("first\nsecond");

		assertThat(lines)
			.containsExactly("first");

		testee.onProcessed();

		assertThat(lines)
			.containsExactly("first", "second");
	}

	@Test
	void callListenerOnlyInOnProcessed() {
		testee.process("without newline");

		assertThat(lines).isEmpty();

		testee.onProcessed();

		assertThat(lines)
			.containsExactly("without newline");
	}

	static class StreamCollector implements StreamProcessor {
		final List<String> processList = new ArrayList<>();
		boolean processed = false;

		@Override
		public void process(String block) {
			processList.add(block);
		}

		@Override
		public void onProcessed() {
			Preconditions.checkArgument(!processed, "onProcessed already called");
			processed = true;
		}
	}
}