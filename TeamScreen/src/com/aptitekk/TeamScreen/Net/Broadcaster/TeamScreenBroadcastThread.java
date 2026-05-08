package com.aptitekk.TeamScreen.Net.Broadcaster;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.NetHandler;


/**
 * Broadcasts to clients saying that theres a classroom here!
 *
 * @author kevint.
 *         Created Feb 24, 2014.
 */
public class TeamScreenBroadcastThread extends Thread{
	private NetHandler parent;
    private DatagramSocket socket;
    public boolean running = true;

    public TeamScreenBroadcastThread(NetHandler master) {
        super("TeamScreenBroadcastThread");
        setDaemon(true);
        this.parent = master;
        try {
        	InetAddress address = InetAddress.getByName(getLanIP());
            this.socket = new DatagramSocket(4445, address);
            this.socket.setSoTimeout(1000);
        } catch (Exception e) {
            e.printStackTrace();
            TeamScreenDaemon.logger.severe("Host does not support datagram sockets. Shutting down...");
            this.running = false;
        }
    }



	@Override
    public void interrupt() {
        super.interrupt();
        this.running = false;
        this.socket.close();
    }

    @Override
    public void run() {
        try {
            String name = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Name");
            
            String IP = this.socket.getLocalAddress().getHostAddress();
           
            byte[] ad = (name + NetHandler.PACKET_SEPARATOR_CONSTANT + IP + "\r\n").getBytes();
            
            List<String> slots = Arrays.asList(new String[]{"A","B","C","D","E","F"});
            Integer[] ports = new Integer[]{4455, 4456, 4457, 4458, 4459, 4460};
            
            int port = ports[slots.indexOf(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot"))];
            
            DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.12"), port);
            TeamScreenDaemon.logger.info("Broadcasting session over LAN from " + this.socket.getLocalAddress());
           
            while (!isInterrupted() && this.running) {
                try {
                    this.socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) { /*Emptiness */}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLanIP() throws Exception {

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) return address.getHostAddress();
                }
            }
            throw new Exception("No usable IPv4 non-loopback address found");
        } catch (Exception e) {
            e.printStackTrace();
            TeamScreenDaemon.logger.severe("Could not automatically detect LAN IP, please set server-ip in server.properties.");
            TeamScreenDaemon.logger.severe("Using " + InetAddress.getLocalHost().getHostAddress());
            return InetAddress.getLocalHost().getHostAddress();
        }
    }
}
