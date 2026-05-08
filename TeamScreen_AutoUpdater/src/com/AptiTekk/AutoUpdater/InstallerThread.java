package com.AptiTekk.AutoUpdater;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class InstallerThread extends Thread
{
    
    @Override
    public void run()
    {
	try
	{
	    Thread.sleep(1000);
	}
	catch(InterruptedException e)
	{
	}
	
	File updateFolder = new File(AutoUpdater.getAbsDir() + "/updates/");
	
	if(updateFolder.exists())
	{
	    int amountToUpdate = updateFolder.listFiles().length;
	    if(amountToUpdate > 0)
	    {
		File[] updates = updateFolder.listFiles();
		Arrays.sort(updates, new Comparator<File>()
		{
		    public int compare(File f1, File f2)
		    {
			return Long.valueOf(f1.lastModified()).compareTo(
				f2.lastModified());
		    }
		});
		
		for(File update : updates)
		{
		    update = unZip
			    .renameFileExtension(update.getPath(), "zip");
		    unZip.unZipUpdate(update.getPath());
		    update.delete();
		}
	    }
	    updateFolder.delete();
	}
	
	try
	{
	    new ProcessBuilder("java", "-jar", new File(AutoUpdater.getAbsDir()
		    + "/"+AutoUpdater.jarName).getAbsolutePath()).start();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	System.exit(0);
    }
    
}
