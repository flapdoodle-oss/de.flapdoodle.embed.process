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

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.progress.IProgressListener;

public class DownloadConfigBuilder extends AbstractBuilder<IDownloadConfig> {

	private static final TypedProperty<UserAgent> USER_AGENT = TypedProperty.with("UserAgent", UserAgent.class);
	private static final TypedProperty<IProgressListener> PROGRESS_LISTENER = TypedProperty.with("ProgressListener", IProgressListener.class);
	private static final TypedProperty<ITempNaming> FILE_NAMING = TypedProperty.with("FileNaming", ITempNaming.class);
	private static final TypedProperty<IDirectory> ARTIFACT_STORE_PATH = TypedProperty.with("ArtifactStorePath",	IDirectory.class);
	private static final TypedProperty<IPackageResolver> PACKAGE_RESOLVER = TypedProperty.with("PackageResolver",	IPackageResolver.class);
	private static final TypedProperty<DownloadPrefix> DOWNLOAD_PREFIX = TypedProperty.with("DownloadPrefix",	DownloadPrefix.class);
	private static final TypedProperty<IDownloadPath> DOWNLOAD_PATH = TypedProperty.with("DownloadPath",	IDownloadPath.class);

	private static final TypedProperty<ITimeoutConfig> TIMEOUT_CONFIG = TypedProperty.with("TimeoutConfig",	ITimeoutConfig.class);
	private static final TypedProperty<IProxyFactory> PROXY_FACTORY = TypedProperty.with("ProxyFactory",	IProxyFactory.class);

	public DownloadConfigBuilder() {
		setDefault(TIMEOUT_CONFIG, new TimeoutConfigBuilder().defaults().build());
		setDefault(PROXY_FACTORY, new NoProxyFactory());
	}

	public DownloadConfigBuilder downloadPath(String path) {
		set(DOWNLOAD_PATH, new DownloadPath(path));
		return this;
	}
	
	protected IProperty<IDownloadPath> downloadPath() {
		return property(DOWNLOAD_PATH);
	}

	public DownloadConfigBuilder downloadPrefix(String prefix) {
		set(DOWNLOAD_PREFIX, new DownloadPrefix(prefix));
		return this;
	}

	protected IProperty<DownloadPrefix> downloadPrefix() {
		return property(DOWNLOAD_PREFIX);
	}

	public DownloadConfigBuilder packageResolver(IPackageResolver packageResolver) {
		set(PACKAGE_RESOLVER, packageResolver);
		return this;
	}
	
	protected IProperty<IPackageResolver> packageResolver() {
		return property(PACKAGE_RESOLVER);
	}

	public DownloadConfigBuilder artifactStorePath(IDirectory artifactStorePath) {
		set(ARTIFACT_STORE_PATH, artifactStorePath);
		return this;
	}

	protected IProperty<IDirectory> artifactStorePath() {
		return property(ARTIFACT_STORE_PATH);
	}

	public DownloadConfigBuilder fileNaming(ITempNaming fileNaming) {
		set(FILE_NAMING, fileNaming);
		return this;
	}

	protected IProperty<ITempNaming> fileNaming() {
		return property(FILE_NAMING);
	}

	public DownloadConfigBuilder progressListener(IProgressListener progressListener) {
		set(PROGRESS_LISTENER, progressListener);
		return this;
	}

	protected IProperty<IProgressListener> progressListener() {
		return property(PROGRESS_LISTENER);
	}

	public DownloadConfigBuilder userAgent(String userAgent) {
		set(USER_AGENT, new UserAgent(userAgent));
		return this;
	}

	protected IProperty<UserAgent> userAgent() {
		return property(USER_AGENT);
	}

	public DownloadConfigBuilder timeoutConfig(ITimeoutConfig timeoutConfig) {
		set(TIMEOUT_CONFIG, timeoutConfig);
		return this;
	}

	protected IProperty<ITimeoutConfig> timeoutConfig() {
		return property(TIMEOUT_CONFIG);
	}

	public DownloadConfigBuilder proxyFactory(IProxyFactory proxyFactory) {
		set(PROXY_FACTORY, proxyFactory);
		return this;
	}

	protected IProperty<IProxyFactory> proxyFactory() {
		return property(PROXY_FACTORY);
	}
	
	@Override
	public IDownloadConfig build() {
		final IDownloadPath downloadPath = get(DOWNLOAD_PATH);
		final String downloadPrefix = get(DOWNLOAD_PREFIX).value();
		final IPackageResolver packageResolver = get(PACKAGE_RESOLVER);
		final IDirectory artifactStorePath = get(ARTIFACT_STORE_PATH);
		final ITempNaming fileNaming = get(FILE_NAMING);
		final IProgressListener progressListener = get(PROGRESS_LISTENER);
		final String userAgent = get(USER_AGENT).value();
		final ITimeoutConfig timeoutConfig = get(TIMEOUT_CONFIG);
		final IProxyFactory proxyFactory = get(PROXY_FACTORY);

		return new ImmutableDownloadConfig(downloadPath, downloadPrefix, packageResolver, artifactStorePath, fileNaming,
				progressListener, userAgent, timeoutConfig, proxyFactory);
	}

	protected static class DownloadPrefix extends ImmutableContainer<String> {

		public DownloadPrefix(String value) {
			super(value);
		}

	}

	protected static class UserAgent extends ImmutableContainer<String> {

		public UserAgent(String value) {
			super(value);
		}

	}

	protected static class ImmutableDownloadConfig implements IDownloadConfig {

		private final IDownloadPath _downloadPath;
		private final IProgressListener _progressListener;
		private final IDirectory _artifactStorePath;
		private final ITempNaming _fileNaming;
		private final String _downloadPrefix;
		private final String _userAgent;
		private final IPackageResolver _packageResolver;
		private final ITimeoutConfig _timeoutConfig;
		private final IProxyFactory _proxyFactory;

		public ImmutableDownloadConfig(IDownloadPath downloadPath, String downloadPrefix, IPackageResolver packageResolver,
				IDirectory artifactStorePath, ITempNaming fileNaming, IProgressListener progressListener, String userAgent,
				ITimeoutConfig timeoutConfig, IProxyFactory proxyFactory) {
			super();
			_downloadPath = downloadPath;
			_downloadPrefix = downloadPrefix;
			_packageResolver = packageResolver;
			_artifactStorePath = artifactStorePath;
			_fileNaming = fileNaming;
			_progressListener = progressListener;
			_userAgent = userAgent;
			_timeoutConfig = timeoutConfig;
			_proxyFactory = proxyFactory;
		}

		@Override
		public IDownloadPath getDownloadPath() {
			return _downloadPath;
		}

		@Override
		public IProgressListener getProgressListener() {
			return _progressListener;
		}

		@Override
		public IDirectory getArtifactStorePath() {
			return _artifactStorePath;
		}

		@Override
		public ITempNaming getFileNaming() {
			return _fileNaming;
		}

		@Override
		public String getDownloadPrefix() {
			return _downloadPrefix;
		}

		@Override
		public String getUserAgent() {
			return _userAgent;
		}

		@Override
		public IPackageResolver getPackageResolver() {
			return _packageResolver;
		}

		@Override
		public ITimeoutConfig getTimeoutConfig() {
			return _timeoutConfig;
		}

		@Override
		public IProxyFactory proxyFactory() {
			return _proxyFactory;
		}
	}

}
