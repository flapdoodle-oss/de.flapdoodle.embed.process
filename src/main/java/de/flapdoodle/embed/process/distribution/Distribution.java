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
package de.flapdoodle.embed.process.distribution;

/**
 *
 */
public class Distribution {

	private final IVersion version;
	private final Platform platform;
	private final BitSize bitsize;
	private final Architecture architecture;

	public Distribution(IVersion version, Platform platform, BitSize bitsize, Architecture architecture) {
		this.version = version;
		this.platform = platform;
		this.bitsize = bitsize;
		this.architecture = architecture;
	}

	public Distribution(IVersion version, Platform platform, BitSize bitsize) {
		this(version, platform, bitsize, Architecture.AMD64);
	}

	public IVersion getVersion() {
		return version;
	}

	public Platform getPlatform() {
		return platform;
	}

	public BitSize getBitsize() {
		return bitsize;
	}

	public Architecture getArchitecture() {
		return architecture;
	}

	@Override
	public String toString() {
		return "" + version + ":" + platform + ":" + bitsize + ":" + architecture;
	}

	public static Distribution detectFor(IVersion version) {
		return new Distribution(version, Platform.detect(), BitSize.detect(), Architecture.detect());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitsize == null)
				? 0
				: bitsize.hashCode());
		result = prime * result + ((platform == null)
				? 0
				: platform.hashCode());
		result = prime * result + ((version == null)
				? 0
				: version.hashCode());
		result = prime * result + ((architecture == null)
				? 0
				: architecture.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Distribution other = (Distribution) obj;
		if (bitsize != other.bitsize)
			return false;
		if (platform != other.platform)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (architecture != other.architecture)
			return false;
		return true;
	}
	
	
}
