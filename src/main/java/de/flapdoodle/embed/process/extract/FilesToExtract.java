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
package de.flapdoodle.embed.process.extract;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileSet.Entry;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.file.FileAlreadyExistsException;
import de.flapdoodle.embed.process.io.file.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class FilesToExtract {

	private final ArrayList<FileSet.Entry> _files;
	private final TempNaming _executableNaming;
	private final File _dirFactoryResult;
	private final boolean _dirFactoryResultIsGenerated;

	public FilesToExtract(Directory dirFactory, TempNaming executableNaming, FileSet fileSet) {
		if (dirFactory==null) throw new NullPointerException("dirFactory is NULL");
		if (executableNaming==null) throw new NullPointerException("executableNaming is NULL");
		if (fileSet==null) throw new NullPointerException("fileSet is NULL");
		
		_files = new ArrayList<>(fileSet.entries());
		_dirFactoryResult = dirFactory.asFile();
		_dirFactoryResultIsGenerated=dirFactory.isGenerated();
		_executableNaming = executableNaming;
	}

	public File baseDir() {
		return _dirFactoryResult;
	}
	
	public boolean baseDirIsGenerated() {
		return _dirFactoryResultIsGenerated;
	}
	
	public boolean nothingLeft() {
		return _files.isEmpty();
	}
	
	public List<FileSet.Entry> files() {
		return Collections.unmodifiableList(_files);
	}

	public ExtractionMatch find(Archive.Entry entry) {
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
		return found!=null ? new Match(_dirFactoryResult,_executableNaming, found) : null;
	}
	
	public static String fileName(Entry entry) {
		return entry.destination();
	}

	public static String executableName(TempNaming executableNaming, Entry entry) {
		return executableNaming.nameFor("extract",fileName(entry));
	}
	
	static class Match implements ExtractionMatch {

		private final Entry _entry;
		private final File _dirFactoryResult;
		private final TempNaming _executableNaming;

		public Match(File dirFactoryResult,TempNaming executableNaming, Entry entry) {
			_dirFactoryResult = dirFactoryResult;
			_executableNaming = executableNaming;
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
					try {
						destination=Files.createTempFile(_dirFactoryResult,executableName(_executableNaming, _entry));
					} catch (FileAlreadyExistsException ex) {
						throw new ExecutableFileAlreadyExistsException(ex);
					}
					break;
				default:
					destination=Files.createTempFile(_dirFactoryResult,fileName(_entry));
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
