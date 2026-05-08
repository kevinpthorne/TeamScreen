package com.aptitekk.TeamScreen.AutoUpdater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class URLUtils
{
    
    private static ArrayList<DLProgressListener> progressListenerList = new ArrayList<DLProgressListener>();
    
    public static File getFileFromConnection(URLConnection connection,
	    String fileLoc)
    {
	
	for(DLProgressListener listener : progressListenerList)
	{
	    listener.downloadStarted();
	}
	
	BufferedInputStream inputStream;
	try
	{
	    inputStream = new BufferedInputStream(connection.getInputStream());
	    FileOutputStream fileOutputStream = new java.io.FileOutputStream(
		    fileLoc);
	    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
		    fileOutputStream, 1024);
	    
	    byte data[] = new byte[1024];
	    
	    long total = 0;
	    int count;
	 
	    int fileSize = connection.getContentLength();
	    
	    boolean doagain = ((count = inputStream.read(data)) != -1);
	    boolean canceled = false;
	    
	    if(!doagain)
	    {
		inputStream.close();
		bufferedOutputStream.close();
		
		for(DLProgressListener listener : progressListenerList)
		{
		    listener.downloadFailed("No data was received.");
		}
		
		return null;
	    }
	    
	    while(doagain && !canceled)
	    {
		total += count;
		
		for(DLProgressListener listener : progressListenerList)
		{
		    listener.progressChanged((int)((total*100)/fileSize));
		    canceled = canceled | listener.shouldCancelDownload();
		}
		
		bufferedOutputStream.write(data, 0, count);
		doagain = ((count = inputStream.read(data)) != -1);
	    }
	    
	    inputStream.close();
	    bufferedOutputStream.close();
	    
	    if(!canceled)
	    {
		for(DLProgressListener listener : progressListenerList)
		{
		    listener.downloadCompleted();
		}
		
		return new File(fileLoc);
	    }
	    return null;
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    for(DLProgressListener listener : progressListenerList)
	    {
		listener.downloadFailed("Could not write update to file.");
	    }
	    return null;
	}
    }
    
    public static String getStringFromConnection(URLConnection connection)
    {
	InputStream inputStream = null;
	DataInputStream dataInputStream = null;
	String lineData, completeData = "";
	
	try
	{
	    inputStream = connection.getInputStream();
	    dataInputStream = new DataInputStream(new BufferedInputStream(
		    inputStream));
	    BufferedReader bufferedReader = new BufferedReader(
		    new InputStreamReader(dataInputStream));
	    
	    while((lineData = bufferedReader.readLine()) != null)
	    {
		if(lineData.startsWith("ERROR"))
		{
		    inputStream.close();
		    dataInputStream.close();
		    bufferedReader.close();
		    return lineData;
		}
		completeData += lineData;
	    }
	    
	    inputStream.close();
	    bufferedReader.close();
	    dataInputStream.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	
	return completeData;
    }
    
    public static void addProgressListener(DLProgressListener listener)
    {
	progressListenerList.add(listener);
    }
    
    public static ArrayList<DLProgressListener> getProgressListenerList()
    {
	return progressListenerList;
    }
    
}
