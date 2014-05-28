package de.flapdoodle.embed.process.config.store;

import java.net.Proxy;


public class NoProxyFactory implements IProxyFactory {

	@Override
	public Proxy createProxy() {
		return null;
	}

}
