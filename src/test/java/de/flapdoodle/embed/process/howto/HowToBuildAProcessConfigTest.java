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
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.Package;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.io.directories.PersistentDir;
import de.flapdoodle.embed.process.transitions.ProcessFactory;
import de.flapdoodle.embed.process.types.*;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HowToBuildAProcessConfigTest {

	private static Map<String, String> resourceResponseMap = new LinkedHashMap<String, String>() {{
		put("/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2", "/archives/phantomjs/phantomjs-2.1.1-linux-x86_64.tar.bz2");
	}};

	@Test
	public void readableSample(@TempDir Path tempDir) throws IOException {
		if (Platform.detect().operatingSystem() != OS.Linux) {
			Assume.assumeTrue("works only on linux", true);
		} else {

			try (HttpServers.Server server = HttpServers.httpServer(getClass(), resourceResponseMap)) {
				String serverUrl = server.serverUrl() + "/ariya/phantomjs/downloads/";

				Version.GenericVersion version = Version.of("2.1.1");

				ProcessFactory processFactory = ProcessFactory.builder()
					.version(version)
					.persistentBaseDir(Start.to(PersistentDir.class)
						.initializedWith(PersistentDir.of(tempDir)))
					.name(Start.to(Name.class).initializedWith(Name.of("phantomjs")))
					.processArguments(Start.to(ProcessArguments.class).initializedWith(ProcessArguments.of(Arrays.asList("--help"))))
					.packageInformation(dist -> Package.builder()
						.archiveType(ArchiveType.TBZ2)
						.fileSet(FileSet.builder().addEntry(FileType.Executable, "phantomjs").build())
						.url(serverUrl + "phantomjs-" + dist.version().asInDownloadPath() + "-linux-x86_64.tar.bz2")
						.build())
					.build();

				if (true) {
					String dotFile = processFactory.setupAsDot("processBuild_sample");
					System.out.println("---------------------------");
					System.out.println(dotFile);
					System.out.println("---------------------------");
				}

				TransitionWalker initLike = processFactory.initLike();

				try (TransitionWalker.ReachedState<ExecutedProcess> init = initLike.initState(StateID.of(ExecutedProcess.class))) {
					System.out.println("started: " + init.current());

				}
			}
		}
	}
}