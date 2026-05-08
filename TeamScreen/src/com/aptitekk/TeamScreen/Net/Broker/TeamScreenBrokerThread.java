package com.aptitekk.TeamScreen.Net.Broker;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import javax.swing.JOptionPane;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.NetHandler;

/**
 * Sends packets
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class TeamScreenBrokerThread extends Thread{

	NetHandler master;
	String toSend;
	InetAddress address;
	boolean enabled = true;

	public TeamScreenBrokerThread(NetHandler master, String toSend, InetAddress address) {
		super("TeamScreenBrokerThread");
		this.toSend = toSend;
		this.address = address;
		this.master = master;
	}

	@Override
	public void run() {
		try {
			SocketAddress saddress = new InetSocketAddress(this.address, 25005);
			Socket clientSocket = new Socket();
			clientSocket.connect(saddress, 20000);
			while(enabled) {
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outToServer.writeBytes(this.toSend + "\r\n");
				System.out.println("Sent: " + this.toSend);
				String receivedPacket = inFromServer.readLine();
				System.out.println("Received: " + receivedPacket);
				String toReturn = this.master.getProcessor().onPacketReceived(receivedPacket)+ "\r\n";
				System.out.println("Returned: " + toReturn);
				outToServer.writeBytes(toReturn);
				clientSocket.close();
				enabled = false;
			}
		} catch (SocketTimeoutException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(null,
				    "TeamScreen couldn't connect to the neighbor you wanted to share with.\nPlease make sure that:"+
				    		"\n\r1. The machine is on\n\r2. They possess the same IP address that is in your settings\n\r"+
				    		"3. Double-check that TeamScreen is running",
				    "Could not connect",
				    JOptionPane.ERROR_MESSAGE);
		} catch (IOException exception) {
			exception.printStackTrace();
		} 
		this.interrupt();
		System.gc();
	}

}
