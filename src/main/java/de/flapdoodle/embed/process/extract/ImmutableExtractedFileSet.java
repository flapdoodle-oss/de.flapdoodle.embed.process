package de.flapdoodle.embed.process.extract;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.flapdoodle.embed.process.config.store.FileType;

public class ImmutableExtractedFileSet implements IExtractedFileSet {

	private final File _executable;
	private final Map<FileType,List<File>> _files;

	ImmutableExtractedFileSet(File executable, Map<FileType,List<File>> files) {
		if (executable==null) throw new NullPointerException("executable is NULL");
		if (files==null) throw new NullPointerException("files is NULL");
		
		_executable = executable;
		Map<FileType,List<File>> copy=new HashMap<FileType, List<File>>();
		for (FileType key : files.keySet()) {
			copy.put(key,Collections.unmodifiableList(new ArrayList<File>(files.get(key))));
		}
		_files=Collections.unmodifiableMap(copy);
	}

	@Override
	public File executable() {
		return _executable;
	}
	
	@Override
	public List<File> files(FileType type) {
		return _files.get(type);
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		File _executable=null;
		Map<FileType,List<File>> _files=new HashMap<FileType, List<File>>();
		
		public Builder executable(File executable) {
			if (_executable!=null) throw new IllegalArgumentException("executable allready set to "+_executable);
			_executable=executable;
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
			return new ImmutableExtractedFileSet(_executable,_files);
		}
	}
}
