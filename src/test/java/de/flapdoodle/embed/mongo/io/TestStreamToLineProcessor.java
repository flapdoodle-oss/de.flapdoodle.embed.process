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
package de.flapdoodle.embed.mongo.io;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;

import junit.framework.TestCase;

import java.util.List;

//CHECKSTYLE:OFF
public class TestStreamToLineProcessor extends TestCase {

	public void testNewlines() {
		List<String> checks = Collections.newArrayList("line 1\n", "line 2\n", "line 3\n", "line 4\n");
		StreamToLineProcessor processor = new StreamToLineProcessor(new AssertStreamProcessor(checks));
		processor.process("li");
		processor.process("ne 1");
		processor.process("\n");
		processor.process("l");
		processor.process("ine 2\nline 3\nli");
		processor.process("ne 4");
		processor.process("\n");
		processor.onProcessed();
	}

	static class AssertStreamProcessor implements IStreamProcessor {

		private final List<String> _checks;
		private boolean _done = false;

		public AssertStreamProcessor(List<String> checks) {
			_checks = checks;
		}

		@Override
		public void process(String block) {
			assertFalse("Lines to process", _checks.isEmpty());
			String line = _checks.remove(0);
			assertNotNull("Line", line);
			assertEquals("Line", line, block);
		}

		@Override
		public void onProcessed() {
			_done = true;
		}

		public boolean isDone() {
			return _done;
		}
	}
}
