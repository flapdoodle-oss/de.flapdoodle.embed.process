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
package de.flapdoodle.embed.processg.howto;

import de.flapdoodle.embed.process.HttpServers;
import de.flapdoodle.embed.process.config.Defaults;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.types.Archive;
import de.flapdoodle.embed.processg.parts.*;
import de.flapdoodle.embed.processg.runtime.*;
import de.flapdoodle.embed.processg.store.*;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Start;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HowToRunAProcessTest {
	private static Map<String, String> resourceResponseMap = new LinkedHashMap<String, String>() {{
		put("/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2", "/archives/phantomjs/phantomjs-2.1.1-linux-x86_64.tar.bz2");
	}};

	@Test
	public void rebuildSample(@TempDir Path temp) throws IOException {
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
			String serverUrl = server.serverUrl()+"/ariya/phantomjs/downloads/";

			try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
//				String serverUrl = "https://bitbucket.org/ariya/phantomjs/downloads/";

				ArchiveStore archiveStore = new LocalArchiveStore(temp.resolve("archives"));
				ExtractedFileSetStore extractedFileSetStore = new ContentHashExtractedFileSetStore(temp.resolve("fileSets"));

				Starter starter = Starter.withDefaults();

				List<Transition<?>> transitions = Arrays.asList(
					InitTempDirectory.with(temp),
					
					Start.to(Name.class).initializedWith(Name.of("phantomjs")).withTransitionLabel("create Name"),

					Start.to(SupportConfig.class).initializedWith(SupportConfig.generic()).withTransitionLabel("create default"),
					Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default"),
					Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())).withTransitionLabel("create empty env"),

					Start.to(Version.class).initializedWith(Version.of("2.1.1")).withTransitionLabel("set version"),
					Derive.given(Name.class).state(ProcessOutput.class)
						.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
						.withTransitionLabel("create named console"),

					Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help")))
						.withTransitionLabel("create arguments"),

					Derive.given(Version.class).state(Distribution.class)
						.deriveBy(Distribution::detectFor)
						.withTransitionLabel("version + platform"),

					PackageOfDistribution.with(dist -> Package.builder()
						.archiveType(de.flapdoodle.embed.process.archives.ArchiveType.TBZ2)
						.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
						.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
						.build()),

					DownloadPackage.with(archiveStore),

					ExtractPackage.withDefaults()
						.withExtractedFileSetStore(extractedFileSetStore),

					starter
				);

				TransitionWalker init = TransitionWalker.with(transitions);

				String dot = Transitions.edgeGraphAsDot("sample", Transitions.asGraph(transitions));
				System.out.println("------------------------------");
				System.out.println(dot);
				System.out.println("------------------------------");

//				try (TransitionWalker.ReachedState<de.flapdoodle.embed.process.archives.ExtractedFileSet> test = init.initState(StateID.of(de.flapdoodle.embed.process.archives.ExtractedFileSet.class))) {
//					System.out.println("fileSet: " + test.current());
//				}

				try (TransitionWalker.ReachedState<Archive> withArchive = init.initState(StateID.of(Archive.class))) {
					System.out.println("with archive: " + withArchive.current());

					try (TransitionWalker.ReachedState<ExtractedFileSet> withFileSet = withArchive.initState(StateID.of(ExtractedFileSet.class))) {
						System.out.println("with fileSet: " + withFileSet.current());
					}

					try (TransitionWalker.ReachedState<ExtractedFileSet> withFileSet = withArchive.initState(StateID.of(ExtractedFileSet.class))) {
						System.out.println("with fileSet(2): " + withFileSet.current());
					}

					try (TransitionWalker.ReachedState<Starter.Running> started = withArchive.initState(starter.destination())) {
						System.out.println("started: " + started.current());
					}
				}

				if (false) {
					try (TransitionWalker.ReachedState<Starter.Running> started = init.initState(starter.destination())) {
						System.out.println("started: " + started.current());
					}
				}
			}
		}
	}

	@Test
	@Disabled
	public void sample() throws IOException {
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
			String serverUrl = server.serverUrl() + "/ariya/phantomjs/downloads/";

			PackageResolver packageResolver = (dist) -> {
				return DistributionPackage.builder().archiveType(ArchiveType.TBZ2)
					.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
					.archivePath("phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2").build();
			};

			Starter starter = Starter.withDefaults();
			Executable executable = Executable.with(Defaults.artifactStore(Defaults.genericDownloadConfig("phantomjs",
				serverUrl /*"https://bitbucket.org/ariya/phantomjs/downloads/"*/, packageResolver)));

			List<Transition<?>> transitions = Arrays.asList(
				Start.to(Version.class).initializedWith(Version.of("2.1.1")),
				Start.to(SupportConfig.class).initializedWith(SupportConfig.generic()),
				Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()),
				Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())),
				Start.to(ProcessOutput.class).initializedWith(ProcessOutput.namedConsole("phantomjs")),
				Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help"))),
				Derive.given(de.flapdoodle.embed.process.extract.ExtractedFileSet.class).state(ProcessExecutable.class).deriveBy(fileSet -> ProcessExecutable.of(fileSet.executable())),
				Derive.given(Version.class).state(Distribution.class).deriveBy(Distribution::detectFor),

				//Start.to(ProcessExecutable.class).initializedWith(ProcessExecutable.of())
				executable,
				starter
			);

			String dot = Transitions.edgeGraphAsDot("sample", Transitions.asGraph(transitions));
			System.out.println("------------------------------");
			System.out.println(dot);
			System.out.println("------------------------------");

			TransitionWalker init = TransitionWalker.with(transitions);

			try (TransitionWalker.ReachedState<Starter.Running> started = init.initState(starter.destination())) {
				System.out.println("started: " + started.current());
			}
		}
	}
}
