package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.extract.ExtractFileSet;
import de.flapdoodle.embed.processg.extract.ExtractedFileSet;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
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

	protected abstract String name();

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
		return StateID.setOf(archive(), distPackage());
	}

	@Value.Default
	protected ThrowingSupplier<Path, IOException> tempDir() {
		return () -> Files.createTempDirectory(name());
	}

	@Override
	public State<ExtractedFileSet> result(StateLookup lookup) {
		Package dist = lookup.of(distPackage());
		Archive archive = lookup.of(archive());
		Path destination = Try.get(tempDir());
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
}
