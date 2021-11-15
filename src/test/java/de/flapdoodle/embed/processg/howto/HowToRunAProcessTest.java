package de.flapdoodle.embed.processg.howto;

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
import de.flapdoodle.embed.processg.runtime.*;
import de.flapdoodle.reverse.InitLike;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionsAsGraph;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Start;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HowToRunAProcessTest {

	@Test
	public void sample() {
		PackageResolver packageResolver = (dist) -> {
			return DistributionPackage.builder().archiveType(ArchiveType.TBZ2)
				.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
				.archivePath("phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2").build();
		};

		Starter starter = Starter.withDefaults();
		Executable executable = Executable.with(Defaults.artifactStore(Defaults.genericDownloadConfig("phantomjs",
			"https://bitbucket.org/ariya/phantomjs/downloads/", packageResolver)));

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

		String dot = TransitionsAsGraph.edgeGraphAsDot("sample", TransitionsAsGraph.asGraphIncludingStartAndEnd(transitions));
		System.out.println("------------------------------");
		System.out.println(dot);
		System.out.println("------------------------------");

		InitLike init = InitLike.with(transitions);

		try (InitLike.ReachedState<Starter.Running> started = init.init(starter.destination())) {
			System.out.println("started: " + started.current());
		}
	}
}
