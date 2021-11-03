package de.flapdoodle.embed.process.parts;


import java.nio.file.Path;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.transition.initlike.InitLike;
import de.flapdoodle.transition.initlike.InitRoutes;
import de.flapdoodle.transition.initlike.State;
import de.flapdoodle.transition.routes.RoutesAsGraph;
import de.flapdoodle.transition.routes.SingleDestination;

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
	protected InitRoutes<SingleDestination<?>> routes() {
		return InitRoutes.builder()
				.state(Version.class).isInitializedWith(version())
				.state(BaseUrl.class).isInitializedWith(BaseUrl.of(baseDownloadUrl()))
				.state(ArtifactsBasePath.class).isInitializedWith(ArtifactsBasePath.of(artifactsBasePath()))
				.given(Version.class).state(Distribution.class).isDerivedBy(Distribution::detectFor)
				.given(Distribution.class).state(ArchiveType.class).isDerivedBy(archiveTypeForDistribution())
				.given(Distribution.class).state(FileSet.class).isDerivedBy(fileSetOfDistribution())
				.given(BaseUrl.class, Distribution.class, ArchiveType.class).state(ArtifactUrl.class)
				.isReachedBy((baseUrl, distribution, archiveType) -> State
						.of(urlOfDistributionAndArchiveType().apply(baseUrl, distribution, archiveType)))
				.given(Distribution.class, ArchiveType.class).state(LocalArtifactPath.class)
				.isDerivedBy(localArtifactPathOfDistributionAndArchiveType())
				.given(ArtifactsBasePath.class, ArtifactUrl.class, LocalArtifactPath.class).state(ArtifactPath.class)
				.isReachedBy((base, url, localPath) -> State.of(artifactPathForUrl().apply(base, url, localPath)))
				.build();
	}

	@Auxiliary
	public String setupAsDot(String appName) {
		return RoutesAsGraph.routeGraphAsDot(appName, RoutesAsGraph.asGraphIncludingStartAndEnd(routes().all()));
	}

	@Auxiliary
	public InitLike initLike() {
		return InitLike.with(routes());
	}

	public static ImmutableProcessFactory.Builder builder() {
		return ImmutableProcessFactory.builder();
	}
}
