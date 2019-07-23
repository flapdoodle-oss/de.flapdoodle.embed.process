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
package de.flapdoodle.embed.process.example;

import java.io.IOException;

import org.junit.Test;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.distribution.IVersion;

public class TestExampleReadMeCode {

	/*
	 * ### Usage
	 */

	// #### Build a generic process starter
	@Test
	public void genericProcessStarter() throws IOException {

		IVersion version=new GenericVersion("2.1.1");

		String phantomjsOsIdentifier;
		ArchiveType phantomjsArchiveType;
		String phantomjsArchiveTypeExtension;
		String phantomjsExecutableName;

		Platform platform = Platform.detect();
		switch (platform) {
			case Linux:
				String osArchitecture = System.getProperty("os.arch");
				if ("x86_64".equals(osArchitecture) || "amd64".equals(osArchitecture)) {
					phantomjsOsIdentifier = "linux-x86_64";
				} else {
					phantomjsOsIdentifier = "linux-i686";
				}
				phantomjsArchiveType = ArchiveType.TBZ2;
				phantomjsArchiveTypeExtension = "tar.bz2";
				phantomjsExecutableName = "phantomjs";
				break;
			case OS_X:
				phantomjsOsIdentifier = "macosx";
				phantomjsArchiveType = ArchiveType.ZIP;
				phantomjsArchiveTypeExtension = "zip";
				phantomjsExecutableName = "phantomjs";
				break;
			case Windows:
				phantomjsOsIdentifier = "windows";
				phantomjsArchiveType = ArchiveType.ZIP;
				phantomjsArchiveTypeExtension = "zip";
				phantomjsExecutableName = "phantomjs.exe";
				break;
			default:
				throw new IllegalStateException("Unsupported operating system: " + platform);
		}


		IRuntimeConfig config = new GenericRuntimeConfigBuilder()
			.name("phantomjs")
			//https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-<OS identifier>.<archive extension>
			.downloadPath("https://bitbucket.org/ariya/phantomjs/downloads/")
			.packageResolver()
				.files(Distribution.detectFor(version), FileSet.builder().addEntry(FileType.Executable, phantomjsExecutableName).build())
				.archivePath(Distribution.detectFor(version), "phantomjs-"+version.asInDownloadPath()+ "-"+phantomjsOsIdentifier+"."+phantomjsArchiveTypeExtension)
				.archiveType(Distribution.detectFor(version), phantomjsArchiveType)
				.build()
			.build();


		GenericStarter starter = new GenericStarter(config);

		GenericExecuteable executable = starter.prepare(new GenericProcessConfig(version));

		GenericProcess process = executable.start();

		process.stop();

		executable.stop();
	}
}
