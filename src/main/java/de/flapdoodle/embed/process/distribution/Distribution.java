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
package de.flapdoodle.embed.process.distribution;

import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import org.immutables.value.Value;
import org.immutables.value.Value.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 *
 */
@Value.Immutable
public abstract class Distribution {

	private static final Logger logger = LoggerFactory.getLogger(Distribution.class);

	@Parameter
	public abstract Version version();

	@Parameter
	public abstract Platform platform();

	@Override
	public String toString() {
		return "" + version() + ":" + platform();
	}

	public static Distribution detectFor(Collection<? extends OS> osList, Version version) {
		List<Platform> platforms = Platform.guess(osList);
		if (platforms.isEmpty()) {
			throw new IllegalArgumentException("could not detect platform");
		}

		Platform platform = platforms.get(0);

		if (platforms.size()!=1) {
			logger.info("more than one platform detected: {}", platforms);
			logger.info("use {}", platform);
		}

		return of(version, platform);
	}

	public static Distribution of(Version version, Platform platform) {
		return ImmutableDistribution.of(version, platform);
	}
}
