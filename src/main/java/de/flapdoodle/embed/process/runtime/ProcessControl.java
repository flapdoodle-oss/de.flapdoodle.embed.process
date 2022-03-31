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

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ProcessControl {

	private static final long MAX_STOP_TIMEOUT_MS = 5000;
	private static Logger logger = LoggerFactory.getLogger(ProcessControl.class);

	private final Process process;

	private InputStreamReader reader;
	private final InputStreamReader error;

	private final Long pid;

	private final SupportConfig runtime;

	public ProcessControl(SupportConfig runtime, Process process) {
		this.process = process;
		this.runtime = runtime;
		reader = new InputStreamReader(this.process.getInputStream());
		error = new InputStreamReader(this.process.getErrorStream());
		pid = Processes.processId(this.process);
	}

	public Reader getReader() {
		return reader;
	}

	public InputStreamReader getError() {
		return error;
	}

	public int stop() {
		return stop(MAX_STOP_TIMEOUT_MS);
	}
	
	public int stop(long maxStopTimeoutMillis) {
		return waitForProcessGotKilled(maxStopTimeoutMillis);
	}

	private void closeIOAndDestroy() {
		if (process != null) {
			try {
				// streams need to be closed, otherwise process may block
				// see http://kylecartmell.com/?p=9
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();

			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			reader = null;
		}
	}

	private Integer stopOrDestroyProcess(long maxStopTimeoutMillis) {
		Integer returnCode=null;
		
		// set back to minimal defaults
		if (maxStopTimeoutMillis<3000) maxStopTimeoutMillis=MAX_STOP_TIMEOUT_MS;

		try {
			returnCode=process.exitValue();
		} catch (IllegalThreadStateException itsx) {
		    	logger.info("stopOrDestroyProcess: "+itsx.getMessage() +" "+((itsx.getCause()!=null) ? itsx.getCause() : "") );
			Callable<Integer> callable= process::waitFor;

			FutureTask<Integer> task = new FutureTask<>(callable);
			new Thread(task).start();

			boolean stopped=false;
			try {

              try {
                returnCode = task.get(100, TimeUnit.MILLISECONDS);
                stopped = true;
              } catch (ExecutionException | TimeoutException e) {
              }

              closeIOAndDestroy();

              try {
                returnCode = task.get(maxStopTimeoutMillis, TimeUnit.MILLISECONDS);
                stopped = true;
              } catch (ExecutionException | TimeoutException e) {
              }
            } catch (InterruptedException e) {
			  Thread.currentThread().interrupt();
            }

          if (!stopped)	{
//				logger.severe(""+runtime.getName()+" NOT exited, thats why we destroy");
				process.destroy();
			}
		}

		return returnCode;
	}

	private int waitForProcessGotKilled(long maxStopTimeoutMillis) {
		Integer retCode=stopOrDestroyProcess(maxStopTimeoutMillis);
		if (retCode==null) {
			String message = "\n\n" + "----------------------------------------------------\n"
					+ "Something bad happened. We couldn't kill "+runtime.name()+" process, and tried a lot.\n"
					+ "If you want this problem solved you can help us if you open a new issue.\n" + "\n"
					+ "Follow this link:\n" + runtime.supportUrl() +
					"\n"
					+ "Thank you:)\n" + "----------------------------------------------------\n\n";
			throw new IllegalStateException("Couldn't kill "+runtime.name()+" process!" + message);
		}
		return retCode;
	}

	public static ProcessControl fromCommandLine(SupportConfig runtime, List<String> commandLine, boolean redirectErrorStream)
			throws IOException {
		ProcessBuilder processBuilder = newProcessBuilder(commandLine, redirectErrorStream);
		return start(runtime, processBuilder);
	}

	public static ProcessControl start(SupportConfig runtime, ProcessBuilder processBuilder) throws IOException {
		return new ProcessControl(runtime, processBuilder.start());
	}

	public static ProcessBuilder newProcessBuilder(List<String> commandLine, boolean redirectErrorStream) {
		return newProcessBuilder(commandLine, new HashMap<>(), redirectErrorStream);
	}

	public static ProcessBuilder newProcessBuilder(List<String> commandLine, Map<String,String> environment, boolean redirectErrorStream) {
		ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
		if (!environment.isEmpty()){
			processBuilder.environment().putAll(environment);
		}
		if (redirectErrorStream)
			processBuilder.redirectErrorStream();
		return processBuilder;
	}

	public static boolean executeCommandLine(
		SupportConfig support,
		List<String> commandLine,
		Consumer<ProcessControl> beforeStop,
		StreamProcessor output,
		boolean redirectErrorStream
	) {
		boolean ret;

		try {
			ProcessControl process = fromCommandLine(support, commandLine, redirectErrorStream);
			Processors.connect(process.getReader(), output);
			beforeStop.accept(process);
			ret = process.stop() == 0;
			logger.info("execSuccess: {} {}", ret, commandLine);
			return ret;
		} catch (IOException e) {
			logger.error("" + commandLine, e);
		}
		return false;
	}

	@Deprecated
	/**
	 * @see ProcessControl#waitFor(long, TimeUnit)
	 */
	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
		return process.waitFor(timeout, unit);
	}

	public static void addShutdownHook(Runnable runnable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runnable));
	}

	public Long getPid() {
		return pid;
	}
}
