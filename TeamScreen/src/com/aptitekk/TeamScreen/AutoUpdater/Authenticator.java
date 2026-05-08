package com.aptitekk.TeamScreen.AutoUpdater;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class Authenticator
{
    private static final Object synchronizedObject = new Object();
    private Handshake handShake;
    public static Image windowIcon;
    private String userName;
    private String passWord;
    
    public Authenticator(Image windowIcon)
    {
	Authenticator.windowIcon = windowIcon;
	System.setProperty("javax.net.ssl.trustStore",AutoUpdater.getAbsDir()+"/secure/cacerts");
	System.setProperty("javax.net.ssl.trustStorePassword","CpAhLLp94j");
	this.loadProperties();
    }
    
    private void loadProperties()
    {
	File propFile = new File(AutoUpdater.getAbsDir()+"/auth.properties");
	try
	{
	    EncryptedProperties prop = new EncryptedProperties("&rA@l_43F^$!cD");
	    if(!propFile.exists())
	    {
		FileOutputStream out = new FileOutputStream(propFile);
		prop.setProperty("userName", "");
		prop.setProperty("passWord", "");
		this.userName = "";
		this.passWord = "";
		prop.store(out, "Authentication Properties File -- DO NOT EDIT.");
		out.close();
	    }
	    else 
	    {
		FileInputStream in = new FileInputStream(propFile);
		prop.load(in);
		this.userName = prop.getProperty("userName");
		this.passWord = prop.getProperty("passWord");
		in.close();
	    }
	    this.userName = "bmineer";
	    this.passWord = "admin10";
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
    
    public boolean doAuthentication()
    {
	System.out.println("Authenticating...");
	if(userName.isEmpty() || passWord.isEmpty())
	{
	    System.out.println("Empty credentials. Asking for user input.");
	    return askForAuth();
	}
	else 
	{
	    System.out.println("Checking credentials with Server.");
	    String result = checkAuthWithServer();
	    if(result.equals("SUCCESS"))
	    {
		System.out.println("Authentication was Successful.");
		return true;
	    }
	    else 
	    {
		System.out.println("Authentication Failed. Asking for user input.\nError was: "+result);
		return askForAuth();
	    }
	}
    }
    
    private boolean askForAuth()
    {
	
	return false;
    }
    
    private String checkAuthWithServer()
    {
	if(!AutoUpdater.generateHandshake())
	    return "ERROR:handShake";
	
	String POSTData = "userName=" + userName + "&passWord="
		+ passWord + "&handShake=" + AutoUpdater.handShake.getHandShakeResult();
	
	String result = AutoUpdater.sendPOSTGetString(AutoUpdater.updaterURLPrefix
		+ AutoUpdater.updaterAuthenticatorSuffix, POSTData);
	
	return result;
    }
}
