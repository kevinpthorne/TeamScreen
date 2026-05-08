package com.AptiTekk.AutoUpdater;

import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;

import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AutoUpdater
{
    
    static String jarName;

    public static void main(String[] args)
    {
	if(args.length > 0)
	{
	    jarName = args[0];
	}
	else 
	{
	    jarName = "Launcher.jar";
	}
	UIManager.put("ProgressBar.background", Color.WHITE);
	UIManager.put("ProgressBar.foreground", new Color(0, 120, 255));
	UIManager.put("ProgressBar.border", new EmptyBorder(10, 0, 10, 0));
	new UpdaterFrame();
	new InstallerThread().start();
    }
    
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
    
}
