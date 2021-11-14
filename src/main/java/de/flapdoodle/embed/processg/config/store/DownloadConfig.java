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
package de.flapdoodle.embed.processg.config.store;

import de.flapdoodle.embed.process.config.store.ProxyFactory;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

import java.util.Optional;

@Value.Immutable
public interface DownloadConfig {

		Optional<ProxyFactory> proxyFactory();

		@Default
		default String getUserAgent() {
				return "Mozilla/5.0 (compatible; +https://github.com/flapdoodle-oss/de.flapdoodle.embed.process)";
		}

		Optional<String> getAuthorization();

		@Default
		default TimeoutConfig getTimeoutConfig() {
				return TimeoutConfig.defaults();
		}

		static ImmutableDownloadConfig.Builder builder() {
				return ImmutableDownloadConfig.builder();
		}

		static ImmutableDownloadConfig defaults() {
				return builder().build();
		}
}
