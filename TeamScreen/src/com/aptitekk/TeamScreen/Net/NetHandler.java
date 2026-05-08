package com.aptitekk.TeamScreen.Net;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Properties;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Util.JArrowButton;
import com.aptitekk.TeamScreen.Net.Broadcaster.TeamScreenBroadcastThread;
import com.aptitekk.TeamScreen.Net.Broker.TeamScreenBrokerThread;
import com.aptitekk.TeamScreen.Net.Listeners.ListenerHandler;
import com.aptitekk.TeamScreen.Net.Listeners.PacketListener;
import com.aptitekk.TeamScreen.Net.jrc.DynamicJRCServerApplication;
import com.aptitekk.TeamScreen.Net.jrc.ClientApplication;
import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * Manages the network framework of the ClassManager
 *
 * @author kevint. Created Feb 13, 2014.
 */
public class NetHandler {

	public static String PACKET_SEPARATOR_CONSTANT = "~";

	TeamScreenDaemon parent;
	private ServerSocket serverSocket;
	private ListenerHandler listenerHandler;
	protected PacketProcessor packetProcessor;
	protected Thread broadcastThread;
	protected ArrayList<TeamScreenBrokerThread> brokerThreads = new ArrayList<TeamScreenBrokerThread>();
	protected ArrayList<DynamicJRCServerApplication> jrcServerThreads = new ArrayList<DynamicJRCServerApplication>();
	protected ArrayList<ClientApplication> jrcClientThreads = new ArrayList<ClientApplication>();

	public NetHandler(TeamScreenDaemon parent) {
		this.parent = parent;

		this.packetProcessor = new PacketProcessor(this);
		this.broadcastThread = new TeamScreenBroadcastThread(this);

		enable();
	}

	public void enable() {
		startBroadcaster();
		startnewListener(25005);

	}

	public void disable() {
		stopListeners();
		stopBroadcaster();
	}

	public void startnewListener(int port) {
		TeamScreenDaemon.logger.info("Started listening on port " + port);
		this.listenerHandler = new ListenerHandler(this, port);
		listenerHandler.start();
	}

	public PacketProcessor getProcessor() {
		return this.packetProcessor;
	}

	public void stopListeners() {
		for (PacketListener l : listenerHandler.listenerList) {
			l.stopping = true;
			l.interrupt();
		}
		try {
			listenerHandler.serverSocket.close();
		} catch (IOException e) {
		}
	}

	public void startBroadcaster() {
		try {
			this.broadcastThread.start();
		} catch (IllegalThreadStateException e) {
			this.broadcastThread = null;
			this.broadcastThread = new TeamScreenBroadcastThread(this);
			this.broadcastThread.start();
		}
	}

	public void stopBroadcaster() {
		try {
			this.broadcastThread.interrupt();
		} catch (Exception e) {
			TeamScreenDaemon.logger
					.warning("Broadcaster was either unable to be stopped, or doesn't exist. Caused by: "
							+ e.getCause());
		}
	}

	public ArrayList<PacketListener> getListeners() {
		return this.listenerHandler.listenerList;
	}

	public void sendPacket(String toSend, InetAddress address) {
		TeamScreenBrokerThread t = new TeamScreenBrokerThread(this, toSend,
				address);
		t.start();
	}

	public void requestConnecttoNeighbor(int direction) {
		try {
			Properties prop = TeamScreenDaemon.getPropertiesHandler()
					.getConfig();

			// [name]:[IP]:[slot]
			String[] info;

			switch (direction) {
			case JArrowButton.NORTH:
				if (!prop.getProperty("TNeighbor").equals("")) {
					info = prop.getProperty("TNeighbor").split(":");
					break;
				}
				return;
			case JArrowButton.SOUTH:
				if (!prop.getProperty("BNeighbor").equals("")) {
					info = prop.getProperty("BNeighbor").split(":");
					break;
				}
				return;
			case JArrowButton.WEST:
				if (!prop.getProperty("LNeighbor").equals("")) {
					info = prop.getProperty("LNeighbor").split(":");
					break;
				}
				return;
			case JArrowButton.EAST:
				if (!prop.getProperty("RNeighbor").equals("")) {
					info = prop.getProperty("RNeighbor").split(":");
					break;
				}
				return;
			case JArrowButton.SPECIAL:
				if (!prop.getProperty("PNeighbor").equals("")) {
					info = prop.getProperty("PNeighbor").split(":");
					break;
				}
				return;
			default:
				return;
			}
			if (info.length != 3) {
				return;
			} else {
				sendPacket("request", InetAddress.getByName(info[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getStackTrace(Throwable t) {
		StringWriter stringWritter = new StringWriter();
		PrintWriter printWritter = new PrintWriter(stringWritter, true);
		t.printStackTrace(printWritter);
		printWritter.flush();
		stringWritter.flush();

		return stringWritter.toString();
	}

	public void startNewJRCServer(WindowInfo w) {
		DynamicJRCServerApplication t = new DynamicJRCServerApplication(w);
		this.jrcServerThreads.add(t);
		t.start();
	}

	public void stopJRCServers() {
		for (DynamicJRCServerApplication t : this.jrcServerThreads) {
			t.interrupt();
			this.jrcServerThreads.remove(t);
		}
		System.gc();
	}

	public void startNewJRCClient(String address, int port, String windowTitle) {
		ClientApplication t = new ClientApplication(address, port, windowTitle);
		this.jrcClientThreads.add(t);
		t.start();
	}

	public void stopJRCClients() {
		for (ClientApplication t : this.jrcClientThreads) {
			t.interrupt();
			this.jrcClientThreads.remove(t);
		}
		System.gc();
	}

	public static boolean validateIP(String ipAddress) {
		if (ipAddress
				.matches("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")) {
			String[] groups = ipAddress.split("\\.");

			for (int i = 0; i <= 3; i++) {
				String segment = groups[i];
				if (segment == null || segment.length() <= 0) {
					return false;
				}

				int value = 0;
				try {
					value = Integer.parseInt(segment);
				} catch (NumberFormatException e) {
					return false;
				}
				if (value > 255) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
