package com.aptitekk.TeamScreen.AutoUpdater;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class AutoUpdaterDownloadThread extends Thread
{
    
    private int currentVersionID;
    private int amountToUpdate;
    private Handshake handShake;
    private File autoUpdater;
    private boolean automaticallyUpdate;
    
    public AutoUpdaterDownloadThread(int currentVersionID, int amountToUpdate,
	    Handshake handShake, boolean automaticallyUpdate)
    {
	this.currentVersionID = currentVersionID;
	this.amountToUpdate = amountToUpdate;
	this.handShake = handShake;
	this.automaticallyUpdate = automaticallyUpdate;
    }
    
    @Override
    public void run()
    {
	if(!automaticallyUpdate)
	    URLUtils.addProgressListener(new DownloadDialog(amountToUpdate+1));
	try
	{
	    Thread.sleep(500);
	}
	catch(InterruptedException e)
	{
	}
	if(downloadUpdate(-1))
	{
	    for(int i = 1; i <= amountToUpdate; i++)
	    {
		if(!downloadUpdate(currentVersionID + i))
		    break;
	    }
	}
    }
    
    private boolean downloadUpdate(int versionID)
    {
	String POSTData = "versionID=" + versionID + "&modType="
		+ AutoUpdater.applicationName + "&handShake="
		+ this.handShake.getHandShakeResult();
	
	URLConnection connection = this.sendPOSTGetConnection(
		AutoUpdater.updaterURLPrefix
		+ AutoUpdater.updaterFileTransferSuffix, POSTData);
	
	if(connection != null)
	{
	    String contentDisposition = connection.getHeaderField("Content-Disposition");
	    if(contentDisposition == null)
	    {
		String result = URLUtils.getStringFromConnection(connection);
		for(DLProgressListener listener : URLUtils.getProgressListenerList())
		{
		    listener.downloadFailed("Server-Sided Error.");
		}
		AutoUpdater.logMessage("Could not download Version ID " + versionID
			+ ".\n    (Result was: " + result + ")", LevelEnum.SEVERE);
		if(automaticallyUpdate)
		    AutoUpdater.notifyObject();
		return false;
	    }
	    String filename = contentDisposition.substring(contentDisposition.indexOf("\"")+1, contentDisposition.lastIndexOf("\""));
	    
	    File update = URLUtils.getFileFromConnection(connection,
		    AutoUpdater.getAbsDir() + (versionID == -1 ? "/" : "/updates/") + filename);
	    if(update == null)
		return false;
	    return true;
	}
	return false;
    }
    
    private URLConnection sendPOSTGetConnection(String URLString,
	    String POSTData)
    {
	URLConnection connection;
	try
	{
	    URL url = new URL(URLString);
	    connection = url.openConnection();
	    connection.setConnectTimeout(5000);
	    connection.setDoOutput(true);
	    connection.setDoInput(true);
	    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
	    
	    OutputStreamWriter outputStream = new OutputStreamWriter(
		    connection.getOutputStream());
	    
	    outputStream.write(POSTData);
	    outputStream.flush();
	    
	    return connection;
	    
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
    
}
