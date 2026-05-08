package com.aptitekk.TeamScreen.Net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ListModel;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Settings.ConfigureNeighbor;



/**
 * 
 * Looks for other alive TeamScreen machines on the LAN
 * 
 * @author kevint
 *
 */
public class RadarThread extends Thread{
	
	ConfigureNeighbor master;
	
	MulticastSocket socket;
	InetAddress group;
	boolean stillRunning = true;

	public RadarThread(ConfigureNeighbor instance) {
		super("TeamScreenRadarThread");
		
		master = instance;
		master.aliveNeighborsModel = new DefaultComboBoxModel<String>();
		master.rawAliveNeighborInfo= new ArrayList<String>();
	}
	
	@Override
	public void run() {
		try 
		{
			List<String> slots = Arrays.asList(new String[]{"A","B","C","D","E","F"});
            Integer[] ports = new Integer[]{4455, 4456, 4457, 4458, 4459, 4460};
            
            int port = ports[slots.indexOf(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot"))];
			socket = new MulticastSocket(port);
			group = InetAddress.getByName("224.0.2.12");
			socket.joinGroup(group);
			socket.setSoTimeout(50000);
			DatagramPacket packet;
			while (stillRunning && !Thread.interrupted()) 
			{
			    byte[] buf = new byte[40];
			    packet = new DatagramPacket(buf, buf.length);
			    socket.receive(packet);
	
			    String received = new String(packet.getData());
			    received = received.replaceAll("[\\n\\t\\s\\r ]", "");
			    
			    String[] splitData = received.split(NetHandler.PACKET_SEPARATOR_CONSTANT);
			    
			    String resolvedData = splitData[0];
			    
			    boolean alreadyAdded = false;
			    
			    if(master.aliveNeighborsModel.getIndexOf(resolvedData) != -1)
			    	alreadyAdded = true;
			    
			    if(!alreadyAdded) {
			    	master.aliveNeighborsModel.addElement(resolvedData);
			    	master.rawAliveNeighborInfo.add(received);
			    	System.out.println("Found " + received);
			    	master.refreshAliveHosts();
			    }
			    
			    Thread.sleep(100);
			}
			socket.leaveGroup(group);
			socket.close();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			master.aliveNeighborsModel.removeAllElements();
			master.rawAliveNeighborInfo.clear();
			master.aliveNeighborsModel.addElement("<None found>");
			master.refreshAliveHosts();
			this.interrupt();
		} catch (SocketException e) {
			e.printStackTrace();
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			;
		}
	
	}

}
