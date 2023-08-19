/*
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkTest {

	@Test
	public void freeNetworkPortFailOnPoolSizeSmallerThanOne() throws IOException {
		InetAddress address = Network.getLocalHost();
		assertThrows(IllegalArgumentException.class, () -> Network.freeServerPorts(address, 0));
	}

	@Test
	public void freeNetworkPortMustReturnDifferentAvailablePorts() throws IOException {
		InetAddress address = Network.getLocalHost();
		int[] ports = Network.freeServerPorts(address, 5);
		assertTrue(ports.length > 0);
		for (int port : ports) {
			ServerSocket serverSocket = new ServerSocket(port, 0, address);
			assertNotNull(serverSocket);
			serverSocket.close();
		}
		HashSet<Integer> set = new HashSet<>();
		for (int port : ports) {
			set.add(port);
		}
		assertEquals(5, set.size());
	}

	@Test
	@Disabled("just run this test if you do changes to Network.freeServerPorts")
	public void freeNetworkPortMustFailIfPoolIsTooLarge() throws IOException {
		InetAddress address = Network.getLocalHost();
		assertThrows(IOException.class, () -> Network.freeServerPorts(address, 50000));
	}

	@Test
	public void localHostMustNotBe127_0_1_1() throws UnknownHostException {
		InetAddress address = Network.getLocalHost();
		assertThat(address.getHostAddress()).isNotEqualTo("127.0.1.1");
	}

	@Test
	public void switchToDifferentPortIfPreferedPortIsAllocated() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();

		try (ServerSocket socket = new ServerSocket(0, 0, address)) {
			int freePort = Network.freeServerPort(address, socket.getLocalPort());
			assertThat(freePort).isNotEqualTo(socket.getLocalPort());
			assertThat(freePort).isNotEqualTo(0);
		}
	}
}
