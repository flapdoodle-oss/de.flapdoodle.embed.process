package de.flapdoodle.embed.process.types;

import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ProcessControl;

import java.nio.file.Path;

@FunctionalInterface
public interface RunningProcessFactory<T extends RunningProcess> {
	T startedWith(ProcessControl process, ProcessOutput processOutput, Path pidFile, long timeout);
}
