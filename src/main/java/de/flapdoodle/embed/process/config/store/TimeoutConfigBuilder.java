package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.ImmutableContainer;

public class TimeoutConfigBuilder extends AbstractBuilder<ITimeoutConfig> {

	private static final String READ_TIMEOUT = "ReadTimeout";
	private static final String CONNECTION_TIMEOUT = "ConnectionTimeout";

	public TimeoutConfigBuilder connectionTimeout(int connectionTimeout) {
		set(CONNECTION_TIMEOUT, ConnectionTimeout.class, new ConnectionTimeout(connectionTimeout));
		return this;
	}

	public TimeoutConfigBuilder readTimeout(int connectionTimeout) {
		set(READ_TIMEOUT, ReadTimeout.class, new ReadTimeout(connectionTimeout));
		return this;
	}
	
	public TimeoutConfigBuilder defaults() {
		setDefault(CONNECTION_TIMEOUT, ConnectionTimeout.class, new ConnectionTimeout(10000));
		setDefault(CONNECTION_TIMEOUT, ReadTimeout.class, new ReadTimeout(10000));
		return this;
	}

	@Override
	public ITimeoutConfig build() {
		final int connectionTimeout = get(CONNECTION_TIMEOUT, ConnectionTimeout.class).value();
		final int readTimeout = get(READ_TIMEOUT, ReadTimeout.class).value();

		return new ImmutableTimeoutConfig(connectionTimeout,readTimeout);
	}

	static class ConnectionTimeout extends ImmutableContainer<Integer> {

		public ConnectionTimeout(int value) {
			super(value);
		}

	}
	
	static class ReadTimeout extends ImmutableContainer<Integer> {

		public ReadTimeout(int value) {
			super(value);
		}

	}
	
	static class ImmutableTimeoutConfig implements ITimeoutConfig {

		private final int _connectionTimeout;
		private final int _readTimeout;

		public ImmutableTimeoutConfig(int connectionTimeout, int readTimeout) {
			_connectionTimeout = connectionTimeout;
			_readTimeout = readTimeout;
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
