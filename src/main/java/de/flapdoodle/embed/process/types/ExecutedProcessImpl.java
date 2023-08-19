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
package de.flapdoodle.embed.process.types;

public class ExecutedProcessImpl implements ExecutedProcess {

	private final int returnCode;

	public ExecutedProcessImpl(int returnCode) {
		this.returnCode = returnCode;
	}

	@Override
	public int returnCode() {
		return returnCode;
	}

	@Override public String toString() {
		return "ExecutedProcess{" +
			"returnCode=" + returnCode +
			'}';
	}
	
	public static <R extends RunningProcess> ExecutedProcess stop(R r) {
		int returnCode = r.stop();
		return new ExecutedProcessImpl(returnCode);
	}
}
