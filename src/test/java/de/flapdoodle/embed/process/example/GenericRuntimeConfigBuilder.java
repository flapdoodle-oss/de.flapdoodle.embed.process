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
package de.flapdoodle.embed.process.example;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.omg.CORBA._PolicyStub;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.AbstractEmbeddedBuilder;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DownloadConfigBuilder;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;


public class GenericRuntimeConfigBuilder extends AbstractBuilder<IRuntimeConfig> {

	public GenericRuntimeConfigBuilder name(String name) {
		set(Name.class,new Name("name"));
		return this;
	}
	
	public GenericRuntimeConfigBuilder downloadPath(String path) {
		set(DownloadPath.class,new DownloadPath(path));
		return this;
	}
	
	public GenericPackageResolverBuilder packageResolver() {
		return new GenericPackageResolverBuilder(this);
	}

	private GenericRuntimeConfigBuilder packageResolver(MapGenericPackageResolver mapGenericPackageResolver) {
		set(IPackageResolver.class,mapGenericPackageResolver);
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
		String downloadPath = get(DownloadPath.class).value();
		String name = get(Name.class).value();
		
		IPackageResolver packageResolver=get(IPackageResolver.class);
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
					.connectionTimeout(12000)
					.userAgent("Mozilla/5.0 (compatible; embedded "+name+"; +https://github.com/flapdoodle-oss/de.flapdoodle.embed.process)"))
				.tempDir(new PropertyOrPlatformTempDir())
				.executableNaming(new UUIDTempNaming()))
			.processOutput(ProcessOutput.getDefaultInstance(name))
			.commandLinePostProcessor(new ICommandLinePostProcessor.Noop()).build();
	}
	
	public static class GenericPackageResolverBuilder extends AbstractEmbeddedBuilder<IPackageResolver> {
		
		private final GenericRuntimeConfigBuilder _parent;

		public GenericPackageResolverBuilder(GenericRuntimeConfigBuilder parent) {
			_parent = parent;
		}
		
		protected <T> T getAndSet(Class<T> type, T defaultValue) {
			T ret=get(type,null);
			if (ret==null) {
				set(type,defaultValue);
				return defaultValue;
			}
			return ret;
		}


		public GenericPackageResolverBuilder executeable(Distribution distribution,String filename) {
			Map<Distribution, String> map = getAndSet(ExecMap.class, new ExecMap(new HashMap<Distribution, String>())).value();
			if (map.put(distribution, filename)!=null) {
				throw new RuntimeException("executable for "+distribution+" allready set");
			}
			return this;
		}
		
		public GenericPackageResolverBuilder archiveType(Distribution distribution,ArchiveType arcType) {
			Map<Distribution, ArchiveType> map = getAndSet(ArchiveTypeMap.class, new ArchiveTypeMap(new HashMap<Distribution, ArchiveType>())).value();
			if (map.put(distribution, arcType)!=null) {
				throw new RuntimeException("archiveType for "+distribution+" allready set");
			}
			return this;
		}
		
		public GenericPackageResolverBuilder archivePath(Distribution distribution,String archivePath) {
			Map<Distribution, String> map = getAndSet(ArchivePathMap.class, new ArchivePathMap(new HashMap<Distribution, String>())).value();
			if (map.put(distribution, archivePath)!=null) {
				throw new RuntimeException("archivePath for "+distribution+" allready set");
			}
			return this;
		}
		
		public GenericRuntimeConfigBuilder build() {
			Map<Distribution, String> execMap = get(ExecMap.class).value();
			Map<Distribution, ArchiveType> arcTypeMap = get(ArchiveTypeMap.class).value();
			Map<Distribution, String> archivePathMap = get(ArchivePathMap.class).value();
			return _parent.packageResolver(new MapGenericPackageResolver(execMap,arcTypeMap,archivePathMap));
		}
	}
	
	static class ExecMap extends ImmutableContainer<Map<Distribution, String>> {

		public ExecMap(Map<Distribution, String> value) {
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

		private final Map<Distribution, String> _execNames;
		private final Map<Distribution, ArchiveType> _arcTypeMap;
		private final Map<Distribution, String> _archivePathMap;
		
		public MapGenericPackageResolver(Map<Distribution, String> execNames, Map<Distribution, ArchiveType> arcTypeMap, Map<Distribution, String> archivePathMap) {
			_execNames = execNames;
			_arcTypeMap = arcTypeMap;
			_archivePathMap = archivePathMap;
		}
		
		@Override
		public Pattern executeablePattern(Distribution distribution) {
			return Pattern.compile(".*"+executableFilename(distribution));
		}

		@Override
		public String executableFilename(Distribution distribution) {
			return _execNames.get(distribution);
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
