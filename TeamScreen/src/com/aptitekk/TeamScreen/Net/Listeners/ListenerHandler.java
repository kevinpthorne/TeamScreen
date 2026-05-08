package com.aptitekk.TeamScreen.Net.Listeners;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.NetHandler;

/**
 * Handles the listener threads
 *
 * @author kevint.
 *         Created Feb 17, 2014.
 */
public class ListenerHandler extends Thread{
	
	public ArrayList<PacketListener> listenerList;
	public ServerSocket serverSocket;
	private NetHandler netHandler;
	private int port;

	public ListenerHandler(NetHandler netHandler, int port)
	{
		super("ListenerHandler");
		setDaemon(true);
		this.listenerList = new ArrayList<PacketListener>();
		this.netHandler = netHandler;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			this.serverSocket = new ServerSocket(port);
			this.serverSocket.setSoTimeout(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true)
		{
			try {
				Socket connectionSocket = serverSocket.accept();
				System.out.println("Socket Accepted!");
				
				PacketListener listener = new PacketListener(netHandler, connectionSocket);
				this.listenerList.add(listener);
				listener.stopping = false;
				listener.start();
			} catch (IOException e) {
				break;
			}			
		}
	}
	
}
