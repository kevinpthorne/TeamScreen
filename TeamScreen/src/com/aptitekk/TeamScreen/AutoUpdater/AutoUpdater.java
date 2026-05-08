package com.aptitekk.TeamScreen.AutoUpdater;

import java.awt.Image;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

/**
 * AutoUpdater portion of TeamScreen
 * 
 * @author Mitch Talmadge
 *
 * Used with permission
 */
public class AutoUpdater
{
    /** 
     * Static variables that connection to the server requires.
     */
    public static final String applicationName = "TeamScreen";
    /** 
     * Static variables that connection to the server requires.
     */
    public static final String updaterURLPrefix = "https://AptiTekk.com/AutoUpdater/V1/";
    /** 
     * Static variables that connection to the server requires.
     */
    public static final String updaterChangeLogSuffix = "ChangeLogFactory.php";
    /**
     * Static variables that connection to the server requires.
     */
    public static final String updaterFileTransferSuffix = "FileTransfer.php";
    /**
     * Static variables that connection to the server requires.
     */
    public static final String updaterVersionFinderSuffix = "VersionFinder.php";
    /**
     * Static variables that connection to the server requires.
     */
    public static final String updaterHandshakeSuffix = "Handshake.php";
    /** 
     * Static variables that connection to the server requires.
     */
    public static final String updaterAuthenticatorSuffix = "Authenticator.php";
    
    private static int currentVersionID;
    private static String currentVersionName = "0.0_0.0";
    private AutoUpdaterDownloadThread autoUpdaterDownloadThread;
    private static final Object synchronizedObject = new Object();
    static Handshake handShake;
    private boolean automaticallyUpdate;
    public static Image windowIcon;
    private static ArrayList<UpdaterListener> updateListeners = new ArrayList<UpdaterListener>();
    
    /**
     * Creates a new AutoUpdater. 
     * @param automaticallyUpdate - Display a ChangeLog (false), or skip ChangeLog and download immediately (true)
     * @param windowIcon - The icon file to be displayed in Task Bar.
     */
    public AutoUpdater(boolean automaticallyUpdate, Image windowIcon)
    {
	deleteUpdatesFolder();
	this.automaticallyUpdate = automaticallyUpdate;
	AutoUpdater.windowIcon = windowIcon;
	System.setProperty("javax.net.ssl.trustStore",getAbsDir()+"/secure/cacerts");
	System.setProperty("javax.net.ssl.trustStorePassword","CpAhLLp94j");
	this.loadProperties();
    }
    
