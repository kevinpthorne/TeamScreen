package com.aptitekk.TeamScreen.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.aptitekk.TeamScreen.FolderManager;
import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.melloware.jintellitype.JIntellitype;

/**
 * Main class to handle properties' files
 *
 * @author Kevin Thorne.
 *         Created Apr 3, 2014.
 */
public class PropertiesHandler {
	
	public static String properties_filepath = FolderManager.getAbsDir()+"/TeamScreen.properties";
	
	private static final String[] properties_fields = {"Name", "Slot", "KeyStroke", "KeyStrokeEnd", "Log", "AutoUpdate", "NotificationLevel"};
	
	private static String prop_comments = "This is the default properties file.\n"+
									"DO NOT EDIT (Unless you are instructed to do so.)\n"+
									"Copyright Kevin Thorne 2014\n"+
									"-------------GRID------------\n"+
									"|            (P)            |\n"+
									"|                           |\n"+
									"| (TL)       (T)       (TR) |\n"+
									"|       \\     |     /       |\n"+
									"| (L)-------(You)-------(R) |\n"+
									"|        /    |    \\        |\n"+
									"| (BL)       (B)       (BR) |\n"+
									"_____________________________\n";
	
	private Properties prop;
	
	public PropertiesHandler() throws FileNotFoundException, IOException {
		if(!new File(properties_filepath).exists()) {
			saveDefaultConfig();
		}
		this.prop = new Properties();
		this.prop.load(new FileInputStream(new File(properties_filepath)));
		for(String field : properties_fields) {
			if(!this.prop.containsKey(field)) {
				saveDefaultConfig();
				TeamScreenDaemon.logger.severe("Properties file corrupt, overwriting with default config.");
				this.prop.load(new FileInputStream(new File(properties_filepath)));
			}
		}
	}
	
	public static Properties loadProperties(String file) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File(file)));
		return prop;
	}
	
	public static Properties loadProperties(File file) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		return prop;
	}
	
	public static void saveProperties(Properties prop, File file) throws FileNotFoundException, IOException {
		prop.store(new FileOutputStream(file), null);
	}
	
	/**
	 * Loads a fresh config straight from the file
	 *
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties loadConfig() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File(properties_filepath)));
		return prop;
	}
	
	/**
	 * Loads the config already in memory
	 *
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Properties getConfig() throws FileNotFoundException, IOException {
		return prop;
	}
	
	/**
	 * Default values
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveDefaultConfig() throws FileNotFoundException, IOException {
		prop = new Properties();
		prop.setProperty("Name", java.net.InetAddress.getLocalHost().getHostName());
		prop.setProperty("Slot", "A");
		prop.setProperty("KeyStroke", String.valueOf(JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT));
		prop.setProperty("KeyStrokeEnd","1");
		prop.setProperty("Log", "false");
		prop.setProperty("NotificationLevel", "warning");
		prop.setProperty("AutoUpdate", "true");
		prop.setProperty("LNeighbor", "");
		prop.setProperty("RNeighbor", "");
		prop.setProperty("TNeighbor", "");
		prop.setProperty("BNeighbor", "");
		prop.setProperty("TLNeighbor", "");
		prop.setProperty("TRNeighbor", "");
		prop.setProperty("PNeighbor", "");
		prop.store(new FileOutputStream(properties_filepath), prop_comments);
		this.prop = prop;
	}
	
	/**
	 * Saves loaded configuration
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveConfig() throws FileNotFoundException, IOException {
		prop.store(new FileOutputStream(properties_filepath), prop_comments);
	}
}
