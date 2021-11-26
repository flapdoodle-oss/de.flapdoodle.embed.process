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
package de.flapdoodle.embed.process.config.store;

import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Parameter;

import java.util.List;
import java.util.regex.Pattern;

@Value.Immutable
public interface FileSet {

	List<Entry> entries();

	@Check
	default void shouldContainOneMoreExecutable() {
		boolean oneOrMoreExecutableFound = entries().stream().anyMatch(e -> e.type() == FileType.Executable);
		if (!oneOrMoreExecutableFound) {
			throw new IllegalArgumentException("there is no executable in this file set");
		}
	}

	@Value.Immutable
	abstract class Entry {
		@Parameter
		public abstract FileType type();

		@Parameter
		public abstract String destination();

		@Parameter
		protected abstract UncompiledPattern uncompiledMatchingPattern();

		@Auxiliary
		public Pattern matchingPattern() {
			return uncompiledMatchingPattern().compile();
		}

		static Entry of(FileType type, String filename, Pattern pattern) {
			return ImmutableEntry.of(type, filename, UncompiledPattern.of(pattern));
		}
	}

	class Builder extends ImmutableFileSet.Builder {

		public Builder addEntry(FileType type, String filename) {
			return addEntry(type, filename, ".*" + filename);
		}

		public Builder addEntry(FileType type, String filename, String pattern) {
			return addEntry(type, filename, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		}

		public Builder addEntry(FileType type, String filename, Pattern pattern) {
			return addEntries(Entry.of(type, filename, pattern));
		}

	}

	static Builder builder() {
		return new Builder();
	}
}
