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

import java.io.Reader;
import java.util.logging.Level;

/**
 *
 */
public class Processors {

	private Processors() {
		throw new IllegalAccessError("singleton");
	}

	public static StreamProcessor console() {
		return new ConsoleOutputStreamProcessor();
	}
	
	public static StreamProcessor silent() {
		return new NullProcessor();
	}

	public static StreamProcessor named(String name, StreamProcessor destination) {
		return new NamedOutputStreamProcessor(name, destination);
	}

	public static StreamProcessor namedConsole(String name) {
		return named(name, console());
	}

	public static StreamProcessor logTo(java.util.logging.Logger logger, Level level) {
		return new LoggingOutputStreamProcessor(logger, level);
	}

	public static StreamProcessor logTo(org.slf4j.Logger logger, Slf4jLevel level) {
		return new Slf4jStreamProcessor(logger, level);
	}

	public static ReaderProcessor connect(Reader reader, StreamProcessor processor) {
		return new ReaderProcessor(reader, processor);
	}
}
