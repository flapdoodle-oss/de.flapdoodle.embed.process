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
package de.flapdoodle.embed.process.example;

import java.util.OptionalLong;

import de.flapdoodle.embed.process.config.ExecutableProcessConfig;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.distribution.Version;

public class GenericProcessConfig implements ExecutableProcessConfig {

	protected final Version version;
	private final SupportConfig _supportConfig;

	public GenericProcessConfig(Version version) {
		this.version = version;
		_supportConfig = SupportConfig.generic();
	}

	@Override
	public Version version() {
		return version;
	}

	@Override
	public SupportConfig supportConfig() {
		return _supportConfig;
	}
	
	@Override
	public OptionalLong stopTimeoutInMillis() {
		return OptionalLong.empty();
	}
}