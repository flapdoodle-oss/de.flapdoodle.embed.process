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
public class ConsoleOneLineProgressListener implements IProgressListener {

	private static final char BAR_DONE = '=';
	private static final char BAR_TODO = '-';
	static final int LINE_LEN = 80;
	static final char[] CLOCK = {'-', '\\', '|', '/'};
	public static final int ONE_HUNDRED_PERCENT = 100;

	private int lastPercent = -1;
	private int lastIdx = 0;

	@Override
	public void progress(String label, int percent) {
		if (percent < 0)
			throw new IllegalArgumentException("Percent < 0: " + percent);
		if (percent > ONE_HUNDRED_PERCENT)
			throw new IllegalArgumentException("Percent > 100: " + percent);

		if (lastPercent == percent) {
			lastIdx++;
			if (lastIdx >= CLOCK.length)
				lastIdx = 0;
		} else {
			lastIdx = 0;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(label).append(" ");
		int lineLength = LINE_LEN - label.length() - 1;
		int percLength = percent * lineLength / ONE_HUNDRED_PERCENT;

		sb.append(makeString(BAR_DONE, percLength));
		if (percent < ONE_HUNDRED_PERCENT) {
			sb.append(CLOCK[lastIdx]);
			sb.append(makeString(BAR_TODO, lineLength - percLength));
		} else {
			sb.append(BAR_DONE);
		}
		sb.append("\r");

		lastPercent = percent;

		System.out.print(sb.toString());
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

	static String makeString(char c, int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
