package com.aptitekk.TeamScreen.Net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.Broadcaster.TeamScreenBroadcastThread;

/**
 * Yay for self-explanatory names :D
 *
 * @author kevint.
 *         Created Mar 15, 2014.
 */
public class PacketProcessor {
	
	NetHandler master;
	
	public PacketProcessor(NetHandler nethandler) {
		this.master = nethandler;
	}

	/**
	 * on Packet received event
	 *
	 * @return
	 */
	public String onPacketReceived(String packetReceived) {
		String[] packetSplit = packetReceived.split(NetHandler.PACKET_SEPARATOR_CONSTANT);
		//[0] is type of packet (request, close, etc), [1] is value
		
		
		if(packetSplit.length >= 2)
			if(packetSplit[0].equalsIgnoreCase("request")) {
				if(packetSplit[1].equalsIgnoreCase("accept")) {
					//start jrc server
					this.master.startNewJRCServer(TeamScreenDaemon.windowPushed);
					try {
						return buildPacket(new String[]{"connectToMe",
								TeamScreenBroadcastThread.getLanIP(),
								TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot"),
								TeamScreenDaemon.windowPushed.getTitle()});
					} catch (Exception exception) {
						exception.printStackTrace();
						return null;
					}
				} else {
					System.err.println(packetSplit[1]);
					return null;
				}
			}  else if(packetSplit[0].equalsIgnoreCase("connectToMe")){
				List<String> slots = Arrays.asList(new String[]{"A","B","C","D","E","F"});
		        Integer[] ports = new Integer[]{4455, 4456, 4457, 4458, 4459, 4460};
		        int port = ports[slots.indexOf(packetSplit[2])];
				this.master.startNewJRCClient(packetSplit[1], port, packetSplit[3]);
				if(TeamScreenDaemon.getVerbose())
					System.out.println("Starting client, terminating communicator thread.");
				return null;
			} else {
				System.err.println("Invalid packet header received. \"" + packetReceived + "\" len: "+packetSplit.length);
				return null;
			}
		else {
			if(packetReceived.equalsIgnoreCase("request")) {
				return buildPacket(new String[]{"request","accept"});
			}else {
				System.err.println("Invalid packet header received. \"" + packetReceived + "\" len: "+packetSplit.length);
				return null;
			}
		}
		
	}
	
	private String buildPacket(String[] args) {
		String finalPacket = "";
		int i = 0;
		for(String s : args) {
			i++;
			//if(i != args.length) {
				finalPacket = finalPacket + s + NetHandler.PACKET_SEPARATOR_CONSTANT;
			//} else {
				//finalPacket = finalPacket + s;
			//}
		}
		return finalPacket;
	}

}
