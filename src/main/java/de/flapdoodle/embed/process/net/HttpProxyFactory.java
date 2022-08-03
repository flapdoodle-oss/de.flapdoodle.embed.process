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
package de.flapdoodle.embed.process.net;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class HttpProxyFactory implements ProxyFactory {

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
