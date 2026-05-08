package com.AptiTekk.AutoUpdater;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class unZip
{
    
    public static final void unZipUpdate(String s)
    {
	try
	{
	    ZipFile zipFile = new ZipFile(s);
	    zipFile.extractAll(new File(AutoUpdater.getAbsDir())
		    .getAbsolutePath());
	    unZip.deleteDirectory(new File(s));
	}
	catch(ZipException e)
	{
	    e.printStackTrace();
	    return;
	}
    }
    
    public static boolean deleteDirectory(File directory)
    {
	if(directory.exists())
	{
	    File[] files = directory.listFiles();
	    if(null != files)
	    {
		for(int i = 0; i < files.length; i++)
		{
		    if(files[i].isDirectory())
		    {
			deleteDirectory(files[i]);
		    }
		    else
		    {
			files[i].delete();
		    }
		}
	    }
	}
	return(directory.delete());
    }
    
    public static File renameFileExtension(String source, String newExtension)
    {
	String target;
	String currentExtension = getFileExtension(source);
	
	if(currentExtension.equals(""))
	{
	    target = source + "." + newExtension;
	}
	else
	{
	    target = source.replaceFirst(Pattern.quote("." + currentExtension)
		    + "$", Matcher.quoteReplacement("." + newExtension));
	    
	}
	new File(source).renameTo(new File(target));
	return new File(target);
    }
    
    public static String getFileExtension(String f)
    {
	String ext = "";
	int i = f.lastIndexOf('.');
	if(i > 0 && i < f.length() - 1)
	{
	    ext = f.substring(i + 1);
	}
	return ext;
    }
    
}
