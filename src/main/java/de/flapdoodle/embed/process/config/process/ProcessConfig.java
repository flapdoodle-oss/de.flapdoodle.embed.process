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
package de.flapdoodle.embed.process.config.process;

import de.flapdoodle.embed.process.config.process.ImmutableProcessConfig.Builder;
import de.flapdoodle.embed.process.io.StreamProcessor;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * @see de.flapdoodle.embed.process.types.ProcessConfig
 */
@Deprecated
@Immutable
public interface ProcessConfig {

	List<String> commandLine();

	StreamProcessor output();

	@Default
	default StreamProcessor error() {
		return output();
	}

	public static Builder builder() {
		return ImmutableProcessConfig.builder();
	}
}
