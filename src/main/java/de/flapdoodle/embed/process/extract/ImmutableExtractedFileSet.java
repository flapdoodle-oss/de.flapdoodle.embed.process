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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.flapdoodle.embed.process.config.store.FileType;

public class ImmutableExtractedFileSet implements IExtractedFileSet {

	private final File _executable;
	private final Map<FileType,List<File>> _files;
	private final File _generatedBaseDir;

	ImmutableExtractedFileSet(File generatedBaseDir, File executable, Map<FileType,List<File>> files) {
		if (executable==null) throw new NullPointerException("executable is NULL");
		if (files==null) throw new NullPointerException("files is NULL");
		
		_generatedBaseDir=generatedBaseDir;
		_executable = executable;
		Map<FileType,List<File>> copy=new HashMap<FileType, List<File>>();
		for (FileType key : files.keySet()) {
			copy.put(key,Collections.unmodifiableList(new ArrayList<File>(files.get(key))));
		}
		_files=Collections.unmodifiableMap(copy);
	}

	@Override
	public File generatedBaseDir() {
		return _generatedBaseDir;
	}
	
	@Override
	public File executable() {
		return _executable;
	}
	
	@Override
	public List<File> files(FileType type) {
		List<File> ret = _files.get(type);
		if (ret==null) ret=Collections.emptyList();
		return ret;
	}
	
	public static Builder builder(File generatedBaseDir) {
		return new Builder().setGeneratedBaseDir(generatedBaseDir);
	}

	public static class Builder {
		File _executable=null;
		File _generatedBaseDir=null;
		Map<FileType,List<File>> _files=new HashMap<FileType, List<File>>();
		
		public Builder executable(File executable) {
			if (_executable!=null) throw new IllegalArgumentException("executable allready set to "+_executable);
			_executable=executable;
			return this;
		}
		
		public Builder setGeneratedBaseDir(File generatedBaseDir) {
			_generatedBaseDir=generatedBaseDir;
			return this;
		}

		public Builder file(FileType type, File file) {
			if (type==FileType.Executable) {
				return executable(file);
			}
			
			List<File> collection = _files.get(type);
			if (collection==null) {
				collection=new ArrayList<File>();
				_files.put(type, collection);
			}
			collection.add(file);
			return this;
		}
		
		public IExtractedFileSet build() {
			return new ImmutableExtractedFileSet(_generatedBaseDir, _executable,_files);
		}
	}
}
