package de.flapdoodle.embed.processg.howto;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.processg.runtime.*;
import de.flapdoodle.reverse.InitLike;
import de.flapdoodle.reverse.edges.Derive;
import de.flapdoodle.reverse.edges.Start;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class HowToRunAProcessTest {

		@Test
		public void sample() {
				Starter starter = Starter.withDefaults();

				InitLike init = InitLike.with(Arrays.asList(
						Start.to(SupportConfig.class).initializedWith(SupportConfig.generic()),
						Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()),
						Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())),
						Start.to(ProcessOutput.class).initializedWith(ProcessOutput.namedConsole("phantomjs")),
						Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help"))),

						//Start.to(ProcessExecutable.class).initializedWith(ProcessExecutable.of())
						starter
				));

				try (InitLike.ReachedState<Starter.Running> started = init.init(starter.destination())) {
						System.out.println("started: "+started.current());
				}
		}
}
