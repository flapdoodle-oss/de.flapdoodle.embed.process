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
package de.flapdoodle.embed.process.runtime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.ExecutableProcessConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.io.file.Files;

public abstract class AbstractProcess<T extends ExecutableProcessConfig, E extends Executable<T, P>, P extends IStopable>
		implements IStopable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractProcess.class);

	public static final int TIMEOUT = 20000;

	private final T config;
	private final RuntimeConfig runtimeConfig;
	private final E executable;
	private ProcessControl process;
	private long processId;

	private boolean stopped = false;
	private boolean registeredJobKiller;
	
	private final Distribution distribution;

	private final File pidFile;

	public AbstractProcess(Distribution distribution, T config, RuntimeConfig runtimeConfig, E executable)
			throws IOException {
		this.config = config;
		this.runtimeConfig = runtimeConfig;
		this.executable = executable;
		this.distribution = distribution;
		// pid file needs to be set before ProcessBuilder is called
		this.pidFile = pidFile(this.executable.getFile().executable());

		// Refactor me - to much things done in this try/catch
		String nextCall="";
		try {

			nextCall="onBeforeProcess()";

			onBeforeProcess(runtimeConfig);

			nextCall="newProcessBuilder()";

			ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(
					runtimeConfig.commandLinePostProcessor().process(distribution,
							getCommandLine(distribution, config, this.executable.getFile())),
					getEnvironment(distribution, config, this.executable.getFile()), true);


			nextCall="onBeforeProcessStart()";

			onBeforeProcessStart(processBuilder, config, runtimeConfig);

			nextCall="start()";

			process = ProcessControl.start(config.supportConfig(), processBuilder);

			nextCall="writePidFile()";

			if (process.getPid() != null) {
				writePidFile(pidFile, process.getPid());
			}

			nextCall="addShutdownHook()";

			if (runtimeConfig.isDaemonProcess() && !executable.isRegisteredJobKiller()) {
				ProcessControl.addShutdownHook(new JobKiller());
				registeredJobKiller = true;
			}

			nextCall="onAfterProcessStart()";

			onAfterProcessStart(process, runtimeConfig);

		} catch (IOException iox) {
			logger.error("failed to call {}", nextCall, iox);
			logger.info("construct {}", config.toString());
			stop();
			throw iox;
		}
	}

	@Override
    public boolean isRegisteredJobKiller() {
        return registeredJobKiller;
    }

    protected File pidFile(File executableFile) {
		return new File(executableFile.getParentFile(),executableBaseName(executableFile.getName())+".pid");
	}

	protected File pidFile() {
		return pidFile;
	}

	private String executableBaseName(String name) {
		int idx=name.lastIndexOf('.');
		if (idx!=-1) {
			return name.substring(0,idx);
		}
		return name;
	}

	public T getConfig() {
		return config;
	}

	protected void onBeforeProcess(RuntimeConfig runtimeConfig) {

	}

	protected void onBeforeProcessStart(ProcessBuilder processBuilder, T config, RuntimeConfig runtimeConfig) {

	}

	protected void onAfterProcessStart(ProcessControl process, RuntimeConfig runtimeConfig) {
		ProcessOutput outputConfig = runtimeConfig.processOutput();
		Processors.connect(process.getReader(), outputConfig.output());
		Processors.connect(process.getError(), StreamToLineProcessor.wrap(outputConfig.error()));
	}

	protected abstract List<String> getCommandLine(Distribution distribution, T config, ExtractedFileSet exe)
			throws IOException;

	protected Map<String, String> getEnvironment(Distribution distribution, T config, ExtractedFileSet exe) {
		// default implementation, override to provide your own environment
		return new HashMap<>();
	}

	@Override
	public synchronized final void stop() {
		if (!stopped) {
			stopped = true;
			stopInternal();
			onAfterProcessStop(this.config, this.runtimeConfig);
			cleanupInternal();
			if (pidFile.exists() && !Files.forceDelete(pidFile)) {
				logger.warn("Could not delete pid file: {}", pidFile);
			}
		}
	}

	protected abstract void stopInternal();

	protected abstract void cleanupInternal();

	protected void onAfterProcessStop(T config, RuntimeConfig runtimeConfig) {

	}

	protected final void stopProcess() {
		if (process != null) {
			if (config.stopTimeoutInMillis().isPresent()) {
				process.stop(config.stopTimeoutInMillis().getAsLong());
			} else {
				process.stop();
			}
		}
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	protected void setProcessId(long processId) {
		this.processId = processId;
	}

	protected boolean sendKillToProcess() {
		return getProcessId() > 0 && Processes.killProcess(config.supportConfig(), distribution.platform(),
				StreamToLineProcessor.wrap(runtimeConfig.processOutput().commands()), getProcessId());
	}

	protected boolean sendTermToProcess() {
		return getProcessId() > 0 && Processes.termProcess(config.supportConfig(), distribution.platform(),
				StreamToLineProcessor.wrap(runtimeConfig.processOutput().commands()), getProcessId());
	}

	protected boolean tryKillToProcess() {
		return getProcessId() > 0 && Processes.tryKillProcess(config.supportConfig(), distribution.platform(),
				StreamToLineProcessor.wrap(runtimeConfig.processOutput().commands()), getProcessId());
	}

	public boolean isProcessRunning() {
		return getProcessId() > 0 && Processes.isProcessRunning(distribution.platform(), getProcessId());
	}

	public long getProcessId() {
		Long pid = process.getPid();
		return pid!=null ? pid : processId;
	}

	/**
	 *
	 */
	class JobKiller implements Runnable {

		@Override
		public void run() {
			AbstractProcess.this.stop();
		}
	}

	protected static int getPidFromFile(File pidFile) throws IOException {
		// wait for file to be created
		int tries = 0;
		while (!pidFile.exists() && tries < 5) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// ignore
			}
			logger.warn("Didn't find pid file in try {}, waiting 100ms...", tries);
			tries++;
		}
		// don't check file to be there. want to throw IOException if
		// something happens
		if (!pidFile.exists()) {
			throw new IOException("Could not find pid file " + pidFile);
		}

		// read the file, wait for the pid string to appear
		String fileContent = StringUtils.chomp(StringUtils.strip(new String(java.nio.file.Files.readAllBytes(pidFile.toPath()))));
		tries = 0;
		while (StringUtils.isBlank(fileContent) && tries < 5) {
			fileContent = StringUtils.chomp(StringUtils.strip(new String(java.nio.file.Files.readAllBytes(pidFile.toPath()))));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// ignore
			}
			tries++;
		}
		// check for empty file
		if (StringUtils.isBlank(fileContent)) {
			throw new IOException("Pidfile " + pidFile + "does not contain a pid. Waited for " + tries * 100 + "ms.");
		}
		// pidfile exists and has content
		try {
			return Integer.parseInt(fileContent);
		} catch (NumberFormatException e) {
			throw new IOException("Pidfile " + pidFile + "does not contain a valid pid. Content: " + fileContent);
		}
	}

	protected void writePidFile(File pidFile, long pid) throws IOException {
		Files.write(pid + "\n", pidFile);
	}
}
