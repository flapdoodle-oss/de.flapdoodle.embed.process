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
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.io.progress.ProgressListeners;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.processg.Resources;
import de.flapdoodle.embed.processg.config.store.DownloadConfig;
import de.flapdoodle.embed.processg.config.store.Package;
import de.flapdoodle.embed.processg.parts.Archive;
import de.flapdoodle.embed.processg.parts.DownloadPackage;
import de.flapdoodle.embed.processg.parts.ImmutablePackageOfDistribution;
import de.flapdoodle.embed.processg.parts.PackageOfDistribution;
import de.flapdoodle.embed.processg.runtime.*;
import de.flapdoodle.embed.processg.store.ArchiveStore;
import de.flapdoodle.embed.processg.store.Downloader;
import de.flapdoodle.embed.processg.store.LocalArchiveStore;
import de.flapdoodle.embed.processg.store.UrlConnectionDownloader;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Start;
import de.flapdoodle.types.Try;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
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

				ArchiveStore archiveStore = new LocalArchiveStore(temp);

				Starter starter = Starter.withDefaults();

				List<Transition<?>> transitions = Arrays.asList(
					Start.to(Version.class).initializedWith(Version.of("2.1.1")),
					Start.to(SupportConfig.class).initializedWith(SupportConfig.generic()),
					Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()),
					Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())),
					Start.to(ProcessOutput.class).initializedWith(ProcessOutput.namedConsole("phantomjs")),
					Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help"))),

					Derive.given(Version.class).state(Distribution.class).deriveBy(Distribution::detectFor),

					PackageOfDistribution.with(dist -> Package.builder()
						.archiveType(de.flapdoodle.embed.processg.extract.ArchiveType.TBZ2)
						.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
						.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
						.build()),

					DownloadPackage.builder()
						.name("phantomjs")
						.archiveStore(archiveStore)
						.build(),

					starter
				);

				String dot = Transitions.edgeGraphAsDot("sample", Transitions.asGraph(transitions));
				System.out.println("------------------------------");
				System.out.println(dot);
				System.out.println("------------------------------");

				TransitionWalker init = TransitionWalker.with(transitions);

				try (TransitionWalker.ReachedState<Archive> test = init.initState(StateID.of(Archive.class))) {
					System.out.println("test: " + test.current());
				}

				System.out.println("------------------------------");

				try (TransitionWalker.ReachedState<Archive> test = init.initState(StateID.of(Archive.class))) {
					System.out.println("second try: " + test.current());
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
				Derive.given(ExtractedFileSet.class).state(ProcessExecutable.class).deriveBy(fileSet -> ProcessExecutable.of(fileSet.executable())),
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
