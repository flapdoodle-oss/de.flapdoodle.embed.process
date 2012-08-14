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
 *
 */
public class Distribution {

	private final IVersion version;
	private final Platform platform;
	private final BitSize bitsize;

	public Distribution(IVersion version, Platform platform, BitSize bitsize) {
		this.version = version;
		this.platform = platform;
		this.bitsize = bitsize;
	}

	public IVersion getVersion() {
		return version;
	}

	public Platform getPlatform() {
		return platform;
	}

	public BitSize getBitsize() {
		return bitsize;
	}

	@Override
	public String toString() {
		return "" + version + ":" + platform + ":" + bitsize;
	}

	public static Distribution detectFor(IVersion version) {
		return new Distribution(version, Platform.detect(), BitSize.detect());
	}
}
