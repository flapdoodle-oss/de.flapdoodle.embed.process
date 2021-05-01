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

import org.immutables.value.Value;
import org.immutables.value.Value.Parameter;

/**
 * Interface for versions
 */
@FunctionalInterface
public interface Version {

	String asInDownloadPath();

	default int major() {
		return 0;
	}
	
	default int minor() {
		return 0;
	}

	default int patch() {
		return 0;
	}

	default boolean isNewerOrEqual(int major, int minor, int patch) {
		if (major() < major) {
			return false;
		} else if (major() > major) {
			return true;
		}

		if (minor() < minor) {
			return false;
		} else if (minor() > minor) {
			return true;
		}

		if (patch() < patch) {
			return false;
		} else if (patch() > patch) {
			return true;
		}

		// same version
		return true;
	}

	default boolean isOlderOrEqual(int major, int minor, int patch) {
		if (major() < major) {
			return true;
		} else if (major() > major) {
			return false;
		}

		if (minor() < minor) {
			return true;
		} else if (minor() > minor) {
			return false;
		}

		if (patch() < patch) {
			return true;
		} else if (patch() > patch) {
			return false;
		}

		// same version
		return true;
	}

	@Value.Immutable
	interface GenericVersion extends Version {
		
		@Override
		@Parameter
		String asInDownloadPath();
	}
	
	static GenericVersion of(String asInDownloadPath) {
		return ImmutableGenericVersion.of(asInDownloadPath);
	}
}
