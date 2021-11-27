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
package de.flapdoodle.embed.process.runtime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Network {

	private static final Logger logger = LoggerFactory.getLogger(Network.class);
	
	private static final String NO_LOCALHOST_ERROR_MESSAGE = "We could not detect if localhost is IPv4 or IPv6. " +
			"Sometimes there is no entry for localhost. " +
			"If 'ping localhost' does not work, it could help to add the right entry in your hosts configuration file.";
	private static final int IPV4_LENGTH = 4;

	private Network() {
		throw new IllegalAccessError("singleton");
	}

	public static boolean localhostIsIPv6() throws UnknownHostException {
		try {
			InetAddress addr = getLocalHost();
			byte[] ipAddr = addr.getAddress();
			return ipAddr.length > IPV4_LENGTH;
		} catch (UnknownHostException ux) {
			logger.error(NO_LOCALHOST_ERROR_MESSAGE, ux);
			throw ux;
		}
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		InetAddress ret = InetAddress.getLocalHost();
		// see https://www.linuxtopia.org/online_books/linux_system_administration/debian_linux_guides/debian_linux_reference_guide/ch-gateway.en_009.html
		// call to getLocalHost() can give 127.0.1.1 which is not the same as localhost and will lead to trouble
		// if used to connect services
		if (!ret.isLoopbackAddress() || ret.getHostAddress().equals("127.0.1.1")) {
			ret = localHostByName();
		}
		return ret;
	}

	private static InetAddress localHostByName() throws UnknownHostException {
		InetAddress ret;
		ret = InetAddress.getByName("localhost");
		if (!ret.isLoopbackAddress()) {
			logger.error("{} is not a loopback address", ret.getHostAddress());
		}
		return ret;
	}

	@Deprecated
	public static int getPreferredFreeServerPort(int preferredPort) throws IOException {
		return getPreferredFreeServerPort(getLocalHost(), preferredPort);
	}

	@Deprecated
	public static int getFreeServerPort() throws IOException {
		return freeServerPort(getLocalHost());
	}

	/**
	 * @see Network#freeServerPort(InetAddress, int)
	 */
	@Deprecated
	public static int getPreferredFreeServerPort(InetAddress hostAddress, int preferredPort) throws IOException {
		return freeServerPort(hostAddress, preferredPort);
	}
	
	public static int freeServerPort(InetAddress hostAddress, int preferredPort) throws IOException {
		try {
			try(Socket socket = new Socket(hostAddress, preferredPort)) {
				return freeServerPort(hostAddress);
			}
		} catch (Exception ex) {
			return preferredPort;
		}
	}

	public static int freeServerPort(InetAddress hostAddress) throws IOException {
		try(ServerSocket socket = new ServerSocket(0,0,hostAddress)) {
			return socket.getLocalPort();
		}
	}

	/**
	 * @see Network#freeServerPort(InetAddress)
	 */
	@Deprecated
	public static int getFreeServerPort(InetAddress hostAddress) throws IOException {
		return freeServerPort(hostAddress);
	}

	/**
	 * @see Network#freeServerPorts(InetAddress, int)
	 */
	@Deprecated
	public static int[] getFreeServerPorts(InetAddress hostAddress, int poolSize) throws IOException {
		return freeServerPorts(hostAddress, poolSize);
	}

	public static int[] freeServerPorts(InetAddress hostAddress, int poolSize) throws IOException {
		if (poolSize<1) {
			throw new IllegalArgumentException("poolSize < 1: "+poolSize);
		}
		
		final ServerSocket[] sockets=new ServerSocket[poolSize];
		final int ports[]=new int[poolSize];
		
		int idx=0;
		try {
			do {
				sockets[idx]=new ServerSocket(0, 0, hostAddress);
				ports[idx]=sockets[idx].getLocalPort();
				idx++;
			} while (idx<poolSize);
			return ports;
		} finally {
			for (int i=0;i<idx;i++) {
				sockets[i].close();
			}
		}
	}

}
