package de.flapdoodle.embed.process.runtime;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessWithPidField extends Process {

	private int pid = 123;

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return 0;
	}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public void destroy() {

	}
}
