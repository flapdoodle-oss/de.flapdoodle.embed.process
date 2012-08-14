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
package de.flapdoodle.embed.process.config.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.flapdoodle.embed.process.io.IStreamProcessor;

/**
 *
 */
public class ProcessConfig {

	private final List<String> commandLine;
	private final IStreamProcessor output;
	private final IStreamProcessor error;

	public ProcessConfig(List<String> commandLine, IStreamProcessor output, IStreamProcessor error) {
		this.commandLine = new ArrayList<String>(commandLine);
		this.output = output;
		this.error = error;
	}

	public ProcessConfig(List<String> commandLine, IStreamProcessor output) {
		this(commandLine, output, null);
	}

	public List<String> getCommandLine() {
		return Collections.unmodifiableList(commandLine);
	}

	public IStreamProcessor getOutput() {
		return output;
	}

	public IStreamProcessor getError() {
		return error;
	}
}
