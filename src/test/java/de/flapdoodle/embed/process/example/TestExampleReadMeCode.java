package de.flapdoodle.embed.process.example;

import java.io.IOException;

import org.junit.Test;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.distribution.IVersion;

public class TestExampleReadMeCode {

	/*
	 * ### Usage
	 */

	// #### Build a generic process starter
	@Test
	public void genericProcessStarter() throws IOException {

		IVersion version=new GenericVersion("1.8.2");
		
		IRuntimeConfig config = new GenericRuntimeConfigBuilder()
			.name("phantomjs")
			.downloadPath("http://phantomjs.googlecode.com/files/")
			.packageResolver()
				.executeable(Distribution.detectFor(version), "phantomjs")
				.archivePath(Distribution.detectFor(version), "phantomjs-"+version.asInDownloadPath()+"-linux-x86_64.tar.bz2")
				.archiveType(Distribution.detectFor(version), ArchiveType.TBZ2)
				.build()
			.build();
		
		
		GenericStarter starter = new GenericStarter(config);
		
		GenericExecuteable executable = starter.prepare(new GenericProcessConfig(version));
		
		GenericProcess process = executable.start();
		
		process.stop();
		
		executable.stop();
	}
}
