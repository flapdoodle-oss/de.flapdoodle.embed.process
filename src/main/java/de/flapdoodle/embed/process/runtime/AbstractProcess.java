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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.io.file.Files;

public abstract class AbstractProcess<T extends IExecutableProcessConfig, E extends Executable<T, P>, P extends IStopable>
		implements IStopable {

	private static Logger logger = LoggerFactory.getLogger(AbstractProcess.class);

	public static final int TIMEOUT = 20000;

	private final T config;
	private final IRuntimeConfig runtimeConfig;
	private final E executable;
	private ProcessControl process;
	private int processId;

	private boolean stopped = false;

	private Distribution distribution;

	private File pidFile;

	public AbstractProcess(Distribution distribution, T config, IRuntimeConfig runtimeConfig, E executable)
			throws IOException {
		this.config = config;
		this.runtimeConfig = runtimeConfig;
		this.executable = executable;
		this.distribution = distribution;
		// pid file needs to be set before ProcessBuilder is called
		this.pidFile = pidFile(this.executable.getFile().executable());
		
		ProcessOutput outputConfig = runtimeConfig.getProcessOutput();

		// Refactor me - to much things done in this try/catch
		String nextCall="";
		try {

			nextCall="onBeforeProcess()";

			onBeforeProcess(runtimeConfig);
			
			nextCall="newProcessBuilder()";

			ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(
					runtimeConfig.getCommandLinePostProcessor().process(distribution,
							getCommandLine(distribution, config, this.executable.getFile())),
					getEnvironment(distribution, config, this.executable.getFile()), true);

			
			nextCall="onBeforeProcessStart()";

			onBeforeProcessStart(processBuilder, config, runtimeConfig);

			nextCall="start()";

			process = ProcessControl.start(config.supportConfig(), processBuilder);
			
			nextCall="writePidFile()";

			writePidFile(pidFile, process.getPid());
			
			nextCall="addShutdownHook()";

			if (runtimeConfig.isDaemonProcess()) {
				ProcessControl.addShutdownHook(new JobKiller());
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

	protected File pidFile(File executeableFile) {
		return new File(executeableFile.getParentFile(),executableBaseName(executeableFile.getName())+".pid");
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

	protected void onBeforeProcess(IRuntimeConfig runtimeConfig) throws IOException {

	}

	protected void onBeforeProcessStart(ProcessBuilder processBuilder, T config, IRuntimeConfig runtimeConfig) {

	}

	protected void onAfterProcessStart(ProcessControl process, IRuntimeConfig runtimeConfig) throws IOException {
		ProcessOutput outputConfig = runtimeConfig.getProcessOutput();
		Processors.connect(process.getReader(), outputConfig.getOutput());
		Processors.connect(process.getError(), StreamToLineProcessor.wrap(outputConfig.getError()));
	}

	protected abstract List<String> getCommandLine(Distribution distribution, T config, IExtractedFileSet exe)
			throws IOException;

	protected Map<String, String> getEnvironment(Distribution distribution, T config, IExtractedFileSet exe) {
		// default implementation, override to provide your own environment
		return new HashMap<String, String>();
	}

	public synchronized final void stop() {
		if (!stopped) {
			stopped = true;
			stopInternal();
			onAfterProcessStop(this.config, this.runtimeConfig);
			cleanupInternal();
			if (!Files.forceDelete(pidFile)) {
				logger.warn("Could not delete pid file: {}", pidFile);
			}
		}
	}

	protected abstract void stopInternal();

	protected abstract void cleanupInternal();

	protected void onAfterProcessStop(T config, IRuntimeConfig runtimeConfig) {

	}

	protected final void stopProcess() {
		if (process != null)
			process.stop();
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	protected void setProcessId(int processId) {
		this.processId = processId;
	}

	protected boolean sendKillToProcess() {
		if (processId > 0) {
			return Processes.killProcess(config.supportConfig(), distribution.getPlatform(),
					StreamToLineProcessor.wrap(runtimeConfig.getProcessOutput().getCommands()), processId);
		}
		return false;
	}

	protected boolean sendTermToProcess() {
		if (processId > 0) {
			return Processes.termProcess(config.supportConfig(), distribution.getPlatform(),
					StreamToLineProcessor.wrap(runtimeConfig.getProcessOutput().getCommands()), processId);
		}
		return false;
	}

	protected boolean tryKillToProcess() {
		if (processId > 0) {
			return Processes.tryKillProcess(config.supportConfig(), distribution.getPlatform(),
					StreamToLineProcessor.wrap(runtimeConfig.getProcessOutput().getCommands()), processId);
		}
		return false;
	}

	public boolean isProcessRunning() {
		if (getProcessId() > 0) {
			return Processes.isProcessRunning(distribution.getPlatform(), getProcessId());
		}
		return false;
	}

	public int getProcessId() {
		Integer pid = process.getPid();
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
		String fileContent = StringUtils.chomp(StringUtils.strip(FileUtils.readFileToString(pidFile)));
		tries = 0;
		while (StringUtils.isBlank(fileContent) && tries < 5) {
			fileContent = StringUtils.chomp(StringUtils.strip(FileUtils.readFileToString(pidFile)));
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

	protected void writePidFile(File pidFile, int pid) throws IOException {
		Files.write(pid + "\n", pidFile);
	}
}
