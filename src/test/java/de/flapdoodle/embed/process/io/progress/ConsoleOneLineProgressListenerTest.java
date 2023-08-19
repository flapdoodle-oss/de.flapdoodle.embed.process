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
package de.flapdoodle.embed.process.io.progress;

import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleOneLineProgressListenerTest {
	public static Stream<Arguments> blocksAndPercent() {
		List<Arguments> ret=new ArrayList<>();
		for (int blocks : Arrays.asList(1,4,8,9)) {
			for (int percent : Arrays.asList(1,10,80,100)) {
				ret.add(Arguments.of(blocks, percent));
			}
		}
		return ret.stream();
	}

	@ParameterizedTest(name = "[{index}]: {0} blocks, {0} percent")
//	@ValueSource(ints = {1, 10, 50, 99})
	@MethodSource("blocksAndPercent")
	void dontCrashAtAnyMessageLength(int blocks, int percent) {
		String label = String.join("", Collections.nCopies(blocks, "0123456789"));
		ConsoleOneLineProgressListener testee = new ConsoleOneLineProgressListener();
		testee.progress(label, percent);
	}

}