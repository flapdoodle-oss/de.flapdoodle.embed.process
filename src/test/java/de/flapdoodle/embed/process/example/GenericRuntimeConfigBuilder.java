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
package de.flapdoodle.embed.process.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.omg.CORBA._PolicyStub;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.AbstractEmbeddedBuilder;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DownloadConfigBuilder;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import de.flapdoodle.embed.process.store.Downloader;


public class GenericRuntimeConfigBuilder extends AbstractBuilder<IRuntimeConfig> {

	private static final TypedProperty<IPackageResolver> PACKAGE_RESOLVER = TypedProperty.with("PackageResolver",IPackageResolver.class);
	private static final TypedProperty<DownloadPath> DOWNLOAD_PATH = TypedProperty.with(DownloadPath.class);
	private static final TypedProperty<Name> NAME = TypedProperty.with(Name.class);

	public GenericRuntimeConfigBuilder name(String name) {
		set(NAME,new Name("name"));
		return this;
	}
	
	public GenericRuntimeConfigBuilder downloadPath(String path) {
		set(DOWNLOAD_PATH,new DownloadPath(path));
		return this;
	}
	
	public GenericPackageResolverBuilder packageResolver() {
		return new GenericPackageResolverBuilder(this);
	}

	private GenericRuntimeConfigBuilder packageResolver(MapGenericPackageResolver mapGenericPackageResolver) {
		set(PACKAGE_RESOLVER,mapGenericPackageResolver);
		return this;
	}
	
	static class Name extends ImmutableContainer<String> {

		public Name(String value) {
			super(value);
		}
	}

	static class DownloadPath extends ImmutableContainer<String> {

		public DownloadPath(String value) {
			super(value);
		}
	}
	
	@Override
	public IRuntimeConfig build() {
		String downloadPath = get(DOWNLOAD_PATH).value();
		String name = get(NAME).value();
		
		IPackageResolver packageResolver=get(PACKAGE_RESOLVER);
		String prefix = "."+name;
		
		return new RuntimeConfigBuilder()
			.artifactStore(new ArtifactStoreBuilder()
				.download(new DownloadConfigBuilder()
					.downloadPath(downloadPath)
					.downloadPrefix(prefix)
					.packageResolver(packageResolver)
					.artifactStorePath(new UserHome(prefix))
					.fileNaming(new UUIDTempNaming())
					.progressListener(new StandardConsoleProgressListener())
					.userAgent("Mozilla/5.0 (compatible; embedded "+name+"; +https://github.com/flapdoodle-oss/de.flapdoodle.embed.process)"))
				.downloader(new Downloader())
				.tempDir(new PropertyOrPlatformTempDir())
				.executableNaming(new UUIDTempNaming()))
			.processOutput(ProcessOutput.getDefaultInstance(name))
			.commandLinePostProcessor(new ICommandLinePostProcessor.Noop()).build();
	}
	
	public static class GenericPackageResolverBuilder extends AbstractEmbeddedBuilder<IPackageResolver> {
		
		private static final TypedProperty<ArchivePathMap> ARCHIVEPATH_MAP = TypedProperty.with("ArchivePath",ArchivePathMap.class);
		private static final TypedProperty<ArchiveTypeMap> ARCHIVETYPE_MAP = TypedProperty.with("ArchiveType",ArchiveTypeMap.class);
		private static final TypedProperty<FileSetMap> FILESET_MAP = TypedProperty.with("FileSetMap",FileSetMap.class);
		private final GenericRuntimeConfigBuilder _parent;

		public GenericPackageResolverBuilder(GenericRuntimeConfigBuilder parent) {
			_parent = parent;
		}
		
		protected <T> T getAndSet(TypedProperty<T> type, T defaultValue) {
			T ret=get(type,null);
			if (ret==null) {
				set(type,defaultValue);
				return defaultValue;
			}
			return ret;
		}


		public GenericPackageResolverBuilder files(Distribution distribution,FileSet files) {
			Map<Distribution, FileSet> map = getAndSet(FILESET_MAP, new FileSetMap(new HashMap<Distribution, FileSet>())).value();
			if (map.put(distribution, files)!=null) {
				throw new RuntimeException("executable for "+distribution+" already set");
			}
			return this;
		}
		
		public GenericPackageResolverBuilder archiveType(Distribution distribution,ArchiveType arcType) {
			Map<Distribution, ArchiveType> map = getAndSet(ARCHIVETYPE_MAP, new ArchiveTypeMap(new HashMap<Distribution, ArchiveType>())).value();
			if (map.put(distribution, arcType)!=null) {
				throw new RuntimeException("archiveType for "+distribution+" already set");
			}
			return this;
		}
		
		public GenericPackageResolverBuilder archivePath(Distribution distribution,String archivePath) {
			Map<Distribution, String> map = getAndSet(ARCHIVEPATH_MAP, new ArchivePathMap(new HashMap<Distribution, String>())).value();
			if (map.put(distribution, archivePath)!=null) {
				throw new RuntimeException("archivePath for "+distribution+" already set");
			}
			return this;
		}
		
		public GenericRuntimeConfigBuilder build() {
			Map<Distribution, FileSet> execMap = get(FILESET_MAP).value();
			Map<Distribution, ArchiveType> arcTypeMap = get(ARCHIVETYPE_MAP).value();
			Map<Distribution, String> archivePathMap = get(ARCHIVEPATH_MAP).value();
			return _parent.packageResolver(new MapGenericPackageResolver(execMap,arcTypeMap,archivePathMap));
		}
	}
	
	static class FileSetMap extends ImmutableContainer<Map<Distribution, FileSet>> {

		public FileSetMap(Map<Distribution, FileSet> value) {
			super(value);
		}
	}

	static class ArchiveTypeMap extends ImmutableContainer<Map<Distribution, ArchiveType>> {

		public ArchiveTypeMap(Map<Distribution, ArchiveType> value) {
			super(value);
		}
	}

	static class ArchivePathMap extends ImmutableContainer<Map<Distribution, String>> {

		public ArchivePathMap(Map<Distribution, String> value) {
			super(value);
		}
	}
	
	static class MapGenericPackageResolver implements IPackageResolver {

		private final Map<Distribution, FileSet> _fileSets;
		
		private final Map<Distribution, ArchiveType> _arcTypeMap;
		private final Map<Distribution, String> _archivePathMap;
		
		public MapGenericPackageResolver(Map<Distribution, FileSet> fileSets, Map<Distribution, ArchiveType> arcTypeMap, Map<Distribution, String> archivePathMap) {
			_fileSets=fileSets;
			_arcTypeMap = arcTypeMap;
			_archivePathMap = archivePathMap;
		}
		
//		@Deprecated
//		public Pattern executeablePattern(Distribution distribution) {
//			return Pattern.compile(".*"+executableFilename(distribution));
//		}
		
		@Override
		public FileSet getFileSet(Distribution distribution) {
			return _fileSets.get(distribution);
		}
		
		@Override
		public ArchiveType getArchiveType(Distribution distribution) {
			return _arcTypeMap.get(distribution);
		}

		@Override
		public String getPath(Distribution distribution) {
			return _archivePathMap.get(distribution);
		}

	}

}
