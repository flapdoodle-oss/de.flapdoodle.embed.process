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
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.lang.model.SourceVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.process.ProcessConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;

import static java.util.Arrays.asList;

public abstract class Processes {

	private static Logger logger = LoggerFactory.getLogger(ProcessControl.class);

	private static final PidHelper PID_HELPER;

	static {
		// Comparing with the string value to avoid a strong dependency on JDK 9
		String sourceVersion = SourceVersion.latest().toString();
		switch (sourceVersion) {
		case "RELEASE_9":
			PID_HELPER = PidHelper.JDK_9;
			break;
		case "RELEASE_10":
		case "RELEASE_11":
		case "RELEASE_12":
		case "RELEASE_13":
		case "RELEASE_14":
		case "RELEASE_15":
			PID_HELPER = PidHelper.JDK_11;
			break;
		default:
			PID_HELPER = PidHelper.LEGACY;
		}
	}

	private Processes() {
		// no instance
	}

	public static Long processId(Process process) {
		return PID_HELPER.getPid(process);
	}

	private static Long unixLikeProcessId(Process process) {
		Class<?> clazz = process.getClass();
		try {
			if (clazz.getName().equals("java.lang.UNIXProcess")) {
				Field pidField = clazz.getDeclaredField("pid");
				pidField.setAccessible(true);
				Object value = pidField.get(process);
				if (value instanceof Integer) {
					logger.debug("Detected pid: {}", value);
					return ((Integer) value).longValue();
				}
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException sx) {
			sx.printStackTrace();
		}
		return null;
	}

	/**
	 * @see http://www.golesny.de/p/code/javagetpid
	 *
	 * @return
	 */
	private static Long windowsProcessId(Process process) {
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
				logger.debug("Detected pid: {}", ret);
				return (long) ret;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean killProcess(SupportConfig support,Platform platform, StreamProcessor output, long pid) {
		return platform.isUnixLike() && ProcessControl.executeCommandLine(support, "[kill process]",
				new ProcessConfig(asList("kill", "-2", "" + pid), output));
	}

	public static boolean termProcess(SupportConfig support,Platform platform, StreamProcessor output, long pid) {
		return platform.isUnixLike() && ProcessControl.executeCommandLine(support, "[term process]",
				new ProcessConfig(asList("kill", "" + pid), output));
	}

	public static boolean tryKillProcess(SupportConfig support,Platform platform, StreamProcessor output, long pid) {
		return platform == Platform.Windows && ProcessControl.executeCommandLine(support, "[taskkill process]",
				new ProcessConfig(asList("taskkill", "/F", "/pid", "" + pid), output));
	}

	public static boolean isProcessRunning(Platform platform, long pid) {

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
				logger.trace("Command: {}", asList(cmd));
				ProcessBuilder processBuilder = ProcessControl
						.newProcessBuilder(asList(cmd), true);
				Process process = processBuilder.start();
				// look for the PID in the output, pass it in for 'success' state
				LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(""+pid,
						new HashSet<>(), StreamToLineProcessor.wrap(Processors.silent()));
				Processors.connect(new InputStreamReader(process.getInputStream()), logWatch);
				logWatch.waitForResult(2000);
				logger.trace("logWatch output: {}", logWatch.getOutput());
				return logWatch.isInitWithSuccess();
			}

		} catch (IOException | InterruptedException e) {
			logger.error("Trying to get process status", e);
			e.printStackTrace();

		}
		return false;
	}

	private enum PidHelper {

		JDK_9 {
			@Override
			Long getPid(Process process) {
				try {
					// Invoking via reflection to avoid a strong dependency on JDK 9
					Method getPid = Process.class.getMethod("getPid");
					return (Long) getPid.invoke(process);
				}
				catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		},
		JDK_11 {
			@Override
			Long getPid(Process process) {
				try {
					// Invoking via reflection to avoid a strong dependency on JDK 11
					Method getPid = Process.class.getMethod("pid");
					return (Long) getPid.invoke(process);
				}
				catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		},
		LEGACY {
			@Override
			Long getPid(Process process) {
				Long pid=unixLikeProcessId(process);
				if (pid==null) {
					pid=windowsProcessId(process);
				}
				return pid;
			}
		};

		abstract Long getPid(Process process);
	}
}
