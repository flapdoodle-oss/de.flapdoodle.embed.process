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
package de.flapdoodle.embed.process.config.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FileSet {

	private final List<Entry> _entries;

	public FileSet(Collection<Entry> entries) {
		if (entries==null) throw new NullPointerException("entries is NULL");
		_entries = Collections.unmodifiableList(new ArrayList<Entry>(entries));
		boolean oneOrMoreExecutableFound=false;
		for (Entry e : _entries) {
			if (e.type()==FileType.Executable) {
				oneOrMoreExecutableFound=true;
				break;
			}
		}
		if (!oneOrMoreExecutableFound) {
			throw new IllegalArgumentException("there is no executable in this file set");
		}
	}
	
	
	public List<Entry> entries() {
		return _entries;
	}

	public static class Entry {

		private final FileType _type;
		private final String _destination;
		private final Pattern _matchingPattern;

		public Entry(FileType type, String destination, Pattern matchingPattern) {
			_type = type;
			_destination = destination;
			_matchingPattern = matchingPattern;
		}

		public FileType type() {
			return _type;
		}

		public String destination() {
			return _destination;
		}

		public Pattern matchingPattern() {
			return _matchingPattern;
		}

	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private final List<Entry> _entries=new ArrayList<FileSet.Entry>();
		
		public Builder addEntry(FileType type, String filename) {
			return addEntry(type,filename,".*"+filename);
		}
		
		public Builder addEntry(FileType type, String filename, String pattern) {
			return addEntry(type,filename,Pattern.compile(pattern,Pattern.CASE_INSENSITIVE));
		}
		
		public Builder addEntry(FileType type, String filename, Pattern pattern) {
			_entries.add(new Entry(type,filename,pattern));
			return this;
		}
		
		public FileSet build() {
			return new FileSet(_entries);
		}
	}
}
