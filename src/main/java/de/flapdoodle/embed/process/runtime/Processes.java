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

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.config.process.ProcessConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;

public abstract class Processes {

	private static Logger logger = Logger.getLogger(ProcessControl.class.getName());

	private Processes() {
		// no instance
	}
	
	public static Integer processId(Process process) {
		Integer pid=unixLikeProcessId(process);
		if (pid==null) {
			pid=windowsProcessId(process);
		}
		return pid;
	}

	static Integer unixLikeProcessId(Process process) {
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
		return null;
	}
	
	/**
	 * @see http://www.golesny.de/p/code/javagetpid
	 * 
	 * @return
	 */
	static Integer windowsProcessId(Process process) {
		if (process.getClass().getName().equals("java.lang.Win32Process")
				|| process.getClass().getName().equals("java.lang.ProcessImpl")) {
			/* determine the pid on windows plattforms */
			try {
				Field f = process.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				long handl = f.getLong(process);

				Kernel32 kernel = Kernel32.INSTANCE;
				WinNT.HANDLE handle = new WinNT.HANDLE();
				handle.setPointer(Pointer.createConstant(handl));
				int ret = kernel.GetProcessId(handle);
				logger.fine("Detected pid: " + ret);
				return ret;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean killProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
		if (platform.isUnixLike()) {
			return ProcessControl.executeCommandLine(support, "[kill process]",
					new ProcessConfig(Collections.newArrayList("kill", "-2", "" + pid), output));
		}
		return false;
	}

	public static boolean termProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
	    if (platform.isUnixLike()) {
		return ProcessControl.executeCommandLine(support, "[term process]",
			new ProcessConfig(Collections.newArrayList("kill", "" + pid), output));
	    }
	    return false;
	}

	public static boolean tryKillProcess(ISupportConfig support,Platform platform, IStreamProcessor output, int pid) {
		if (platform == Platform.Windows) {
			return ProcessControl.executeCommandLine(support, "[taskkill process]",
					new ProcessConfig(Collections.newArrayList("taskkill", "/F", "/pid", "" + pid), output));
		}
		return false;
	}

	public static boolean isProcessRunning(Platform platform, int pid) {
	
		try {
			final Process pidof;
			if (platform.isUnixLike()) {
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
			logger.log(Level.SEVERE,"Trying to get process status",e);
			e.printStackTrace();
	
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,"Trying to get process status",e);
			e.printStackTrace();
		}
		return false;
	}
}
