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
package de.flapdoodle.embed.process.distribution;

/**
 * Platform enum
 */
public enum Platform {
	Linux,
	Windows,
	OS_X;

	public static Platform detect() {
		String osName = System.getProperty("os.name");
		if (osName.equals("Linux"))
			return Linux;
		if (osName.startsWith("Windows", 0))
			return Windows;
		if (osName.equals("Mac OS X"))
			return OS_X;
		throw new IllegalArgumentException("Could not detect Platform: os.name=" + osName);
	}
}
