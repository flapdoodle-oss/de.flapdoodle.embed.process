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
package de.flapdoodle.embed.process.runtime;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessWithPidField extends Process {

	private int pid = 123;

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return 0;
	}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public void destroy() {

	}
}
