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
package de.flapdoodle.embed.process.io.progress;

/**
 *
 */
public class StandardConsoleProgressListener implements IProgressListener {

	private String lastLabel = null;
	private int lastPercent = -1;

	@Override
	public void progress(String label, int percent) {
		if (!label.equals(lastLabel)) {
			System.out.print(label);
			System.out.print(" ");
		}
		if (percent != lastPercent) {
			System.out.print(percent);
			System.out.print("% ");
		}
		lastLabel = label;
		lastPercent = percent;
	}

	@Override
	public void done(String label) {
		System.out.println(label + " DONE");
	}

	@Override
	public void start(String label) {
		System.out.println(label + " START");
	}

	@Override
	public void info(String label, String message) {
		System.out.println(label + " " + message);
	}
}
