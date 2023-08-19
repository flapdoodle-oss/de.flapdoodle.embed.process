/*
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
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.ArchiveType;
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
import de.flapdoodle.embed.process.transitions.*;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.os.CommonOS;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.graph.TransitionGraph;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HowToRunAProcessTest {

	@RegisterExtension
	public static Recording recording = Recorder.with("HowToRunAProcess.md", TabSize.spaces(2));

	//	https://bitbucket.org/ariya/phantomjs/downloads/
	private static Map<String, String> resourceResponseMap = new LinkedHashMap<String, String>() {{
		put("/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2", "/archives/phantomjs/phantomjs-2.1.1-linux-x86_64.tar.bz2");
	}};

	@BeforeEach
	void skipIfNotLinux() {
		if (Platform.detect(CommonOS.list()).operatingSystem() != CommonOS.Linux) {
			Assume.assumeTrue("works only on linux", true);
		}
	}

	@Test
	public void genericSample(@org.junit.jupiter.api.io.TempDir Path temp) throws IOException {
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
			String serverUrl = server.serverUrl() + "/ariya/phantomjs/downloads/";

			recording.begin();

			Transitions transitions = Transitions.from(
				InitTempDirectory.with(temp),

				Derive.given(TempDir.class)
					.state(ProcessWorkingDir.class)
					.with(Directories.deleteOnTearDown(
						TempDir.createDirectoryWith("workDir"),
						ProcessWorkingDir::of
					)),

				Derive.given(de.flapdoodle.embed.process.io.directories.TempDir.class)
					.state(DownloadCache.class)
					.deriveBy(tempDir -> new LocalDownloadCache(tempDir.value().resolve("archives")))
					.withTransitionLabel("setup DownloadCache"),

				Derive.given(de.flapdoodle.embed.process.io.directories.TempDir.class)
					.state(ExtractedFileSetStore.class)
					.deriveBy(tempDir -> new ContentHashExtractedFileSetStore(tempDir.value().resolve("fileSets")))
					.withTransitionLabel("setup ExtractedFileSetStore"),

				Start.to(Name.class).initializedWith(Name.of("phantomjs")).withTransitionLabel("create Name"),

				Start.to(SupportConfig.class).initializedWith(SupportConfig.generic()).withTransitionLabel("create default"),
				Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default"),
				Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(phantomJsEnv())).withTransitionLabel("create empty env"),

				Start.to(Version.class).initializedWith(Version.of("2.1.1")).withTransitionLabel("set version"),
				Derive.given(Name.class).state(ProcessOutput.class)
					.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
					.withTransitionLabel("create named console"),

				Start.to(ProgressListener.class)
					.providedBy(StandardConsoleProgressListener::new)
					.withTransitionLabel("progressListener"),

				Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help")))
					.withTransitionLabel("create arguments"),

				Derive.given(Version.class).state(Distribution.class)
					.deriveBy(version -> Distribution.detectFor(CommonOS.list(), version))
					.withTransitionLabel("version + platform"),

				PackageOfDistribution.with(dist -> Package.builder()
					.archiveType(ArchiveType.TBZ2)
					.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
					.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
					.build()),

				DownloadPackage.withDefaults(),

				ExtractPackage.withDefaults()
					.withExtractedFileSetStore(StateID.of(ExtractedFileSetStore.class)),

				Executer.withDefaults()
			);

			TransitionWalker init = transitions.walker();

			String dot = TransitionGraph.edgeGraphAsDot("sample", transitions);
			recording.output("sample.dot", dot);
			recording.end();
//			recording.file("sample.dot.png", "HowToRunAProcess.png", asPng(dot));
			recording.file("sample.dot.svg", "HowToRunAProcess.svg", asSvg(dot));
			recording.begin();

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

	private byte[] asPng(String dot) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			Graphviz.fromString(dot)
				.width(3200)
				.render(Format.PNG)
				.toOutputStream(os);
			return os.toByteArray();
		}
	}

	private byte[] asSvg(String dot) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			Graphviz.fromString(dot)
//				.width(3200)
				.render(Format.SVG_STANDALONE)
				.toOutputStream(os);
			return os.toByteArray();
		}
	}

	private static Map<String, String> phantomJsEnv() {
		Map<String,String> phantomJsEnv=new LinkedHashMap<>();
		// see https://stackoverflow.com/questions/73004195/phantomjs-wont-install-autoconfiguration-error
		phantomJsEnv.put("OPENSSL_CONF","/dev/null");
		return phantomJsEnv;
	}

	@Test
	public void processFactorySample(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
		try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
			String serverUrl = server.serverUrl() + "/ariya/phantomjs/downloads/";

			recording.begin();

			Version.GenericVersion version = Version.of("2.1.1");

			ProcessFactory processFactory = ProcessFactory.builder()
				.version(version)
				.persistentBaseDir(Start.to(PersistentDir.class)
					.initializedWith(PersistentDir.of(tempDir)))
				.name(Start.to(Name.class).initializedWith(Name.of("phantomjs")))
				.processArguments(Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help"))))
				.processEnv(Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(phantomJsEnv())))
				.packageInformation(dist -> Package.builder()
					.archiveType(ArchiveType.TBZ2)
					.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
					.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
					.build())
				.osList(CommonOS::list)
				.build();

			TransitionWalker walker = processFactory.walker();
			String dot = TransitionGraph.edgeGraphAsDot("process factory sample", processFactory.transitions());
			recording.output("sample.dot", dot);
			recording.end();
			recording.file("sample.dot.svg", "HowToRunAProcessWithFactory.svg", asSvg(dot));
			recording.begin();

			try (TransitionWalker.ReachedState<ExecutedProcess> started = walker.initState(StateID.of(ExecutedProcess.class))) {
				assertThat(started.current().returnCode())
					.isEqualTo(0);
			}

			recording.end();
		}
	}
}
