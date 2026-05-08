package com.aptitekk.TeamScreen.Net.jrc;

import java.awt.Frame;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.one.stone.soup.remote.control.server.ControlAdapter;
import org.one.stone.soup.screen.recorder.ScreenRecorderListener;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.JRCServerControlExtension;
import com.aptitekk.TeamScreen.Net.Broadcaster.TeamScreenBroadcastThread;
import com.aptitekk.TeamScreen.Net.jrc.Server.DynamicRemoteControlServer;
import com.aptitekk.TeamScreen.Net.jrc.Server.DynamicWindowScreenRecorder;
import com.aptitekk.TeamScreen.Net.jrc.Server.WindowControlAdapter;
import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * Java Dynamic Remote Control Server Application<br>
 * Modified from the <i>RemoteControlServerApplication</i> class
 *
 * @author kevint, Nicholas Cross
 *         Created Apr 6, 2014.
 */
public class DynamicJRCServerApplication extends Thread {
	
	private static WindowInfo window;
	private DynamicRemoteControlServer server;
	private boolean serverRunning = false;
	private static boolean threadRunning = false;
	
	private static DynamicJRCServerApplication instance;
	
	private Thread windowUpdater;
	
	private WindowControlAdapter controlAdapter;
	private DynamicWindowScreenRecorder screenRecorder;
	
	private JRCServerControlExtension sideBar;
	
	public DynamicJRCServerApplication(WindowInfo w) {
		DynamicJRCServerApplication.window = w;
		instance = this;
	}

	public Frame getRootFrame() {
		return null;
	}

	public void disconnect() {
		stopServer();
		this.serverRunning = false;
	}
	
	@Override
	public void run() {
		DynamicJRCServerApplication.threadRunning = true;
		try {
			//System.out.println("Window hooks placed");
			this.windowUpdater = new Thread() {
				public void run() {
					while(threadRunning) {
						try {
							window = TeamScreenDaemon.getWindowManager().updateWindow(window);
							if(screenRecorder != null)
								screenRecorder.updateWindow(window);
							if(controlAdapter != null)
								controlAdapter.updateWindow(window);
							//TeamScreenDaemon.getWindowManager().bringWindowToFront(window);
						} catch (com.aptitekk.TeamScreen.JNA.WindowManager.WindowNotFoundException e) {
							//System.out.println("Window hooks removed");
							e.printStackTrace();
							break;
						}
					}
					instance.interrupt();
				}
			};
			this.windowUpdater.start();
			startServer();
		} catch (Exception exception) {
			TeamScreenDaemon.logger.severe("Could not start JRC server");
			exception.printStackTrace();
		}
	}
	
	@Override
	public void interrupt() {
		this.windowUpdater.interrupt();
		disconnect();
		super.interrupt();
	}
	
	public void startServer() throws Exception
	{
		if(this.serverRunning==true)
		{
			TeamScreenDaemon.logger.warning("[Warning] The server is already running");
			return;
		}
		
		initialiseServer();
	}
	
	public void stopServer()
	{
		if(this.serverRunning==false)
		{
			TeamScreenDaemon.logger.warning("[Warning] The server is not running");
			return;
		}
		this.serverRunning=false;
		this.server.stop();
		this.windowUpdater.interrupt();
		TeamScreenDaemon.logger.info("RCD Server Stopped");
	}

	public void setRequester(String requester) {
		TeamScreenDaemon.logger.info("Connected: " + requester);
		
	}

	public ControlAdapter getControlAdapter() {
		return this.controlAdapter;
	}
	
	private ControlAdapter createControlAdapter() {
		this.controlAdapter = new WindowControlAdapter(DynamicJRCServerApplication.window);
		return this.controlAdapter;
	}

	public DynamicWindowScreenRecorder getScreenRecorder(OutputStream outputStream,
			ScreenRecorderListener listener) {
		if(this.screenRecorder == null)
			this.screenRecorder = new DynamicWindowScreenRecorder(outputStream, listener, DynamicJRCServerApplication.window);
		return this.screenRecorder;
	}
	
	private void initialiseServer() throws Exception
	{
		createControlAdapter();
		
		String address = TeamScreenBroadcastThread.getLanIP();
		List<String> slots = Arrays.asList(new String[]{"A","B","C","D","E","F"});
        Integer[] ports = new Integer[]{4455, 4456, 4457, 4458, 4459, 4460};
        int port = ports[slots.indexOf(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot"))];
		
		this.server = new DynamicRemoteControlServer(this);
		this.server.setAutoAccept(true);
		this.server.start("Remote Control Server",address,port,2,1000);
		
		TeamScreenDaemon.logger.info("RCD Server Started on "+address+":"+port);
		this.serverRunning=true;
	}
	
	public boolean isServerRunning() {
		return this.serverRunning;
	}

	public void updateWindowRect(WindowInfo info) {
		DynamicJRCServerApplication.window = info;
		this.controlAdapter.updateWindow(info);
	}

}
