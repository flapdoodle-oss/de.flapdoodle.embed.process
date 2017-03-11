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
			if (ipAddr.length > IPV4_LENGTH) {
				return true;
			}
			return false;
		} catch (UnknownHostException ux) {
			logger.error(NO_LOCALHOST_ERROR_MESSAGE, ux);
			throw ux;
		}
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		InetAddress ret = InetAddress.getLocalHost();
		if (!ret.isLoopbackAddress()) {
			ret = InetAddress.getByName("localhost");
			if (!ret.isLoopbackAddress()) {
				logger.error("{} is not a loopback address", ret.getHostAddress());
			}
		}
		return ret;
	}

	public static int getPreferredFreeServerPort(int preferredPort) throws IOException {
		return getPreferredFreeServerPort(getLocalHost(), preferredPort);
	}

	public static int getFreeServerPort() throws IOException {
		return getFreeServerPort(getLocalHost());
	}

	public static int getPreferredFreeServerPort(InetAddress hostAddress, int preferredPort) throws IOException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(preferredPort, 0, hostAddress);
			return preferredPort;
		} catch (IOException e) {
			return getFreeServerPort(hostAddress);
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}

	public static int getFreeServerPort(InetAddress hostAddress) throws IOException {
		int ports[] = getFreeServerPorts(hostAddress, 10);
		return randomEntryOf(ports);
	}

	public static int randomEntryOf(int[] ports) {
		return ports[ThreadLocalRandom.current().nextInt(ports.length)];
	}
	
	public static int[] getFreeServerPorts(InetAddress hostAddress, int poolSize) throws IOException {
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
