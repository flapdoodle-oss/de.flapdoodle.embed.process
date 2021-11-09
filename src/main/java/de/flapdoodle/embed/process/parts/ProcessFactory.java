package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.transition.initlike.Edge;
import de.flapdoodle.transition.initlike.EdgesAsGraph;
import de.flapdoodle.transition.initlike.InitLike;
import de.flapdoodle.transition.initlike.edges.Depends;
import de.flapdoodle.transition.initlike.edges.Merge2;
import de.flapdoodle.transition.initlike.edges.Merge3;
import de.flapdoodle.transition.initlike.edges.Start;
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
	protected List<Edge<?>> routes() {
		return Arrays.asList(
				Start.to(Version.class).initializedWith(version()),
				Start.to(BaseUrl.class).initializedWith(BaseUrl.of(baseDownloadUrl())),
				Start.to(ArtifactsBasePath.class).initializedWith(ArtifactsBasePath.of(artifactsBasePath())),
				Depends.given(Version.class).state(Distribution.class).deriveBy(Distribution::detectFor),
				Depends.given(Distribution.class).state(ArchiveType.class).deriveBy(archiveTypeForDistribution()),
				Depends.given(Distribution.class).state(FileSet.class).deriveBy(fileSetOfDistribution()),
				Merge3.given(BaseUrl.class).and(Distribution.class).and(ArchiveType.class).state(ArtifactUrl.class)
						.deriveBy((baseUrl, distribution, archiveType) -> urlOfDistributionAndArchiveType().apply(baseUrl, distribution, archiveType)),
				Merge2.given(Distribution.class).and(ArchiveType.class).state(LocalArtifactPath.class)
						.deriveBy(localArtifactPathOfDistributionAndArchiveType()),
				Merge3.given(ArtifactsBasePath.class).and(ArtifactUrl.class).and(LocalArtifactPath.class).state(ArtifactPath.class)
						.deriveBy((base, url, localPath) -> artifactPathForUrl().apply(base, url, localPath))
		);
	}

	@Auxiliary
	public String setupAsDot(String appName) {
		return EdgesAsGraph.edgeGraphAsDot(appName, EdgesAsGraph.asGraphIncludingStartAndEnd(routes()));
	}

	@Auxiliary
	public InitLike initLike() {
		return InitLike.with(routes());
	}

	public static ImmutableProcessFactory.Builder builder() {
		return ImmutableProcessFactory.builder();
	}
}
