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

import java.util.Optional;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.progress.ProgressListener;

public class DownloadConfigBuilder extends AbstractBuilder<DownloadConfig> {

	private static final TypedProperty<UserAgent> USER_AGENT = TypedProperty.with("UserAgent", UserAgent.class);
    private static final TypedProperty<Authorization> AUTHORIZATION = TypedProperty.with("Authorization", Authorization.class);
	private static final TypedProperty<ProgressListener> PROGRESS_LISTENER = TypedProperty.with("ProgressListener", ProgressListener.class);
	private static final TypedProperty<ITempNaming> FILE_NAMING = TypedProperty.with("FileNaming", ITempNaming.class);
	private static final TypedProperty<Directory> ARTIFACT_STORE_PATH = TypedProperty.with("ArtifactStorePath",	Directory.class);
	private static final TypedProperty<PackageResolver> PACKAGE_RESOLVER = TypedProperty.with("PackageResolver",	PackageResolver.class);
	private static final TypedProperty<DownloadPrefix> DOWNLOAD_PREFIX = TypedProperty.with("DownloadPrefix",	DownloadPrefix.class);
	private static final TypedProperty<DistributionDownloadPath> DOWNLOAD_PATH = TypedProperty.with("DownloadPath",	DistributionDownloadPath.class);

	private static final TypedProperty<TimeoutConfig> TIMEOUT_CONFIG = TypedProperty.with("TimeoutConfig",	TimeoutConfig.class);
	private static final TypedProperty<ProxyFactory> PROXY_FACTORY = TypedProperty.with("ProxyFactory",	ProxyFactory.class);

	public DownloadConfigBuilder() {
		setDefault(TIMEOUT_CONFIG, TimeoutConfig.defaults());
	}

	public DownloadConfigBuilder downloadPath(String path) {
		set(DOWNLOAD_PATH, new SameDownloadPathForEveryDistribution(path));
		return this;
	}

	protected IProperty<DistributionDownloadPath> downloadPath() {
		return property(DOWNLOAD_PATH);
	}

	public DownloadConfigBuilder downloadPrefix(String prefix) {
		set(DOWNLOAD_PREFIX, new DownloadPrefix(prefix));
		return this;
	}

	protected IProperty<DownloadPrefix> downloadPrefix() {
		return property(DOWNLOAD_PREFIX);
	}

	public DownloadConfigBuilder packageResolver(PackageResolver packageResolver) {
		set(PACKAGE_RESOLVER, packageResolver);
		return this;
	}

	protected IProperty<PackageResolver> packageResolver() {
		return property(PACKAGE_RESOLVER);
	}

	public DownloadConfigBuilder artifactStorePath(Directory artifactStorePath) {
		set(ARTIFACT_STORE_PATH, artifactStorePath);
		return this;
	}

	protected IProperty<Directory> artifactStorePath() {
		return property(ARTIFACT_STORE_PATH);
	}

	public DownloadConfigBuilder fileNaming(ITempNaming fileNaming) {
		set(FILE_NAMING, fileNaming);
		return this;
	}

	protected IProperty<ITempNaming> fileNaming() {
		return property(FILE_NAMING);
	}

	public DownloadConfigBuilder progressListener(ProgressListener progressListener) {
		set(PROGRESS_LISTENER, progressListener);
		return this;
	}

	protected IProperty<ProgressListener> progressListener() {
		return property(PROGRESS_LISTENER);
	}

	public DownloadConfigBuilder userAgent(String userAgent) {
		set(USER_AGENT, new UserAgent(userAgent));
		return this;
	}

	public DownloadConfigBuilder authorization(String authorization) {
		set(AUTHORIZATION, new Authorization(authorization));
		return this;
	}

	protected IProperty<UserAgent> userAgent() {
		return property(USER_AGENT);
	}

	public DownloadConfigBuilder timeoutConfig(TimeoutConfig timeoutConfig) {
		set(TIMEOUT_CONFIG, timeoutConfig);
		return this;
	}

	protected IProperty<TimeoutConfig> timeoutConfig() {
		return property(TIMEOUT_CONFIG);
	}

	public DownloadConfigBuilder proxyFactory(ProxyFactory proxyFactory) {
		set(PROXY_FACTORY, proxyFactory);
		return this;
	}

	protected IProperty<ProxyFactory> proxyFactory() {
		return property(PROXY_FACTORY);
	}

	@Override
	public DownloadConfig build() {
		final DistributionDownloadPath downloadPath = get(DOWNLOAD_PATH);
		final String downloadPrefix = get(DOWNLOAD_PREFIX).value();
		final PackageResolver packageResolver = get(PACKAGE_RESOLVER);
		final Directory artifactStorePath = get(ARTIFACT_STORE_PATH);
		final ITempNaming fileNaming = get(FILE_NAMING);
		final ProgressListener progressListener = get(PROGRESS_LISTENER);
		final String userAgent = get(USER_AGENT).value();
		final String authorization = get(AUTHORIZATION, new Authorization("")).value();
		final TimeoutConfig timeoutConfig = get(TIMEOUT_CONFIG);
		final ProxyFactory proxyFactory = get(PROXY_FACTORY, null);

		return new ImmutableDownloadConfig(downloadPath, downloadPrefix, packageResolver, artifactStorePath, fileNaming,
				progressListener, userAgent, authorization, timeoutConfig, Optional.ofNullable(proxyFactory));
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

	protected static class Authorization extends ImmutableContainer<String> {

		public Authorization(String value) { super(value); }

	}

	protected static class ImmutableDownloadConfig implements DownloadConfig {

		private final DistributionDownloadPath _downloadPath;
		private final ProgressListener _progressListener;
		private final Directory _artifactStorePath;
		private final ITempNaming _fileNaming;
		private final String _downloadPrefix;
		private final String _userAgent;
		private final String _authorization;
		private final PackageResolver _packageResolver;
		private final TimeoutConfig _timeoutConfig;
		private final Optional<ProxyFactory> _proxyFactory;

		public ImmutableDownloadConfig(DistributionDownloadPath downloadPath, String downloadPrefix, PackageResolver packageResolver,
				Directory artifactStorePath, ITempNaming fileNaming, ProgressListener progressListener, String userAgent,
				String authorization, TimeoutConfig timeoutConfig, Optional<ProxyFactory> proxyFactory) {
			super();
			_downloadPath = downloadPath;
			_downloadPrefix = downloadPrefix;
			_packageResolver = packageResolver;
			_artifactStorePath = artifactStorePath;
			_fileNaming = fileNaming;
			_progressListener = progressListener;
			_userAgent = userAgent;
			_authorization = authorization;
			_timeoutConfig = timeoutConfig;
			_proxyFactory = proxyFactory;
		}

		@Override
		public DistributionDownloadPath getDownloadPath() {
			return _downloadPath;
		}

		@Override
		public ProgressListener getProgressListener() {
			return _progressListener;
		}

		@Override
		public Directory getArtifactStorePath() {
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
		public String getAuthorization() {
			return _authorization;
		}

		@Override
		public PackageResolver getPackageResolver() {
			return _packageResolver;
		}

		@Override
		public TimeoutConfig getTimeoutConfig() {
			return _timeoutConfig;
		}

		@Override
		public Optional<ProxyFactory> proxyFactory() {
			return _proxyFactory;
		}
	}

}
