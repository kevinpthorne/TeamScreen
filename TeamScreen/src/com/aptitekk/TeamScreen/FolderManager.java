package com.aptitekk.TeamScreen;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Comparator;

import javax.swing.ImageIcon;


/**
 * Manages folder and directory modifications
 *
 * @author kevint.
 *         Created Apr 7, 2013.
 */
public class FolderManager {
	
	/**
	 * Static FolderManager variable
	 */
	public static FolderManager foldermanager;
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	/**
	 * Deletes a directory
	 *
	 * @param directory
	 * @return bool
	 */
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
	/**
	 * Copies a directory
	 *
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFolder(File src, File dest) throws IOException{
		 
    	if(src.isDirectory()){
 
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
    		   //System.out.println("Directory copied from "+ src + "  to " + dest);
    		}
 
    		//list all the directory contents
    		String files[] = src.list();
 
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		}
 
    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest); 
 
    	        byte[] buffer = new byte[1024];
 
    	        int length;
    	        //copy the file content in bytes 
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }
 
    	        in.close();
    	        out.close();
    	        //System.out.println("File copied from " + src + " to " + dest);
    	}
	}
	public static boolean isWindows() {
		 
		return (OS.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (OS.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
 
	public static boolean isSolaris() {
 
		return (OS.indexOf("sunos") >= 0);
 
	}
	
	/**
	 * Use this for finding where our JAR sits <br>
	 * DO NOT ATTEMPT ANY OTHER WAY. This is for continuity purposes
	 *
	 * @return
	 */
	public static String getAbsDir()
	{
		try {
			File abs = new File(TeamScreenDaemon.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
			return abs.getAbsolutePath();
		} catch (URISyntaxException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Obtain the image from inside the jar
	 *
	 * @param path
	 * @param description
	 * @return
	 */
    public static Image createImage(String path, String description) {
        URL imageURL = TeamScreenDaemon.class.getResource(path);
         
        if (imageURL == null) {
            throw new NullPointerException("Resource not found: " + path);
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
