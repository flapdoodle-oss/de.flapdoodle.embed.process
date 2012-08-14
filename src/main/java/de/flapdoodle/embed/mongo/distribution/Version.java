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
package de.flapdoodle.embed.mongo.distribution;

import de.flapdoodle.embed.process.distribution.IVersion;

/**
 * MongoDB Version enum
 */
public enum Version implements IVersion {

	V1_6_5("1.6.5"),
	@Deprecated
	V1_7_6("1.7.6"),
	@Deprecated
	V1_8_0_rc0("1.8.0-rc0"),
	@Deprecated
	V1_8_0("1.8.0"),
	@Deprecated
	V1_8_1("1.8.1"),
	@Deprecated
	V1_8_2_rc0("1.8.2-rc0"),
	@Deprecated
	V1_8_2("1.8.2"),
	@Deprecated
	V1_8_4("1.8.4"),
	V1_8_5("1.8.5"),
	@Deprecated
	V1_9_0("1.9.0"),
	@Deprecated
	V2_0_1("2.0.1"),
	@Deprecated
	V2_0_4("2.0.4"),
	V2_0_5("2.0.5"),
	V2_0_6("2.0.6"),
	V2_0_7_RC1("2.0.7-rc1"),

	@Deprecated
	V2_1_0("2.1.0"),
	V2_1_1("2.1.1"),
	V2_1_2("2.1.2"),
	V2_2_0_RC0("2.2.0-rc0"),
	;

	private final String specificVersion;

	Version(String vName) {
		this.specificVersion = vName;
	}

	@Override
	public String asInDownloadPath() {
		return specificVersion;
	}

	@Override
	public String toString() {
		return "Version{" + specificVersion + '}';
	}

	public static enum Main implements IVersion {
		V1_6(V1_6_5),
		V1_8(V1_8_5),
		V2_0(V2_0_6),
		V2_1(V2_1_2),
		V2_2(V2_2_0_RC0);

		private final Version _latest;

		Main(Version latest) {
			_latest = latest;
		}

		@Override
		public String asInDownloadPath() {
			return _latest.asInDownloadPath();
		}
	}

}
