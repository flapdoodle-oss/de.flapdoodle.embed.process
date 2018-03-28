package de.flapdoodle.embed.process.runtime;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class ProcessesTest {

	@Test
	public void processId() throws IOException {
		Process process = new ProcessBuilder("sleep", "10").start();
		assertNotNull(Processes.processId(process));
	}

}
