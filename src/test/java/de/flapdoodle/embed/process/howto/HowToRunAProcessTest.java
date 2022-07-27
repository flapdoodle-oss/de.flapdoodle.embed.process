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
package de.flapdoodle.embed.process.howto;

import de.flapdoodle.embed.process.HttpServers;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.ContentHashExtractedFileSetStore;
import de.flapdoodle.embed.process.store.DownloadCache;
import de.flapdoodle.embed.process.store.ExtractedFileSetStore;
import de.flapdoodle.embed.process.store.LocalDownloadCache;
import de.flapdoodle.embed.process.transitions.*;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import de.flapdoodle.types.Try;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HowToRunAProcessTest {

	@RegisterExtension
	public static Recording recording = Recorder.with("HowToRunAProcess.md", TabSize.spaces(2));

	private static Map<String, String> resourceResponseMap = new LinkedHashMap<String, String>() {{
		put("/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2", "/archives/phantomjs/phantomjs-2.1.1-linux-x86_64.tar.bz2");
	}};

	@Test
	public void rebuildSample(@TempDir Path temp) throws IOException {
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
			String serverUrl = server.serverUrl() + "/ariya/phantomjs/downloads/";

			try (ProgressListeners.RemoveProgressListener ignored = ProgressListeners.setProgressListener(new StandardConsoleProgressListener())) {
//				String serverUrl = "https://bitbucket.org/ariya/phantomjs/downloads/";

				recording.begin();

				Transitions transitions = Transitions.from(
					InitTempDirectory.with(temp),

					Derive.given(de.flapdoodle.embed.process.nio.directories.TempDir.class)
							.state(ProcessWorkingDir.class)
							.with(tempDir -> {
									Path workDir = Try.get(() -> tempDir.createDirectory("workDir"));
									return State.of(ProcessWorkingDir.of(workDir), w -> {
										Try.run(() -> Files.deleteIfExists(w.value()));
									});
								}),

					Derive.given(de.flapdoodle.embed.process.nio.directories.TempDir.class)
						.state(DownloadCache.class)
						.deriveBy(tempDir -> new LocalDownloadCache(tempDir.value().resolve("archives")))
						.withTransitionLabel("setup DownloadCache"),

					Derive.given(de.flapdoodle.embed.process.nio.directories.TempDir.class)
						.state(ExtractedFileSetStore.class)
						.deriveBy(tempDir -> new ContentHashExtractedFileSetStore(tempDir.value().resolve("fileSets")))
						.withTransitionLabel("setup ExtractedFileSetStore"),

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

					DownloadPackage.withDefaults(),

					ExtractPackage.withDefaults()
						.withExtractedFileSetStore(StateID.of(ExtractedFileSetStore.class)),

					Executer.withDefaults()
				);

				TransitionWalker init = transitions.walker();

				String dot = Transitions.edgeGraphAsDot("sample", transitions.asGraph());
				recording.output("sample.dot", dot);

				try (TransitionWalker.ReachedState<Archive> withArchive = init.initState(StateID.of(Archive.class))) {
					try (TransitionWalker.ReachedState<ExtractedFileSet> withFileSet = withArchive.initState(StateID.of(ExtractedFileSet.class))) {
						try (TransitionWalker.ReachedState<ExecutedProcess> started = withFileSet.initState(StateID.of(ExecutedProcess.class))) {
							assertThat(started.current().returnCode())
								.isEqualTo(0);
						}
					}
				}

				recording.end();
			}
		}
	}
}