    private void loadProperties()
    {
	File propFile = new File(getAbsDir()+"/updates.properties");
	try
	{
	    EncryptedProperties prop = new EncryptedProperties("<*d~Pq(E[F8u~m");
	    if(!propFile.exists())
	    {
		FileOutputStream out = new FileOutputStream(propFile);
		prop.setProperty("currentVersionID", "0");
		prop.setProperty("currentVersionName", "0.0_0.0");
		currentVersionID = 0;
		currentVersionName = "0.0_0.0";
		prop.store(out, "Auto Updater Properties File -- DO NOT EDIT.");
		out.close();
	    }
	    else 
	    {
		FileInputStream in = new FileInputStream(propFile);
		prop.load(in);
		currentVersionID = Integer.parseInt(prop.getProperty("currentVersionID"));
		currentVersionName = prop.getProperty("currentVersionName");
		in.close();
	    }
	}
	catch(FileNotFoundException e)
	{
	    e.printStackTrace();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    
    /**
     * Checks for updates on the server
     */
    public void checkForUpdates()
    {
	logMessage("Checking for Updates...", LevelEnum.INFO);
	
	String POSTData = "currentVersionID=" + currentVersionID + "&modType="
		+ AutoUpdater.applicationName;
	
	String result = sendPOSTGetString(updaterURLPrefix
		+ updaterVersionFinderSuffix, POSTData);
	if(result == null || result.startsWith("ERROR"))
	{
	    System.out.println("Error message received: " + result);
	    return;
	}
	else if(result.startsWith("SUCCESS"))
	{
	    int amountToUpdate = Integer.parseInt(result.split(":")[1]);
	    System.out.println("Amount to Update: " + amountToUpdate);
	    String changeLog = sendPOSTGetString(
		    updaterURLPrefix
		    + updaterChangeLogSuffix, POSTData);
	    if(automaticallyUpdate || this.askUserToUpdate(changeLog, amountToUpdate))
	    {
		System.out.println("AutoUpdater granted permission to update. Generating Handshake...");
		if(generateHandshake())
		{
		    System.out.println("Handshake generated. Downloading...");
		    this.downloadUpdates(amountToUpdate);
		    synchronized(synchronizedObject)
		    {
			try
			{
			    synchronizedObject.wait();
			}
			catch(InterruptedException e)
			{
			}
		    }
		}
		else
		{
		    return;
		}
	    }
	    else
	    {
		System.out.println("User said No. Continuing...");
	    }
	}
	else
	{
	    logMessage("Could not connect to AutoUpdater. Aborting.\n    (Result was: "
		    + result + ")", LevelEnum.SEVERE);
	}
    }
    
    static boolean generateHandshake()
    {
	String POSTData = "currentVersionID=" + currentVersionID + "&modType="
		+ AutoUpdater.applicationName;
	String data = sendPOSTGetString(AutoUpdater.updaterURLPrefix
		+ AutoUpdater.updaterHandshakeSuffix, POSTData);
	if(data != null && !data.isEmpty())
	{
	    if(data.contains("SUCCESS"))
	    {
		String[] split = data.split(":");
		if(split.length == 3)
		{
		    handShake = new Handshake(split[1], split[2]);
		    return true;
		}
	    }
	    else
	    {
		logMessage("Cannot generate Handshake.\n    (Result was: "
			+ data + ")", LevelEnum.SEVERE);
	    }
	}
	return false;
    }
    
    private boolean askUserToUpdate(String changeLog, int amountToUpdate)
    {
	System.out.println("Asking user to Update...");
	return new ChangeLogDialog(changeLog, amountToUpdate).askUserToUpdate();
    }
    
    private void downloadUpdates(int amountToUpdate)
    {
	new File("updates/").mkdir();
	logMessage("Downloading Updates...", LevelEnum.INFO);
	
	this.autoUpdaterDownloadThread = new AutoUpdaterDownloadThread(
		currentVersionID, amountToUpdate, handShake, automaticallyUpdate);
	this.autoUpdaterDownloadThread.start();
    }
    
    static String sendPOSTGetString(String URLString, String POSTData)
    {
	URLConnection connection;
	URL url;
	try
	{
	    url = new URL(URLString);
	    
	    connection = url.openConnection();
	    connection.setUseCaches(false);
	    connection.setConnectTimeout(5000);
	    connection.setDoOutput(true);
	    OutputStreamWriter outputStream = new OutputStreamWriter(
		    connection.getOutputStream());
	    
	    outputStream.write(POSTData);
	    outputStream.flush();
	    
	    return URLUtils.getStringFromConnection(connection);
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	return "ERROR:noConnection";
    }
    
    /**
     * 
     */
    public static void deleteUpdatesFolder()
    {
	File updatesFolder = new File(getAbsDir()+"/updates");
	if(updatesFolder.exists())
	{
	    try
	    {
		FileUtils.deleteDirectory(updatesFolder);
	    }
	    catch(IOException e)
	    {
		e.printStackTrace();
	    }
	}
	File autoUpdater = new File(getAbsDir() + "/AutoUpdater.jar");
	if(autoUpdater.exists())
	    autoUpdater.delete();
    }
    
    /**
     * @return Current version
     */
    public String getCurrentVersionName()
    {
	return currentVersionName;
    }
    
    /**
     * @return Location of the jar
     */
    public static String getAbsDir()
    {
	try
	{
	    File abs = new File(AutoUpdater.class.getProtectionDomain()
		    .getCodeSource().getLocation().toURI().getPath())
	    .getParentFile().getAbsoluteFile();
	    return abs.getAbsolutePath();
	}
	catch(URISyntaxException e)
	{
	    return null;
	}
    }
    
    /**
     * @param path
     * @return Internal URL for files inside the jar
     */
    public static URL getResourceByPath(String path)
    {
	return AutoUpdater.class.getResource(path);
    }
    
    /**
     * Adds an UpdaterListener (For shutting down, etc)
     * @param listener - The UpdaterListener.
     */
    public void addUpdaterListener(UpdaterListener listener)
    {
	if(listener != null)
	{
	    if(listener instanceof UpdaterListener)
	    {
		updateListeners.add(listener);
	    }
	}
    }
    
    static void haltProgram()
    {
	for(UpdaterListener listener : updateListeners)
	{
	    listener.haltProgram();
	}
    }
    
    static void logMessage(String message, LevelEnum level)
    {
	for(UpdaterListener listener : updateListeners)
	{
	    listener.logMessage(message, level);
	}
    }
    
    static void notifyObject()
    {
	synchronized(synchronizedObject)
	{
	    synchronizedObject.notify();
	}
    }
    
}
