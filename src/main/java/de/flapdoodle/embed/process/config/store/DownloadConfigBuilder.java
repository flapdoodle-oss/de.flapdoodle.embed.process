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
package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.ImmutableContainer;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.progress.IProgressListener;
import de.flapdoodle.embed.process.store.Downloader;


public class DownloadConfigBuilder extends AbstractBuilder<IDownloadConfig> {

	private static final String USER_AGENT = "UserAgent";
	private static final String PROGRESS_LISTENER = "ProgressListener";
	private static final String FILE_NAMING = "FileNaming";
	private static final String ARTIFACT_STORE_PATH = "ArtifactStorePath";
	private static final String PACKAGE_RESOLVER = "PackageResolver";
	private static final String DOWNLOAD_PREFIX = "DownloadPrefix";
	private static final String DOWNLOAD_PATH = "DownloadPath";
	private static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
	private static final String READ_TIMEOUT = "ReadTimeout";


	public DownloadConfigBuilder downloadPath(String path) {
		set(DOWNLOAD_PATH, DownloadPath.class, new DownloadPath(path));
		return this;
	}

	public DownloadConfigBuilder downloadPrefix(String prefix) {
		set(DOWNLOAD_PREFIX, DownloadPrefix.class, new DownloadPrefix(prefix));
		return this;
	}

	public DownloadConfigBuilder packageResolver(IPackageResolver packageResolver) {
		set(PACKAGE_RESOLVER, IPackageResolver.class, packageResolver);
		return this;
	}

	public DownloadConfigBuilder artifactStorePath(IDirectory artifactStorePath) {
		set(ARTIFACT_STORE_PATH, IDirectory.class, artifactStorePath);
		return this;
	}

	public DownloadConfigBuilder fileNaming(ITempNaming fileNaming) {
		set(FILE_NAMING, ITempNaming.class, fileNaming);
		return this;
	}

	public DownloadConfigBuilder progressListener(IProgressListener progressListener) {
		set(PROGRESS_LISTENER, IProgressListener.class, progressListener);
		return this;
	}

	public DownloadConfigBuilder userAgent(String userAgent) {
		set(USER_AGENT, UserAgent.class, new UserAgent(userAgent));
		return this;
	}

	/**
	 * Connection timeout in milliseconds.
	 * @param timeout
	 * @return
	 */
	public DownloadConfigBuilder connectionTimeout(int timeout) {
		set(CONNECTION_TIMEOUT, int.class, timeout);
		return this;
	}

	/**
	 * Read timeout in milliseconds.
	 * @param timeout
	 * @return
	 */
	public DownloadConfigBuilder readTimeout(int timeout) {
		set(READ_TIMEOUT, int.class, timeout);
		return this;
	}

	@Override
	public IDownloadConfig build() {
		final String downloadPath = get(DOWNLOAD_PATH, DownloadPath.class).value();
		final String downloadPrefix = get(DOWNLOAD_PREFIX, DownloadPrefix.class).value();
		final IPackageResolver packageResolver = get(PACKAGE_RESOLVER, IPackageResolver.class);
		final IDirectory artifactStorePath = get(ARTIFACT_STORE_PATH, IDirectory.class);
		final ITempNaming fileNaming = get(FILE_NAMING, ITempNaming.class);
		final IProgressListener progressListener = get(PROGRESS_LISTENER, IProgressListener.class);
		final String userAgent = get(USER_AGENT, UserAgent.class).value();
		final int connectionTimeout = getOrDefault(CONNECTION_TIMEOUT, int.class, Downloader.CONNECTION_TIMEOUT);
		final int readTimeout = getOrDefault(READ_TIMEOUT, int.class, Downloader.READ_TIMEOUT);

		return new ImmutableDownloadConfig(downloadPath, downloadPrefix, packageResolver, artifactStorePath, fileNaming,
				progressListener, userAgent, connectionTimeout, readTimeout);
	}

	protected static class DownloadPath extends ImmutableContainer<String> {

		public DownloadPath(String value) {
			super(value);
		}

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

		private final String _downloadPath;
		private final IProgressListener _progressListener;
		private final IDirectory _artifactStorePath;
		private final ITempNaming _fileNaming;
		private final String _downloadPrefix;
		private final String _userAgent;
		private final IPackageResolver _packageResolver;
		private final int _connectionTimeout;
		private final int _readTimeout;

		public ImmutableDownloadConfig(String downloadPath, String downloadPrefix, IPackageResolver packageResolver,
		                               IDirectory artifactStorePath, ITempNaming fileNaming,
		                               IProgressListener progressListener, String userAgent,
		                               int connectionTimeout, int readTimeout) {
			super();
			_downloadPath = downloadPath;
			_downloadPrefix = downloadPrefix;
			_packageResolver = packageResolver;
			_artifactStorePath = artifactStorePath;
			_fileNaming = fileNaming;
			_progressListener = progressListener;
			_userAgent = userAgent;
			_connectionTimeout = connectionTimeout;
			_readTimeout = readTimeout;
		}

		@Override
		public String getDownloadPath() {
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
		public int getConnectionTimeout() {
			return _connectionTimeout;
		}

		@Override
		public int getReadTimeout() {
			return _readTimeout;
		}

	}

}
