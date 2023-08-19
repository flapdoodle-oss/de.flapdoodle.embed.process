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

import org.immutables.value.Value.Immutable;

@Immutable
public interface ProcessOutput {

	StreamProcessor output();

	StreamProcessor error();

	StreamProcessor commands();

	static ProcessOutput namedConsole(String label) {
		return builder()
				.output(Processors.namedConsole("["+label+" output]"))
				.error(Processors.namedConsole("["+label+" error]"))
				.commands(Processors.console())
				.build();
	}
	
	static ProcessOutput silent() {
		return builder()
				.output(Processors.silent())
				.error(Processors.silent())
				.commands(Processors.silent())
				.build();
	}

	static ProcessOutput named(String label, org.slf4j.Logger logger) {
		return builder()
				.output(Processors.named("["+label+" output]", Processors.logTo(logger, Slf4jLevel.INFO)))
				.error(Processors.named("["+label+" error]", Processors.logTo(logger, Slf4jLevel.ERROR)))
				.commands(Processors.logTo(logger, Slf4jLevel.DEBUG))
				.build();
	}

	static ImmutableProcessOutput.Builder builder() {
		return ImmutableProcessOutput.builder();
	}
}
