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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.ExecutableProcessConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;
import de.flapdoodle.embed.process.store.IArtifactStore;

public abstract class Starter<CONFIG extends ExecutableProcessConfig,EXECUTABLE extends Executable<CONFIG, PROCESS>,PROCESS extends IStopable> {
	
	private static Logger logger = LoggerFactory.getLogger(Starter.class);
	
	private final RuntimeConfig runtime;
	
	protected Starter(RuntimeConfig config) {
		runtime = config;
	}

	public EXECUTABLE prepare(CONFIG config) {
		return prepare(config, Distribution.detectFor(config.version()));
	}

	public EXECUTABLE prepare(CONFIG config, Distribution distribution) {
		try {
			IArtifactStore artifactStore = runtime.artifactStore();
			
			Optional<ExtractedFileSet> files = artifactStore.extractFileSet(distribution);
			if (files.isPresent()) {
				return newExecutable(config, distribution, runtime, files.get());
			} else {
				throw new DistributionException("could not find Distribution",distribution);
			}
		} catch (IOException iox) {
			String messageOnException = config.supportConfig().messageOnException().apply(getClass(), iox);
			if (messageOnException==null) {
				messageOnException="prepare executable";
			}
			logger.error(messageOnException, iox);
			throw new DistributionException(distribution,iox);
		}
	}

	protected abstract EXECUTABLE newExecutable(CONFIG config, Distribution distribution, RuntimeConfig runtime, ExtractedFileSet exe);
}
