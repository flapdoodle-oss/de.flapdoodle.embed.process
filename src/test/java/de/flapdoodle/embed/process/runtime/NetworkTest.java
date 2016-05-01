package de.flapdoodle.embed.process.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashSet;

import org.junit.Test;

public class NetworkTest {

	@Test(expected=IllegalArgumentException.class)
	public void freeNetworkPortFailOnPoolSizeSmallerThanOne() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		Network.getFreeServerPorts(address, 0);
	}
	
	@Test
	public void freeNetworkPortMustReturnDifferentAviablePorts() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		int[] ports = Network.getFreeServerPorts(address, 5);
		assertTrue(ports.length>0);
		for (int port : ports) {
			ServerSocket serverSocket = new ServerSocket(port, 0, address);
			assertNotNull(serverSocket);
			serverSocket.close();
		}
		HashSet<Integer> set=new HashSet<Integer>();
		for (int port : ports) {
			set.add(port);
		}
		assertEquals(5,set.size());
	}
	
	@Test(expected=IOException.class)
	public void freeNetworkPortMustFailIfPoolIsTooLarge() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		Network.getFreeServerPorts(address, 50000);
	}
}
