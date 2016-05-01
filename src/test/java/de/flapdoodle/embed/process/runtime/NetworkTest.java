package de.flapdoodle.embed.process.runtime;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.junit.Test;

public class NetworkTest {

	@Test(expected=IllegalArgumentException.class)
	public void freeNetworkPortFailOnPoolSizeSmallerThanOne() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		Network.getFreeServerPorts(address, 0);
	}
	
	@Test
	public void freeNetworkPortMustReturnValidResult() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		int[] ports = Network.getFreeServerPorts(address, 5);
		assertTrue(ports.length>0);
		for (int port : ports) {
			ServerSocket serverSocket = new ServerSocket(port, 0, address);
			assertNotNull(serverSocket);
			serverSocket.close();
		}
	}
	
	@Test
	public void freeNetworkPortMustReturnAnyValueIfPoolIsTooLarge() throws UnknownHostException, IOException {
		InetAddress address = Network.getLocalHost();
		int[] ports = Network.getFreeServerPorts(address, 50000);
		assertTrue(ports.length>0);
	}
}
