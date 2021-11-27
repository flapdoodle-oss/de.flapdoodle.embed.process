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
package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.extract.ExtractFileSet;
import de.flapdoodle.embed.processg.extract.ExtractedFileSet;
import de.flapdoodle.embed.processg.runtime.Name;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.types.ThrowingFunction;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;

@Value.Immutable
public abstract class ExtractPackage implements Transition<ExtractedFileSet> {

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

	@Override
	public Set<StateID<?>> sources() {
		return StateID.setOf(archive(), distPackage(), name());
	}

	@Value.Default
	protected ThrowingFunction<String, Path, IOException> tempDir() {
		return Files::createTempDirectory;
	}

	@Override
	public State<ExtractedFileSet> result(StateLookup lookup) {
		Package dist = lookup.of(distPackage());
		Archive archive = lookup.of(archive());
		Name name = lookup.of(name());

		Path destination = Try.apply(tempDir(), name.value());
		ExtractFileSet extractor = dist.archiveType().extractor();

		ExtractedFileSet extractedFileSet = Try.get(() -> extractor.extract(destination, archive.value(), dist.fileSet()));

		return State.of(extractedFileSet, fileSet -> {
			Try.run(() -> Files.walk(fileSet.baseDir())
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete));
		});
	}

	public static ImmutableExtractPackage.Builder builder() {
		return ImmutableExtractPackage.builder();
	}

	public static ImmutableExtractPackage withDefaults() {
		return builder().build();
	}
}
