package com.aptitekk.TeamScreen;

import java.io.File;
import java.io.FileInputStream;
import com.aptitekk.TeamScreen.AutoUpdater.EncryptedProperties;

/**
 * @author kevint
 *
 */
public class LicensingHandler {
	
	private static String properties_filepath = FolderManager.getAbsDir()+"/licensing";
	
	private static final String[] properties_fields = {"a"};
	
	private static String prop_comments = "DO NOT MODIFY THIS!\n"+
											"MODIFCATION WILL HAVE THIS FILE DELETED AND YOUR LICENSE KEY INVALIDATED";
	
	
	EncryptedProperties properties;
	
	/**
	 * Handles the licensing of TeamScreen
	 */
	public LicensingHandler() {
		if(!new File(properties_filepath).exists()) {
			//new license file, register with server with the new key
			if(register() == null) {
				TeamScreenDaemon.logger.severe("License key could not be registered, quitting.");
				System.exit(1);
			}
		}
		try {
			this.properties = new EncryptedProperties("Fa&U=ruheWuPh5p");
			this.properties.load(new FileInputStream(new File(properties_filepath)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		for(String field : properties_fields) {
			if(!this.properties.containsKey(field)) {
				//malformed file!
				TeamScreenDaemon.logger.severe("License file corrupt! Deleting then shutting down");
				new File(properties_filepath).delete();
				System.exit(1);
			}
		}
		
		if(verify() == null) {
			TeamScreenDaemon.logger.severe("License key could not be verified, quitting.");
			System.exit(1);
		}
	}
	
	private String register() {
		return null;
	}
	private String verify() {
		return null;
	}
}
