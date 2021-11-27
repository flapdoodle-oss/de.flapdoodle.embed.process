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
package de.flapdoodle.embed.process.parts;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
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

	public abstract ArtifactUrlOfDistributionAndArchiveType artifactUrlOfDistributionAndArchiveType();

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
		return Transitions.edgeGraphAsDot(appName, Transitions.asGraph(routes()));
	}

	@Auxiliary
	public TransitionWalker initLike() {
		return TransitionWalker.with(routes());
	}

	public static ImmutableProcessFactory.Builder builder() {
		return ImmutableProcessFactory.builder();
	}
}
