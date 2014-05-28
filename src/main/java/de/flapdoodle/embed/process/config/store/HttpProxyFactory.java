package de.flapdoodle.embed.process.config.store;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class HttpProxyFactory implements IProxyFactory {

	private final String _hostName;
	private final int _port;

	public HttpProxyFactory(String hostName, int port) {
		_hostName = hostName;
		_port = port;
	}

	@Override
	public Proxy createProxy() {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_hostName, _port));
	}

}
