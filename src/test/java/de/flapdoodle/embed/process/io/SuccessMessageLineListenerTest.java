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

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SuccessMessageLineListenerTest {
	private final SuccessMessageLineListener testee = SuccessMessageLineListener.of(
		Arrays.asList("OK", "done"),
		Arrays.asList("Error: (?<error>.*)","failed with (?<error>[A-Z]{3})"),
		"error"
	);

	@Test
	void noErrorAndNoSuccess() {
		testee.inspect("NOPE");

		assertThat(testee.successMessageFound())
			.isFalse();
		assertThat(testee.errorMessage())
			.isEmpty();
	}

	@Test
	void shouldSucceed() {
		testee.inspect("well done");

		assertThat(testee.successMessageFound())
			.isTrue();
	}

	@Test
	void shouldFailWithRestOfTheLine() {
		testee.inspect("Error: something went wrong");

		assertThat(testee.successMessageFound())
			.isFalse();
		assertThat(testee.errorMessage())
			.contains("something went wrong");
	}

	@Test
	void shouldFailWith3LetterCode() {
		testee.inspect("failed with ABC code");

		assertThat(testee.successMessageFound())
			.isFalse();
		assertThat(testee.errorMessage())
			.contains("ABC");
	}

	@Test
	void waitNotifyShouldWork() throws InterruptedException {
		assertThat(testee.successMessageFound()).isFalse();

		new Thread(() -> {
			try {
				Thread.sleep(100);
				testee.inspect("OK");
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();
		
		testee.waitForResult(3000);
		assertThat(testee.successMessageFound()).isTrue();
	}
}