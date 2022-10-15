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

import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.directories.PersistentDir;
import de.flapdoodle.embed.process.io.directories.TempDir;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;

import java.util.Collections;
import java.util.function.Function;

@Immutable
public abstract class ProcessFactory {

	public abstract Version version();

	protected abstract Transition<Name> name();

	@Value.Default
	protected Transition<TempDir> initTempDirectory() {
		return InitTempDirectory.withPlatformTempRandomSubDir();
	}

	@Value.Default
	protected Transition<ProcessWorkingDir> processWorkingDir() {
		return Derive.given(TempDir.class)
			.state(ProcessWorkingDir.class)
			.with(Directories.deleteOnTearDown(
				TempDir.createDirectoryWith("workDir"),
				ProcessWorkingDir::of
			));
	}

	@Value.Default
	protected Start<ProcessConfig> processConfig() {
		return Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default");
	}

	@Value.Default
	protected Transition<ProcessEnv> processEnv() {
		return Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())).withTransitionLabel("create empty env");
	}

	protected abstract Transition<ProcessArguments> processArguments();

	@Value.Default
	protected Transition<ProcessOutput> processOutput() {
		return Derive.given(Name.class).state(ProcessOutput.class)
			.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
			.withTransitionLabel("create named console");
	}

	@Value.Default
	protected Transition<ProgressListener> progressListener() {
		return Start.to(ProgressListener.class)
			.providedBy(StandardConsoleProgressListener::new)
			.withTransitionLabel("progressListener");
	}

	@Value.Default
	protected Transition<SupportConfig> supportConfig() {
		return Start.to(SupportConfig .class).initializedWith(SupportConfig.generic());
	}

	protected abstract Transition<PersistentDir> persistentBaseDir();

	@Value.Default
	protected Transition<DownloadCache> downloadCache() {
		return Derive.given(PersistentDir.class)
			.state(DownloadCache.class)
			.deriveBy(storeBaseDir -> new LocalDownloadCache(storeBaseDir.value().resolve("archives")))
			.withTransitionLabel("downloadCache");
	}

	@Value.Default
	protected Transition<ExtractedFileSetStore> extractedFileSetStore() {
		return Derive.given(PersistentDir.class)
			.state(ExtractedFileSetStore.class)
			.deriveBy(baseDir -> new ContentHashExtractedFileSetStore(baseDir.value().resolve("fileSets")))
			.withTransitionLabel("extractedFileSetStore");
	}

	@Value.Default
	protected Transition<ExtractedFileSet> extractPackage() {
		return ExtractPackage.withDefaults()
			.withExtractedFileSetStore(StateID.of(ExtractedFileSetStore.class));
	}

	@Value.Default
	protected Transition<Archive> downloadPackage() {
		return DownloadPackage.withDefaults();
	}

	@Value.Default
	protected Transition<Distribution> distribution() {
		return Derive.given(Version.class).state(Distribution.class)
			.deriveBy(Distribution::detectFor);
	}

	protected abstract Function<Distribution, Package> packageInformation();

	@Value.Default
	protected Transition<ExecutedProcess> executer() {
		return Executer.withDefaults();
	}

	@Auxiliary
	public Transitions transitions() {
		return Transitions.from(
			initTempDirectory(),
			processWorkingDir(),
			name(),
			Start.to(Version.class).initializedWith(version()),
			supportConfig(),
			processConfig(),
			processEnv(),
			processArguments(),
			processOutput(),
			progressListener(),
			persistentBaseDir(),
			Derive.given(Distribution.class)
				.state(Package.class)
				.deriveBy(packageInformation()),
			distribution(),
			downloadCache(),
			extractedFileSetStore(),
			extractPackage(),
			downloadPackage(),
			executer()
		);
	}

	@Auxiliary
	public TransitionWalker walker() {
		return transitions().walker();
	}

	public static ImmutableProcessFactory.Builder builder() {
		return ImmutableProcessFactory.builder();
	}
}
