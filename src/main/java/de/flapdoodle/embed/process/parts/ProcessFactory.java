package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionsAsGraph;
import de.flapdoodle.reverse.InitLike;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Join;
import de.flapdoodle.reverse.edges.Start;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Immutable
public abstract class ProcessFactory {
	public abstract Version version();

	public abstract String baseDownloadUrl();

	public abstract Path artifactsBasePath();

	public abstract ArchiveTypeOfDistribution archiveTypeForDistribution();

	public abstract FileSetOfDistribution fileSetOfDistribution();

	public abstract UrlOfDistributionAndArchiveType urlOfDistributionAndArchiveType();

	public abstract LocalArtifactPathOfDistributionAndArchiveType localArtifactPathOfDistributionAndArchiveType();

	public abstract ArtifactPathForUrl artifactPathForUrl();

	@Auxiliary
	protected List<Transition<?>> routes() {
		return Arrays.asList(
				Start.to(Version.class).initializedWith(version()),
				Start.to(BaseUrl.class).initializedWith(BaseUrl.of(baseDownloadUrl())),
				Start.to(ArtifactsBasePath.class).initializedWith(ArtifactsBasePath.of(artifactsBasePath())),
				Derive.given(Version.class).state(Distribution.class).deriveBy(Distribution::detectFor),
				Derive.given(Distribution.class).state(ArchiveType.class).deriveBy(archiveTypeForDistribution()),
				Derive.given(Distribution.class).state(FileSet.class).deriveBy(fileSetOfDistribution()),
//				Merge3.given(BaseUrl.class).and(Distribution.class).and(ArchiveType.class).state(ArtifactUrl.class)
//						.deriveBy((baseUrl, distribution, archiveType) -> urlOfDistributionAndArchiveType().apply(baseUrl, distribution, archiveType)),
				Join.given(Distribution.class).and(ArchiveType.class).state(LocalArtifactPath.class)
						.deriveBy(localArtifactPathOfDistributionAndArchiveType())
//				Merge3.given(ArtifactsBasePath.class).and(ArtifactUrl.class).and(LocalArtifactPath.class).state(ArtifactPath.class)
//						.deriveBy((base, url, localPath) -> artifactPathForUrl().apply(base, url, localPath))
		);
	}

	@Auxiliary
	public String setupAsDot(String appName) {
		return TransitionsAsGraph.edgeGraphAsDot(appName, TransitionsAsGraph.asGraphIncludingStartAndEnd(routes()));
	}

	@Auxiliary
	public InitLike initLike() {
		return InitLike.with(routes());
	}

	public static ImmutableProcessFactory.Builder builder() {
		return ImmutableProcessFactory.builder();
	}
}
