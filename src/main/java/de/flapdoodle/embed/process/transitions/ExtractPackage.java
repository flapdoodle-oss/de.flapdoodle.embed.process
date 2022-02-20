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
package de.flapdoodle.embed.process.transitions;

import de.flapdoodle.embed.process.archives.ExtractFileSet;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.types.Archive;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.naming.HasLabel;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class ExtractPackage implements Transition<ExtractedFileSet>, HasLabel {

//	protected abstract Optional<ExtractedFileSetStore> extractedFileSetStore();

	@Override
	@Value.Default
	public String transitionLabel() {
		return "ExtractPackage";
	}

	@Value.Default
	protected StateID<Name> name() {
		return StateID.of(Name.class);
	}

	@Override
	@Value.Default
	public StateID<ExtractedFileSet> destination() {
		return StateID.of(ExtractedFileSet.class);
	}

	@Value.Default
	protected StateID<Archive> archive() {
		return StateID.of(Archive.class);
	}

	@Value.Default
	protected StateID<Package> distPackage() {
		return StateID.of(Package.class);
	}

	@Value.Default
	protected StateID<TempDir> tempDir() {
		return StateID.of(TempDir.class);
	}

	protected abstract Optional<StateID<ExtractedFileSetStore>> extractedFileSetStore();

	@Override
	public Set<StateID<?>> sources() {
		return extractedFileSetStore().isPresent()
			? StateID.setOf(archive(), distPackage(), name(), tempDir(), extractedFileSetStore().get())
			: StateID.setOf(archive(), distPackage(), name(), tempDir());
	}

	@Override
	public State<ExtractedFileSet> result(StateLookup lookup) {
		Package dist = lookup.of(distPackage());
		Archive archive = lookup.of(archive());
		Name name = lookup.of(name());
		TempDir tempDir = lookup.of(tempDir());

		Optional<ExtractedFileSetStore> store = extractedFileSetStore().map(lookup::of);

		Path destination = Try.apply(tempDir::createDirectory, name.value());
		return extractedFileSet(dist, archive, destination, store);
	}

	private State<ExtractedFileSet> extractedFileSet(Package dist, Archive archive, Path destination, Optional<ExtractedFileSetStore> extractedFileSetStore) {
		if (extractedFileSetStore.isPresent()) {
			ExtractedFileSetStore store = extractedFileSetStore.get();
			Optional<ExtractedFileSet> cachedExtractedFileSet = store.extractedFileSet(archive.value(), dist.fileSet());
			if (cachedExtractedFileSet.isPresent()) {
				return State.of(cachedExtractedFileSet.get());
			} else {
				ExtractFileSet extractor = dist.archiveType().extractor();
				ExtractedFileSet extractedFileSet = Try.get(() -> extractor.extract(destination, archive.value(), dist.fileSet()));

				return Try.supplier(() -> cachedFileSet(store.store(archive.value(), dist.fileSet(), extractedFileSet)))
					.fallbackTo(ex -> temporaryFileSet(extractedFileSet))
					.get();
			}
		} else {
			ExtractFileSet extractor = dist.archiveType().extractor();
			return temporaryFileSet(Try.get(() -> extractor.extract(destination, archive.value(), dist.fileSet())));
		}
	}

	private static State<ExtractedFileSet> temporaryFileSet(ExtractedFileSet current) {
		return State.of(current, fileSet -> Try.run(() -> Directories.deleteAll(fileSet.baseDir())));
	}

	private static State<ExtractedFileSet> cachedFileSet(ExtractedFileSet current) {
		return State.of(current);
	}

	public static ImmutableExtractPackage.Builder builder() {
		return ImmutableExtractPackage.builder();
	}

	public static ImmutableExtractPackage withDefaults() {
		return builder().build();
	}
}
