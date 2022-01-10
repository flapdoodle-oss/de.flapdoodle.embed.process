package de.flapdoodle.embed.process.types;

import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.ReaderProcessor;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RunningProcess {
	int stop();

	static Runnable connectIOTo(ProcessControl process, ProcessOutput processOutput) {
		ReaderProcessor outputReader = Processors.connect(process.getReader(), processOutput.output());
		ReaderProcessor errorReader = Processors.connect(process.getError(), StreamToLineProcessor.wrap(processOutput.error()));

		return () -> ReaderProcessor.abortAll(outputReader, errorReader);
	}

	static <T extends RunningProcess> T start(
		RunningProcessFactory<T> runningProcessFactory,
		Path executable,
		List<String> arguments,
		Map<String, String> environment,
		ProcessConfig processConfig,
		ProcessOutput outputConfig,
		SupportConfig supportConfig
	)
		throws IOException {
		Path pidFile = pidFile(executable);

		List<String> commandLine = Stream
			.concat(Stream.of(executable.toFile().getAbsolutePath()), arguments.stream())
			.collect(Collectors.toList());

		ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(commandLine, environment, true);
		ProcessControl process = ProcessControl.start(supportConfig, processBuilder);

		try {
			if (process.getPid() != null) {
				writePidFile(pidFile, process.getPid());
			}

			T running = runningProcessFactory.startedWith(process, outputConfig, pidFile, processConfig.stopTimeoutInMillis());

			if (processConfig.daemonProcess()) {
				ProcessControl.addShutdownHook(running::stop);
			}
			return running;
		}
		catch (IOException iox) {
			Files.delete(pidFile);
			process.stop(processConfig.stopTimeoutInMillis());
			throw iox;
		}
	}

	static String executableBaseName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			return name.substring(0, idx);
		}
		return name;
	}

	static Path pidFile(Path executableFile) {
		return executableFile.getParent().resolve(executableBaseName(executableFile.getFileName().toString()) + ".pid");
	}

	static void writePidFile(Path pidFile, long pid) throws IOException {
		Files.write(pidFile, Collections.singletonList("" + pid));
	}
}
