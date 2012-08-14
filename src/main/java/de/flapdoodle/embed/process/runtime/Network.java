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
package de.flapdoodle.embed.process.runtime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 *
 */
public class Network {

	private static Logger logger = Logger.getLogger(Network.class.getName());
	public static final int IP_LENGTH = 4;

	private Network() {
		throw new IllegalAccessError("singleton");
	}

	public static boolean localhostIsIPv6() throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		byte[] ipAddr = addr.getAddress();
		if (ipAddr.length > IP_LENGTH)
			return true;
		return false;
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		InetAddress ret = InetAddress.getLocalHost();
		if (!ret.isLoopbackAddress()) {
			ret = InetAddress.getByName("localhost");
			if (!ret.isLoopbackAddress()) {
				logger.severe("" + ret.getHostAddress() + " is not a loopback address");
			}
		}
		return ret;
	}

	public static int getFreeServerPort() throws IOException {
		return getFreeServerPort(getLocalHost());
	}
	
	public static int getFreeServerPort(InetAddress hostAdress) throws IOException {
		int ret;
		ServerSocket socket = new ServerSocket(0, 0, hostAdress);
		ret = socket.getLocalPort();
		socket.close();
		return ret;
	}

}
