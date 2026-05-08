package com.aptitekk.TeamScreen.Net.jrc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.one.stone.soup.io.Connection;
import org.one.stone.soup.net.SocketConnection;
import org.one.stone.soup.remote.control.client.RemoteClientController;
import org.one.stone.soup.remote.control.hub.HubHelper;
import org.one.stone.soup.screen.recorder.ScreenPlayer;
import org.one.stone.soup.util.TimeWatch;
import org.one.stone.soup.xml.XmlElement;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Net.jrc.Client.ClientWindowFrame;
import com.aptitekk.TeamScreen.Net.jrc.Client.DynamicScreenPlayer;

/**
 * The class that is called when a window is pushed to it<br>
 * Modified from the <i>XappJavaRemoteControlClient</i> class
 *
 * @author kevint, Nicholas Cross
 *         Created Apr 6, 2014.
 */
public class ClientApplication extends Thread implements RemoteClientController,MouseListener,MouseMotionListener,ActionListener{
	
	private String currentFile=null;
	
	private boolean clientRunning = false;
	
	private ClientWindowFrame client;
	private String windowTitle;
	
	private Connection mainConnection;
	private Connection controlConnection;
	private DynamicScreenPlayer screenPlayer;
	private String serverKey;
	private TimeWatch timeoutTimer;
	
	private long frameMonitorTime = 0;
	private int frameMonitorCount = 0;
	
	private String address;
	private int port;
	
	public ClientApplication(String address, int port, String windowTitle) {
		this.address = address;
		this.port = port;
		this.windowTitle = windowTitle;
		
		this.timeoutTimer = new TimeWatch(30000);
		this.timeoutTimer.addActionListener(this);
		this.timeoutTimer.setActionCommand("timedOut");
	}
	
	@Override
	public void run() {
		connect();
		while(this.clientRunning) {
			;
		}
	}
	@Override
	public void interrupt() {
		disconnect();
		super.interrupt();
	}

	@Override
	public void keyTyped(KeyEvent e) {;}
	@Override
	public void mouseClicked(MouseEvent e) {;}
	@Override
	public void mouseEntered(MouseEvent e) {;}
	@Override
	public void mouseExited(MouseEvent e) {;}
	
	public void connect()
	{
		if(this.clientRunning==true)
		{
			TeamScreenDaemon.logger.warning("Client already connected.");
			return;
		}
		
		this.clientRunning = true;
		
		String hubAlias = this.address;
		
		try{
			/*if(useTunnel==true)
			{
				mainConnection = new HttpTunnelConnection(tunnelHost,address,port);
			}
			else
			{*/
				this.mainConnection = new SocketConnection( new Socket(this.address,this.port),this.address+":"+this.port );
			//}
		
			this.client = new ClientWindowFrame(this, hubAlias + " - " + this.windowTitle);
			showClient();
			
			System.out.println("Logged in. Sending alias "+hubAlias+" to server.");
			HubHelper.writeLine(hubAlias,this.mainConnection.getOutputStream());
			System.out.println("Done.");
			
			InputStream iStream = this.mainConnection.getInputStream();
	
			System.out.println("Reading key.");
			String targetKey = HubHelper.readLine(iStream);
			if(targetKey.length()==0)
			{
				TeamScreenDaemon.logger.warning("Connection refused");
				disconnect();
				return;
			}
			
			//System.out.println("Key:"+targetKey);
			
			this.screenPlayer = new DynamicScreenPlayer(iStream,this.client);
			
			this.clientRunning = true;
						
			/*if(useTunnel==true)
			{
				controlConnection = new HttpTunnelConnection(tunnelHost,address,port);
			}
			else
			{*/
				this.controlConnection = new SocketConnection( new Socket(this.address,this.port),this.address+":"+this.port );
			//}
			
	
			//System.out.println("Send Key:"+targetKey);			
			HubHelper.writeLine(targetKey,this.controlConnection.getOutputStream());							
			System.out.println("Key Sent.");
			
			System.out.println("Sending Full Control request.");
			HubHelper.writeLine("fullControl",this.controlConnection.getOutputStream());
			System.out.println("Full Control request sent.");

			this.client.picLabel.addMouseListener(this);
			this.client.picLabel.addMouseMotionListener(this);
			this.client.picLabel.addKeyListener(this);
		
			System.out.println("Screen Player Started");
			
			this.screenPlayer.play();
			
			this.timeoutTimer.startTimer();
			
			//client.doLayout();
			//panel.doLayout();
			//((XappSwingScrollPane)XappRootApplication.getComponent("view")).doLayout();			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			disconnect();
		}
		
	}
	
	/**
	 * Adds the client into the JFrame
	 *
	 */
	public void showClient()
	{
		this.client.setVisible(true);
	}
	public void doLayout()
	{
		this.client.doLayout();
	}
	
	public void disconnect()
	{
		if(this.clientRunning==false)
		{
			System.out.println("Client not connected.");
			return;
		}
		
		this.timeoutTimer.stopTimer();
		
		try{
			this.client.getView().removeMouseListener(this);
			this.client.getView().removeMouseMotionListener(this);
			this.client.getView().removeKeyListener(this);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		try{ this.mainConnection.close(); }catch(Exception e){}
		try{ this.controlConnection.close(); }catch(Exception e){}
		
		this.screenPlayer=null;
		
		try {
			this.client.dispose();
			this.client = null;
		}catch(Exception e){}
		
		try{
			this.clientRunning=false;
		}catch(Exception e){}
		System.out.println("Disconnected from Remote Machine");
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{	
		XmlElement packet = new XmlElement("keyPressed");
		packet.addAttribute("code",""+e.getKeyCode());
		sendCommand(packet);
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		XmlElement packet = new XmlElement("keyReleased");
		packet.addAttribute("code",""+e.getKeyCode());
		sendCommand(packet);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{	
		XmlElement packet = new XmlElement("mouseMoved");
		packet.addAttribute("x",""+e.getX());
		packet.addAttribute("y",""+e.getY());
		sendCommand(packet);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		XmlElement packet = new XmlElement("mousePressed");
		packet.addAttribute("code",""+e.getModifiers());
		sendCommand(packet);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		XmlElement packet = new XmlElement("mouseReleased");
		packet.addAttribute("code",""+e.getModifiers());
		sendCommand(packet);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		;
		
	}

	@Override
	public void requestNextFrame()
	{
		this.frameMonitorCount++;
		
		/*if(System.currentTimeMillis()-frameMonitorTime>TimeConstants.SECOND_MILLIS*10)
		{
			XappProgressBar progress = ((XappProgressBar)XappRootApplication.getComponent("rates.frameRate"));
			progress.setData(""+(frameMonitorCount*3));
			progress.setTitle("Frames / Minute:"+(frameMonitorCount*6));
			
			frameMonitorTime=System.currentTimeMillis();			
			frameMonitorCount=0;
		}*/
		
		XmlElement packet = new XmlElement("nextFrame");
		sendCommand(packet);
		
		this.timeoutTimer.resetTimer();
	}

	private synchronized void sendCommand(XmlElement packet)
	{
		if(this.controlConnection==null)
		{
			return;
		}
		try{
			this.controlConnection.getOutputStream().write(packet.toXmlNoBeautify().getBytes());
			this.controlConnection.getOutputStream().flush();
			
			//System.out.println("Sent: "+packet.toXmlNoBeautify());
		}
		catch(Exception e){
			System.out.println("Failed to send "+packet.toXmlNoBeautify());
			disconnect();
		}
	}

}
