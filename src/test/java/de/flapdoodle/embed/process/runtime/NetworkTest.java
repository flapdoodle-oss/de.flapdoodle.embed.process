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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashSet;

import org.junit.Test;

public class NetworkTest {

	@Test(expected=IllegalArgumentException.class)
	public void freeNetworkPortFailOnPoolSizeSmallerThanOne() throws IOException {
		InetAddress address = Network.getLocalHost();
		Network.getFreeServerPorts(address, 0);
	}
	
	@Test
	public void freeNetworkPortMustReturnDifferentAvailablePorts() throws IOException {
		InetAddress address = Network.getLocalHost();
		int[] ports = Network.getFreeServerPorts(address, 5);
		assertTrue(ports.length>0);
		for (int port : ports) {
			ServerSocket serverSocket = new ServerSocket(port, 0, address);
			assertNotNull(serverSocket);
			serverSocket.close();
		}
		HashSet<Integer> set= new HashSet<>();
		for (int port : ports) {
			set.add(port);
		}
		assertEquals(5,set.size());
	}
	
	@Test(expected=IOException.class)
	public void freeNetworkPortMustFailIfPoolIsTooLarge() throws IOException {
		InetAddress address = Network.getLocalHost();
		Network.getFreeServerPorts(address, 50000);
	}
}
