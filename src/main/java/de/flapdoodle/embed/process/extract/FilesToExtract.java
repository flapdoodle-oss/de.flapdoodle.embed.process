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
package de.flapdoodle.embed.process.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.FileSet.Entry;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.file.Files;

public class FilesToExtract {

	private final ArrayList<FileSet.Entry> _files;
	private final IDirectory _dirFactory;
	private final ITempNaming _exeutableNaming;

	public FilesToExtract(IDirectory dirFactory, ITempNaming exeutableNaming, FileSet fileSet) {
		if (dirFactory==null) throw new NullPointerException("dirFactory is NULL");
		if (exeutableNaming==null) throw new NullPointerException("exeutableNaming is NULL");
		if (fileSet==null) throw new NullPointerException("fileSet is NULL");
		
		_files = new ArrayList<FileSet.Entry>(fileSet.entries());
		_dirFactory = dirFactory;
		_exeutableNaming = exeutableNaming;
	}

	public boolean nothingLeft() {
		return _files.isEmpty();
	}

	public IExtractionMatch find(IArchiveEntry entry) {
		Entry found = null;

		if (!entry.isDirectory()) {
			for (FileSet.Entry e : _files) {
				if (e.matchingPattern().matcher(entry.getName()).matches()) {
					found = e;
					break;
				}
			}

			if (found != null) {
				_files.remove(found);
			}
		}
		return found!=null ? new Match(_dirFactory,_exeutableNaming, found) : null;
	}
	
	static class Match implements IExtractionMatch {

		private final Entry _entry;
		private final IDirectory _dirFactory;
		private final ITempNaming _exeutableNaming;

		public Match(IDirectory dirFactory,ITempNaming exeutableNaming, Entry entry) {
			_dirFactory = dirFactory;
			_exeutableNaming = exeutableNaming;
			_entry = entry;
		}
		
		@Override
		public FileType type() {
			return _entry.type();
		}

		@Override
		public File write(InputStream source, long size) throws IOException {
			File destination;
			switch (_entry.type()) {
				case Executable: 
					destination=Files.createTempFile(_dirFactory,_exeutableNaming.nameFor("extract",_entry.destination()));
					break;
				default:
					destination=Files.createTempFile(_dirFactory,_entry.destination());
					break;
			}
			
			Files.write(source, size, destination);
			switch (_entry.type()) {
				case Executable:
					destination.setExecutable(true);
					break;
			}
			return destination;
		}
		
	}

}
