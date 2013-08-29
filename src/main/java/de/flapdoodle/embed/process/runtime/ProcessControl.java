/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.process.ProcessConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;

/**
 *
 */
public class ProcessControl {

	private static Logger logger = Logger.getLogger(ProcessControl.class.getName());
	public static final int SLEEPT_TIMEOUT = 10;

	private Process process;

	private InputStreamReader reader;
	private InputStreamReader error;

	private Integer pid;
	private File workDir;
	private List<String> commands;

	private ISupportConfig runtime;

	public ProcessControl(ISupportConfig runtime, List<String> commands, File workDir, Process process) {
		this.process = process;
		this.runtime = runtime;
		this.workDir = workDir;
		this.commands = commands;
		reader = new InputStreamReader(this.process.getInputStream());
		error = new InputStreamReader(this.process.getErrorStream());
		pid = getProcessID();
	}

	public Reader getReader() {
		return reader;
	}

	public InputStreamReader getError() {
		return error;
	}

	public int stop() {
		return waitForProcessGotKilled();
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
				logger.severe(e.getMessage());
			}
			reader = null;
		}
	}

	private Integer stopOrDestroyProcess() {
		Integer returnCode=null;
		
		try {
			returnCode=process.exitValue();
		} catch (IllegalThreadStateException itsx) {
		    	logger.info("stopOrDestroyProcess: "+itsx.getMessage() +" "+itsx.getCause() );
			Callable<Integer> callable=new Callable<Integer>() {
				
				@Override
				public Integer call() throws Exception {
					return process.waitFor();
				}
			};
			FutureTask<Integer> task = new FutureTask<Integer>(callable);
			new Thread(task).start();

			boolean stopped=false;
			try {
				returnCode=task.get(100, TimeUnit.MILLISECONDS);
				stopped=true;
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (TimeoutException e) {
			}
			
			closeIOAndDestroy();
			
			try {
				returnCode=task.get(900, TimeUnit.MILLISECONDS);
				stopped=true;
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (TimeoutException e) {
			}

			try {
				returnCode=task.get(2000, TimeUnit.MILLISECONDS);
				stopped=true;
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (TimeoutException e) {
			}

			if (!stopped)	{
//				logger.severe(""+runtime.getName()+" NOT exited, thats why we destroy");
				process.destroy();
			}
		}
		
		return returnCode;
	}

	//CHECKSTYLE:OFF

	/**
	 * It may happen in tests, that the process is currently using some files in
	 * the temp directory, e.g. journal files (journal/j._0) and got killed at
	 * that time, so it takes a bit longer to kill the process. So we just wait
	 * for a second (in 10 ms steps) that the process got really killed.
	 */
	private int waitForProcessGotKilled() {
//		final ProcessState state = new ProcessState();

//		final Timer timer = new Timer();
//		timer.scheduleAtFixedRate(new TimerTask() {
//
//			public void run() {
//				try {
//					state.returnCode = process.waitFor();
//				} catch (InterruptedException e) {
//					logger.severe(e.getMessage());
//				} finally {
//					state.setKilled(true);
//					timer.cancel();
//				}
//			}
//		}, 0, 10);
//		// wait for max. 1 second that process got killed
		Integer retCode=stopOrDestroyProcess();

//		int countDown = 100;
//		while (!state.isKilled() && (countDown-- > 0))
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				logger.severe(e.getMessage());
//				Thread.currentThread().interrupt();
//			}
		if (retCode==null) {
//			timer.cancel();
			String message = "\n\n" + "----------------------------------------------------\n"
					+ "Something bad happend. We couldn't kill "+runtime.getName()+" process, and tried a lot.\n"
					+ "If you want this problem solved you can help us if you open a new issue.\n" + "\n"
					+ "Follow this link:\n" + runtime.getSupportUrl() +
					"\n"
					+ "Thank you:)\n" + "----------------------------------------------------\n\n";
			throw new IllegalStateException("Couldn't kill "+runtime.getName()+" process!" + message);
		}
		return retCode;
	}

	//CHECKSTYLE:ON
	public static ProcessControl fromCommandLine(ISupportConfig runtime, List<String> commandLine, boolean redirectErrorStream)
			throws IOException {
		ProcessBuilder processBuilder = newProcessBuilder(commandLine, redirectErrorStream);
		return start(runtime, processBuilder);
	}

	public static ProcessControl start(ISupportConfig runtime, ProcessBuilder processBuilder) throws IOException {
		return new ProcessControl(runtime, processBuilder.command(), processBuilder.directory(), processBuilder.start());
	}

	public static ProcessBuilder newProcessBuilder(List<String> commandLine, boolean redirectErrorStream) {
		return newProcessBuilder(commandLine,new HashMap<String,String>(), redirectErrorStream);
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

	public static boolean executeCommandLine(ISupportConfig support, String label, ProcessConfig processConfig) {
		boolean ret = false;

		List<String> commandLine = processConfig.getCommandLine();
		try {
			ProcessControl process = fromCommandLine(support, processConfig.getCommandLine(), processConfig.getError() == null);
			Processors.connect(process.getReader(), processConfig.getOutput());
			Thread.sleep(SLEEPT_TIMEOUT);
			ret = process.stop() == 0;
			logger.info("execSuccess: " + ret + " " + commandLine);
			return ret;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "" + commandLine, e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "" + commandLine, e);
		}
		return false;
	}

	public static boolean killProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
		if ((platform == Platform.Linux) || (platform == Platform.OS_X)) {
			return executeCommandLine(support, "[kill process]",
					new ProcessConfig(Collections.newArrayList("kill", "-2", "" + pid), output));
		}
		return false;
	}
	
	public static boolean termProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
	    if ((platform == Platform.Linux) || (platform == Platform.OS_X)) {
		return executeCommandLine(support, "[term process]",
			new ProcessConfig(Collections.newArrayList("kill", "" + pid), output));
	    }
	    return false;
	}

	public static boolean tryKillProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
		if (platform == Platform.Windows) {
			return executeCommandLine(support, "[taskkill process]",
					new ProcessConfig(Collections.newArrayList("taskkill", "/F", "/pid", "" + pid), output));
		}
		return false;
	}

	private Integer getProcessID() {
		// that should work on Linux/MacOS for the most part
		Class<?> clazz = process.getClass();
		try {
			if (clazz.getName().equals("java.lang.UNIXProcess")) {
				Field pidField = clazz.getDeclaredField("pid");
				pidField.setAccessible(true);
				Object value = pidField.get(process);
				if (value instanceof Integer) {
					logger.fine("Detected pid: " + value);
					return (Integer) value;
				}
			}
		} catch (SecurityException sx) {
			sx.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		// Windows: try to use Sigar to find out process id
		long pid;
		try {
			Sigar sigar = new Sigar();
			// supply command name, this will be something like
			// extract<md5hash>, so somewhat reliable. could also query for
			// workdir but would need to infer that from parent java process.
			ProcessFinder find = new ProcessFinder(sigar);
			String exeFilename = new File(commands.get(0)).getName();
			pid = find.findSingleProcess("Exe.Name.ct=" + exeFilename);
			logger.fine("Detected pid: " + pid);
			return (int) pid;
		} catch (SigarException e) {
			// ignore, will throw if there is no process matching the pattern
		}		
		return null;
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	public static void addShutdownHook(Runnable runable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runable));
	}
	
	public Integer getPid() {
		return pid;
	}

	public static boolean isProcessRunning(Platform platform, int pid) {

		try {
			final Process pidof;
			if (platform == Platform.Linux || platform == Platform.OS_X) {
				pidof = Runtime.getRuntime().exec(
						new String[] { "kill", "-0", "" + pid });
				return pidof.waitFor() == 0;
			} else {
				// windows
				// process might be in either NOT RESPONDING due to
				// firewall blocking, or could be RUNNING
				final String[] cmd = { "tasklist.exe",
						"/FI", "PID eq " + pid ,"/FO", "CSV" };
				logger.finer("Command: " + Arrays.asList(cmd));
				ProcessBuilder processBuilder = ProcessControl
						.newProcessBuilder(Arrays.asList(cmd), true);
				Process process = processBuilder.start();
				// look for the PID in the output, pass it in for 'success' state
				LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(""+pid,
					new HashSet<String>(), StreamToLineProcessor.wrap(Processors.silent()));
				Processors.connect(new InputStreamReader(process.getInputStream()), logWatch);
				logWatch.waitForResult(2000);
				logger.finer("logWatch output: " + logWatch.getOutput());
				return logWatch.isInitWithSuccess();
			}

		} catch (IOException e) {
			logger.severe("IOException when trying to get process status:"
					+ e.getMessage());
			e.printStackTrace();

		} catch (InterruptedException e) {
			logger.severe("IOException when trying to get process status:"
					+ e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
}
