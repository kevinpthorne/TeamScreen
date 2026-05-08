package com.aptitekk.TeamScreen.Net.Listeners;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.NetHandler;


/**
 * Listens to packets sent to the server from client
 *
 * @author kevint.
 *         Created Feb 14, 2014.
 */
public class PacketListener extends Thread{
	
	public boolean stopping = false;
	private Socket connectionSocket;
	private NetHandler master;
	
	/**
	 * 
	 *
	 * @param netHandler
	 * @param connectionSocket
	 */
	public PacketListener(NetHandler netHandler, Socket connectionSocket) {
		super("PacketListener");
		this.connectionSocket = connectionSocket;
		this.master = netHandler;
	}
	
	/**
	 * Opens for packets on specified port
	 *
	 */
	@Override
	public void run() {
		while(!this.stopping)
		{
			try
			{
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				connectionSocket.setSoTimeout(20000);
				System.out.println("Reading from client...");
				String clientPacket = inFromClient.readLine();
				if(clientPacket == null || clientPacket.equals("null"))
				{
					System.out.println("Null packet received:(");
					this.stopping = true;
					this.interrupt();
					break;
				}
				System.out.println("Received: " + clientPacket);
				String toSend = "";
				
				toSend = this.master.getProcessor().onPacketReceived(clientPacket);
				if(toSend == null) {
					System.out.println("Transitioning...");
					this.stopping = true;
					this.interrupt();
					System.gc();
					break;
				}
				
			    if(!toSend.equals(""))
			    	outToClient.writeBytes(toSend +"\r\n");
			    System.out.println("Sent: "+toSend);
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				this.stopping = true;
				this.interrupt();
			} catch (SocketException e) {
				this.stopping = true;
				this.interrupt();
			} catch (IOException e) {
				e.printStackTrace();
				this.stopping = true;
				this.interrupt();
			}
			this.interrupt();
			System.gc();
		}
	}
	
	private boolean isIDValid(String ID, String token)
	{
		//do some database checks to see if id is valid with token
		//could also take this opportunity to log which computer is sending a packet.
		return true;
	}

}
