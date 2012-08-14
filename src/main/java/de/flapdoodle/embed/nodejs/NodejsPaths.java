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
package de.flapdoodle.embed.nodejs;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.IVersion;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 */
public class NodejsPaths {

	private static Logger logger = Logger.getLogger(NodejsPaths.class.getName());

	public static Pattern getExecutablePattern(Distribution distribution) {
		return Pattern.compile(".*" + getExecutable(distribution));
	}

	//CHECKSTYLE:OFF
	public static String getExecutable(Distribution distribution) {
		String mongodPattern;
		switch (distribution.getPlatform()) {
			case Linux:
				mongodPattern = "node";
				break;
			case Windows:
				mongodPattern = "node.exe";
				break;
			case OS_X:
				mongodPattern = "node";
				break;
			default:
				throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
		}
		return mongodPattern;
	}

	public static ArchiveType getArchiveType(Distribution distribution) {
		ArchiveType archiveType;
		switch (distribution.getPlatform()) {
			case Linux:
				archiveType = ArchiveType.TGZ;
				break;
			case Windows:
				archiveType = ArchiveType.EXE;
				break;
			case OS_X:
				archiveType = ArchiveType.TGZ;
				break;
			default:
				throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
		}
		return archiveType;
	}

	public static String getPath(Distribution distribution) {
		String sversion = getVersionPart(distribution.getVersion());

		ArchiveType archiveType = getArchiveType(distribution);
		String sarchiveType;
		String subdir="";
		switch (archiveType) {
			case TGZ:
				sarchiveType = "tar.gz";
				break;
			case ZIP:
				sarchiveType = "zip";
				break;
			case EXE:
				sarchiveType = "exe";
				break;
			default:
				throw new IllegalArgumentException("Unknown ArchiveType " + archiveType);
		}

		String splatform;
		switch (distribution.getPlatform()) {
			case Linux:
				splatform = "-"+sversion+"-linux";
				break;
			case Windows:
				splatform = "";
				break;
			case OS_X:
				splatform = "-"+sversion+"-darwin";
				break;
			default:
				throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
		}

		String sbitSize;
		switch (distribution.getBitsize()) {
			case B32:
				switch (distribution.getPlatform()) {
					case Linux:
						sbitSize = "-x86";
						break;
					case Windows:
						sbitSize = "";
						break;
					case OS_X:
						sbitSize = "-x86";
						break;
					default:
						throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
				}
				break;
			case B64:
				switch (distribution.getPlatform()) {
					case Linux:
						sbitSize = "-x64";
						break;
					case Windows:
						sbitSize = "";
						subdir="x64/";
						break;
					case OS_X:
						sbitSize = "-x64";
						break;
					default:
						throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown BitSize " + distribution.getBitsize());
		}

		return sversion + "/"+subdir+"node" + splatform + sbitSize + "." + sarchiveType;
	}

	protected static String getVersionPart(IVersion version) {
		return version.asInDownloadPath();
	}

}
