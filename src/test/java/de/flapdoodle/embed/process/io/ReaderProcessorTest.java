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
package de.flapdoodle.embed.process.io;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class ReaderProcessorTest {

	@Test
	public void readerShouldFinishIfInterrupt() throws IOException, InterruptedException {
		PipedOutputStream writeToThisStream = new PipedOutputStream();

		TesteeWrapper testee;
		try (InputStream is = new PipedInputStream(writeToThisStream)) {
			testee = new TesteeWrapper(is);

			writeToThisStream.write("first line".getBytes(StandardCharsets.UTF_8));
			writeToThisStream.flush();
			Thread.sleep(10);

			Assertions.assertThat(testee.blocks()).containsExactly("first line");
		}

		Assertions.assertThat(testee.processedCalled()).isFalse();

		testee.processor.abort();

		Assertions.assertThat(testee.processedCalled()).isTrue();
		Assertions.assertThat(testee.processor.isAlive()).isFalse();
	}

	@Test
	public void abortAllMustFinishAllReader() throws IOException, InterruptedException {
		PipedOutputStream firstStream = new PipedOutputStream();
		PipedOutputStream secondStream = new PipedOutputStream();

		try (PipedInputStream firstIs = new PipedInputStream(firstStream)) {
			try (PipedInputStream secondIs = new PipedInputStream(secondStream)) {
				TesteeWrapper first = new TesteeWrapper(firstIs);
				TesteeWrapper second = new TesteeWrapper(secondIs);

				firstStream.write("first".getBytes(StandardCharsets.UTF_8));
				secondStream.write("second".getBytes(StandardCharsets.UTF_8));
				firstStream.flush();
				secondStream.flush();
				Thread.sleep(10);

				Assertions.assertThat(first.blocks()).containsExactly("first");
				Assertions.assertThat(second.blocks()).containsExactly("second");

				Assertions.assertThat(first.processedCalled()).isFalse();
				Assertions.assertThat(second.processedCalled()).isFalse();

				ReaderProcessor.abortAll(first.processor, second.processor);

				Assertions.assertThat(first.processedCalled()).isTrue();
				Assertions.assertThat(second.processedCalled()).isTrue();

				Assertions.assertThat(first.processor.isAlive()).isFalse();
				Assertions.assertThat(second.processor.isAlive()).isFalse();
			}

	}
	}

	static class TesteeWrapper {
		private final ReaderProcessor processor;
		private final CopyOnWriteArrayList<String> blocks=new CopyOnWriteArrayList<>();
		private final AtomicBoolean processedCalled=new AtomicBoolean();

		public TesteeWrapper(InputStream is) {
			this.processor = new ReaderProcessor(new InputStreamReader(is), new StreamProcessor() {
				@Override public void process(String block) {
					blocks.add(block);
				}
				@Override public void onProcessed() {
					processedCalled.compareAndSet(false, true);
				}
			});
		}

		public List<String> blocks() {
			// make a copy bc assertj is not thread safe
			return new ArrayList<>(blocks);
		}

		public boolean processedCalled() {
			return processedCalled.get();
		}
	}
}