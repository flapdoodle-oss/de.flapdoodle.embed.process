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
package de.flapdoodle.embed.mongo;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.Starter;

/**
 *
 */
public class MongodStarter extends Starter<MongodConfig,MongodExecutable,MongodProcess> {

	private static Logger logger = Logger.getLogger(MongodStarter.class.getName());

//	private final IRuntimeConfig runtime;

	private MongodStarter(IRuntimeConfig config) {
		super(config);
	}

	public static MongodStarter getInstance(IRuntimeConfig config) {
		return new MongodStarter(config);
	}

	public static MongodStarter getDefaultInstance() {
		return getInstance(new RuntimeConfig());
	}

	@Override
	protected boolean checkDistribution(Distribution distribution) throws IOException {
		return super.checkDistribution(distribution);
	}
	
	@Override
	protected File extractExe(Distribution distribution) throws IOException {
		return super.extractExe(distribution);
	}
	
//	public MongodExecutable prepare(MongodConfig mongodConfig) {
//		Distribution distribution = Distribution.detectFor(mongodConfig.getVersion());
//		
//		try {
//			IProgressListener progress = runtime.getProgressListener();
//
//			progress.done("Detect Distribution");
//			if (checkDistribution(distribution)) {
//				progress.done("Check Distribution");
//				File mongodExe = extractMongod(distribution);
//
//				return new MongodExecutable(distribution, mongodConfig, runtime, mongodExe);
//			} else {
//				throw new MongodException("could not find Distribution",distribution);
//			}
//		} catch (IOException iox) {
//			logger.log(Level.SEVERE, "start", iox);
//			throw new MongodException(distribution,iox);
//		}
//	}

	protected MongodExecutable newExecutable(MongodConfig mongodConfig, Distribution distribution, IRuntimeConfig runtime, File mongodExe) {
		return new MongodExecutable(distribution, mongodConfig, runtime, mongodExe);
	}

	protected Pattern executeablePattern(Distribution distribution) {
		return Paths.getMongodExecutablePattern(distribution);
	}

	protected String executableFilename(Distribution distribution) {
		return Paths.getMongodExecutable(distribution);
	}
}
