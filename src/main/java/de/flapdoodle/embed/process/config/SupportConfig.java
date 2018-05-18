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
package de.flapdoodle.embed.process.config;

import java.util.function.BiFunction;

import org.immutables.value.Value;

@Value.Immutable
public interface SupportConfig {

	String name();

	String supportUrl();

	BiFunction<Class<?>, Exception, String> messageOnException();

	long maxStopTimeoutMillis();

	static SupportConfig generic() {
		return builder()
				.name("generic")
				.supportUrl("https://github.com/flapdoodle-oss/de.flapdoodle.embed.process")
				.messageOnException((clazz,ex) -> null)
				.maxStopTimeoutMillis(15000)
				.build();
	}

	static ImmutableSupportConfig.Builder builder() {
		return ImmutableSupportConfig.builder();
	}
}
