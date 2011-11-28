package com.robbix.mp5.res;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceServer
{
	private static final AtomicInteger nextSerial = new AtomicInteger();
	
	private int listenPort;
	private DoListen listener;
	private Thread listenThread;
	
	public ResourceServer(int listenPort)
	{
		this.listenPort = listenPort;
	}
	
	public synchronized void start()
	{
		if (listener != null && listener.running)
			return;
		
		listener = new DoListen();
		listener.running = true;
		listenThread = new Thread(listener);
		listenThread.setName("MP5-ResourceServer-" + nextSerial.getAndIncrement());
		listenThread.setDaemon(true);
		listenThread.start();
	}
	
	public synchronized void stop()
	{
		if (listener == null)
			return;
	}
	
	private class DoListen implements Runnable
	{
		public boolean running = false;
		
		public void run()
		{
			while (running)
			{
				ServerSocket listenSocket;
				
				try
				{
					listenSocket = new ServerSocket(listenPort);
					listenSocket.setSoTimeout(30000);
					Socket socket = listenSocket.accept();
					socket.setSoTimeout(30000);
					
					// Read request
					DataInputStream in = new DataInputStream(socket.getInputStream());
					checkHeader(in);
					Request request = Request.valueOf(in.readUTF());
//					String moduleName = in.readUTF();
					
					switch (request)
					{
					case SPRITE_SET:
						break;
					default: throw new IOException("Request type not supported: " + request);
					}
					
					// Write response
					
					new OutputStreamWriter(socket.getOutputStream()).write("Have a nice day!");
				}
				catch (SocketTimeoutException ste)
				{
					continue;
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	}
	
	public static enum Request
	{
		SPRITE_SET, SOUND, UNIT_TYPE, CURSOR, TILE_SET, MAP
	}
	
	private static final byte[] MAGIC = "MP5RES".getBytes();
	
	private void checkHeader(DataInputStream in) throws IOException
	{
		byte[] header = new byte[MAGIC.length];
		in.readFully(header);
		
		if (!Arrays.equals(header, MAGIC))
			throw new IOException("Invalid Magic");
	}
}
