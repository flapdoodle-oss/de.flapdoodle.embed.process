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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;

public class PlatformLibraryStoreBuilder extends AbstractBuilder<LibraryStore> {

	private static final TypedProperty<LibraryContainer> LIBRARIES = TypedProperty
			.with("Libraries", LibraryContainer.class);

	public PlatformLibraryStoreBuilder defaults() {
		setDefault(LIBRARIES, null);
		return this;
	}

	public PlatformLibraryStoreBuilder setLibraries(Platform platform,
			String[] libraries) {
		LibraryContainer libraryContainer = get(LIBRARIES, null);
		Map<Platform, List<String>> _libraries;
		if (libraryContainer == null) {
			_libraries = new HashMap<Platform, List<String>>();
			set(LIBRARIES, new LibraryContainer(_libraries));
		} else {
			_libraries = libraryContainer.value();
		}
		_libraries.put(platform, Arrays.asList(libraries));
		return this;
	}

	public PlatformLibraryStoreBuilder setLibrary(Platform platform, String library) {
		Map<Platform, List<String>> _libraries = get(LIBRARIES).value();
		if (_libraries == null) {
			_libraries = new HashMap<Platform, List<String>>();
			_libraries.put(platform, Arrays.asList(library));
		}
		set(LIBRARIES, new LibraryContainer(_libraries));
		return this;
	}

	@Override
	public LibraryStore build() {
		Map<Platform, List<String>> value = get(LIBRARIES).value();
		return new ImmutableLibraryStore(value);
	}

	static class LibraryContainer extends
			ImmutableContainer<Map<Platform, List<String>>> {

		public LibraryContainer(Map<Platform, List<String>> libraries) {
			super(libraries);
		}
	}

	static class ImmutableLibraryStore implements LibraryStore {

		private final Map<Platform, List<String>> _libraries;

		private ImmutableLibraryStore(Map<Platform, List<String>> _libraries) {
			this._libraries = _libraries;
		}

		@Override
		public List<String> getLibrary(Distribution distribution) {
			return _libraries.get(distribution.platform());
		}

	}
}
