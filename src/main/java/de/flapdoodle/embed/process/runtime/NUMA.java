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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.io.Readers;

/**
 *
 */
public class NUMA {

	private static Logger logger = Logger.getLogger(NUMA.class.getName());

	public static synchronized boolean isNUMA(ISupportConfig support, Platform platform) {
		Boolean ret = NUMA_STATUS_MAP.get(platform);
		if (ret == null) {
			ret = isNUMAOnce(support, platform);
			NUMA_STATUS_MAP.put(platform, ret);
		}
		return ret;
	}

	static final Map<Platform, Boolean> NUMA_STATUS_MAP = new HashMap<Platform, Boolean>();


	public static boolean isNUMAOnce(ISupportConfig support, Platform platform) {
		if (platform == Platform.Linux) {
			try {
				ProcessControl process = ProcessControl
						.fromCommandLine(support, Collections.newArrayList("grep", "NUMA=y", "/boot/config-`uname -r`"), true);
				Reader reader = process.getReader();
				String content = Readers.readAll(reader);
				process.stop();
				boolean isNUMA = !content.isEmpty();
				if (isNUMA) {
					logger.warning("-----------------------------------------------\n"
							+ "NUMA support is still alpha. If you have any Problems with it, please contact us.\n"
							+ "-----------------------------------------------");
				}
				return isNUMA;
			} catch (IOException ix) {
				ix.printStackTrace();
			}
		}
		return false;
	}
}
