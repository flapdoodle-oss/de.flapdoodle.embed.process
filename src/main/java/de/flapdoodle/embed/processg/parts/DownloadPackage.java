package de.flapdoodle.embed.processg.parts;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.config.store.PackageResolver;
import de.flapdoodle.embed.processg.store.ArchiveStore;
import de.flapdoodle.embed.processg.store.Downloader;
import de.flapdoodle.reverse.State;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.StateLookup;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.types.ThrowingSupplier;
import de.flapdoodle.types.Try;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class DownloadPackage implements Transition<Archive> {

	protected abstract String name();

	protected abstract ArchiveStore archiveStore();

	@Value.Default
	protected Downloader downloader() {
		return Downloader.platformDefault();
	}

	@Value.Default
	protected ThrowingSupplier<Path, IOException> tempDir() {
		return () -> Files.createTempDirectory(name());
	}

	@Value.Default
	protected StateID<Distribution> distribution() {
		return StateID.of(Distribution.class);
	}

	@Value.Default
	protected StateID<Package> distPackage() {
		return StateID.of(Package.class);
	}

	@Override
	@Value.Default
	public StateID<Archive> destination() {
		return StateID.of(Archive.class);
	}

	@Override
	@Value.Auxiliary
	public final Set<StateID<?>> sources() {
		return StateID.setOf(distribution(), distPackage());
	}

	@Override
	public State<Archive> result(StateLookup lookup) {
		Distribution dist = lookup.of(distribution());
		Package distPackage = lookup.of(distPackage());

		Optional<Path> archive = archiveStore().archiveFor(name(), dist, distPackage.archiveType());
		if (archive.isPresent()) {
			return State.of(archive.map(Archive::of).get());
		} else {
			try {
				Path downloadedArchive = downloader().download(tempDir().get(), distPackage.url());
				Path storedArchive = archiveStore().store(name(), dist, distPackage.archiveType(), downloadedArchive);
				return State.of(Archive.of(storedArchive), it -> {
					Try.run(() -> Files.delete(it.value()));
				});
			}
			catch (IOException iox) {
				throw new IllegalStateException("download failed", iox);
			}
		}
	}

	public static ImmutableDownloadPackage.Builder builder() {
		return ImmutableDownloadPackage.builder();
	}

}
