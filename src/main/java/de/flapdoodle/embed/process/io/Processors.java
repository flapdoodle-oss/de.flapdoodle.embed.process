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
package de.flapdoodle.embed.process.io;

import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Processors {

	private Processors() {
		throw new IllegalAccessError("singleton");
	}

	public static IStreamProcessor console() {
		return new ConsoleOutputStreamProcessor();
	}

	public static IStreamProcessor named(String name, IStreamProcessor destination) {
		return new NamedOutputStreamProcessor(name, destination);
	}

	public static IStreamProcessor namedConsole(String name) {
		return named(name, console());
	}

	public static IStreamProcessor logTo(Logger logger, Level level) {
		return new LoggingOutputStreamProcessor(logger, level);
	}
	
	public static ReaderProcessor connect(Reader reader, IStreamProcessor processor) {
		return new ReaderProcessor(reader, processor);
	}
}
