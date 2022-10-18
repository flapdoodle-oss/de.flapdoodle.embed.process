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

import java.util.function.Consumer;

public class ListeningStreamProcessor implements StreamProcessor {

	private final StreamProcessor delegate;
	private final Consumer<String> lineListener;
	private final StringBuilder buffer = new StringBuilder();

	public ListeningStreamProcessor(StreamProcessor delegate, Consumer<String> lineListener) {
		this.delegate = delegate;
		this.lineListener = lineListener;
	}

	@Override
	public void process(String block) {
		buffer.append(block);

		parseLines();

		delegate.process(block);
	}

	private void parseLines() {
		int newLine;
		do {
			String content = buffer.toString();
			newLine = content.indexOf('\n');
			if (newLine != -1) {
				String line = content.substring(0, newLine);
				lineListener.accept(line);
				buffer.delete(0, newLine + 1);
			}
		} while (newLine != -1);
	}

	@Override
	public void onProcessed() {
		lineListener.accept(buffer.toString());
		buffer.setLength(0);

		delegate.onProcessed();
	}
}
