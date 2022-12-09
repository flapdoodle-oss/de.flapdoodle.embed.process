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
package de.flapdoodle.embed.process.io.directories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 *
 */
public class UserHome implements Directory {
	private final String postFix;

	private static Logger logger= LoggerFactory.getLogger(UserHome.class);

	public UserHome(String postFix) {
		this.postFix=postFix;
	}
	
	@Override
	public File asFile() {
		return userHome(System::getProperty).resolve(postFix).toFile();
	}
	
	@Override
	public boolean isGenerated() {
		return false;
	}

	public static Path userHome(Function<String, String> systemGetProperty) {
		String userHome = systemGetProperty.apply("user.home");
		if (userHome==null) throw new IllegalArgumentException("user.home is null");
		if ("?".equals(userHome)) {
			logger.warn("user.home is set to '?', maybe this is running inside a docker container");
			logger.warn("use fallback to user.dir");
			String userDir = systemGetProperty.apply("user.dir");
			if (userDir==null) throw new IllegalArgumentException("user.dir is null");
			if (userDir.equals("?")) throw new IllegalArgumentException("user.dir is set to '?'");
			logger.warn("use user.dir('{}') as fallback for user.home('{}')",userDir, userHome);
			userHome = userDir;
		}
		Path path = Paths.get(userHome);
		if (!Files.isDirectory(path)) throw new IllegalArgumentException(""+path+" is not a directory");
		return path;
	}
}
