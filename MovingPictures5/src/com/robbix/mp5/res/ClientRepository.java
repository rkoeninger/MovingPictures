package com.robbix.mp5.res;

import java.io.IOException;
import java.net.Socket;

public class ClientRepository extends Repository
{
	@SuppressWarnings("unused")
	private Socket socket;
	
	public ClientRepository(String host, int port) throws IOException
	{
		socket = new Socket(host, port);
	}
	
	public Resource getResource(String name)
	{
		return null;
	}
}
