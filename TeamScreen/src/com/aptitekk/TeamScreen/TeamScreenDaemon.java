package com.aptitekk.TeamScreen;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.aptitekk.TeamScreen.AutoUpdater.Authenticator;
import com.aptitekk.TeamScreen.AutoUpdater.AutoUpdater;
import com.aptitekk.TeamScreen.AutoUpdater.LevelEnum;
import com.aptitekk.TeamScreen.AutoUpdater.UpdaterListener;
import com.aptitekk.TeamScreen.Frames.MainFrame;
import com.aptitekk.TeamScreen.Frames.PushSelect;
import com.aptitekk.TeamScreen.Frames.SettingsFrame;
import com.aptitekk.TeamScreen.Frames.WindowSelect;
import com.aptitekk.TeamScreen.JNA.WindowManager;
import com.aptitekk.TeamScreen.JNA.WindowManagers.*;
import com.aptitekk.TeamScreen.Net.NetHandler;
import com.aptitekk.TeamScreen.Properties.PropertiesHandler;
import com.aptitekk.TeamScreen.Util.Interceptor;
import com.aptitekk.TeamScreen.Util.LogFormat;
import com.aptitekk.TeamScreen.Util.NotImplementedException;
import com.aptitekk.TeamScreen.Util.WindowInfo;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeException;

/**
 * Main tray for TeamScreen, stays dormant until user action
 *
 * @author Kevin Thorne. Created Apr 3, 2014.
 */
public class TeamScreenDaemon implements KeyListener, HotkeyListener,
		UpdaterListener {

	public static TrayIcon trayIcon;

	public static final Logger logger = Logger.getLogger("TeamScreen");

	private final Set<Character> keysPressed = new HashSet<Character>();

	static PropertiesHandler properties;
	public static String runningversion;
	public static final SystemTray tray = SystemTray.getSystemTray();
	public NetHandler netHandler;

	static MainFrame frame;
	static SettingsFrame sframe;
	TeamScreenDaemon instance = null;
	static boolean enabled = true;
	static WindowSelect wSelect;
	static PushSelect pSelect;
	public static WindowInfo windowPushed;

	static WindowManager wManager;

	static boolean verbose = false;

	/**
	 * Start up here
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		UIManager.put("ProgressBar.background", Color.WHITE);
		UIManager.put("ProgressBar.foreground", new Color(0, 120, 255));
		UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		UIManager.put("ProgressBar.border", new EmptyBorder(5, 0, 5, 0));
		UIManager
				.put("ProgressBar.font", new Font("SansSerif", Font.PLAIN, 12));

		if (args.length >= 2) {
			for (int i = 0; i > args.length; i++) {
				if (args[i].equals("-v") || args[i].equals("--verbose")) {
					verbose = true;
				}
			}
		}

		// new LicensingHandler();

		new TeamScreenDaemon();
	}

	private TeamScreenDaemon() {
		instance = this;
		logger.info("Starting TeamScreen...");

		try {

			properties = new PropertiesHandler();

			logger.setUseParentHandlers(false);
			LogFormat formatter = new LogFormat();
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(formatter);
			logger.addHandler(consoleHandler);
			if (verbose) {
				Interceptor inter = new Interceptor(System.out, logger,
						Interceptor.OUT);
				Interceptor interr = new Interceptor(System.err, logger,
						Interceptor.ERR);
				System.setErr(interr);
				System.setOut(inter);
			}
			if (Boolean.valueOf(properties.getConfig().getProperty("Log"))) {
				FileHandler fh;
				try {
					logger.info("Opened log file at: "
							+ FolderManager.getAbsDir() + "/TeamScreen.log");
					fh = new FileHandler(FolderManager.getAbsDir()
							+ "/TeamScreen.log");
					fh.setFormatter(formatter);
					logger.addHandler(fh);
				} catch (Exception exception) {
					logger.severe("Could not create log file, not storing logs. Caused by: "
							+ exception.getCause());
				}
			}
			// register window managers and hotkey managers

			if (FolderManager.isWindows()) {
				wManager = new WindowsNative();
				logger.info("OS: Windows");

				// Init hotkeys
				JIntellitype.getInstance();
				// JIntellitype.getInstance().registerHotKey(1,
				// Integer.valueOf(properties.getConfig().getProperty("KeyStroke")).intValue()
				// ,
				// Integer.valueOf(properties.getConfig().getProperty("KeyStrokeEnd")).intValue());
				JIntellitype.getInstance().registerHotKey(1,
						JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
						(int) '1');
				JIntellitype.getInstance().addHotKeyListener(this);
			} else if (FolderManager.isMac()) {
				logger.info("OS: Mac OS");
				throw new NotImplementedException("Mac is not supported yet.");
			} else if (FolderManager.isUnix()) {
				wManager = new X11Native();
				logger.info("OS: Linux running X11");
			} else {
				logger.info("No supported OS detected");
				throw new UnsupportedOperationException(
						"You aren't running Unix, Mac, or Windows.");
			}

			createTray();
			addTray();

			displayPopup(trayIcon, "Checking For Updates...",
					"TeamScreen is now checking for updates...",
					TrayIcon.MessageType.INFO);

			/*
			 * if(!new
			 * Authenticator(FolderManager.createImage("/images/TeamScreen-64px.png"
			 * , "tray icon")).doAuthentication()) { System.exit(0); return; }
			 */

			// AutoUpdater updater = new
			// AutoUpdater(Boolean.valueOf(properties.getConfig().getProperty("AutoUpdate")),
			// FolderManager.createImage("/images/TeamScreen-64px.png",
			// "tray icon"));
			// updater.addUpdaterListener(this);
			// updater.checkForUpdates();

			this.netHandler = new NetHandler(this);

			displayPopup(trayIcon, "Ready", "Initialization complete.",
					TrayIcon.MessageType.INFO);

			// openMainWindow();

			logger.info("Started TeamScreen successfully!");
		} catch (JIntellitypeException e) {
			if (FolderManager.isWindows())
				JOptionPane.showMessageDialog(null,
						"TeamScreen is already running!", "Redundant Process",
						JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane
						.showMessageDialog(
								null,
								"TeamScreen was confused on whether you were running Windows or not...",
								"Well, this is embarrassing",
								JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (NotImplementedException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"NotImplementedException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			if (FolderManager.isWindows())
				JIntellitype.getInstance().cleanUp();
			System.exit(1);
		} catch (UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"UnsupportedOperationException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			if (FolderManager.isWindows())
				JIntellitype.getInstance().cleanUp();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					null,
					"TeamScreen has managed to cause an error:\n"
							+ e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			if (FolderManager.isWindows())
				JIntellitype.getInstance().cleanUp();
			System.exit(1);
		}

		// Thread.currentThread().setDaemon(true);
	}

	public static void displayPopup(TrayIcon tray, String title, String text,
			TrayIcon.MessageType type) {
		try {
			if (properties.getConfig().containsKey("NotificationLevel")) {
				TrayIcon.MessageType notificationLevel = TrayIcon.MessageType
						.valueOf(properties.getConfig()
								.getProperty("NotificationLevel").toUpperCase());
				switch (notificationLevel) {
				case WARNING:
					if (type.equals(TrayIcon.MessageType.WARNING)
							|| type.equals(TrayIcon.MessageType.ERROR))
						tray.displayMessage(title, text, type);
					break;
				case INFO:
					tray.displayMessage(title, text, type);
					break;
				case ERROR:
					if (type.equals(TrayIcon.MessageType.ERROR))
						tray.displayMessage(title, text, type);
					break;
				default:
					tray.displayMessage(title, text, type);
					break;
				}
			} else {
				System.err
						.println("No NotificationLevel found, falling back to default");
				properties.getConfig().setProperty("NotificationLevel", "info");
				properties.saveConfig();
				tray.displayMessage(title, text, type);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void enable() {
		displayPopup(trayIcon, "TeamScreen now enabled!", "Services started.",
				TrayIcon.MessageType.INFO);
		logger.info("TeamScreen enabled.");
		enabled = true;
		this.netHandler.enable();
		if (frame != null)
			frame.updateStatus();
	}

	public void disable() {
		displayPopup(trayIcon, "TeamScreen now disabled!", "Services stopped.",
				TrayIcon.MessageType.INFO);
		logger.info("TeamScreen disabled.");
		this.netHandler.disable();
		enabled = false;
		if (frame != null)
			frame.updateStatus();
	}

	public void exit() {
		try {
			properties.saveConfig();
			logger.info("Configuration saved succesfully");
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Configuration could not be saved");
		}
		tray.remove(trayIcon);
		if (FolderManager.isWindows())
			JIntellitype.getInstance().cleanUp();
		this.netHandler.disable();
		TeamScreenDaemon.nullifyWindows();
		logger.info("TeamScreen shutdown succesfully");
		System.exit(0);
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void createTray() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();

		trayIcon = new TrayIcon(createImage("/images/TeamScreen-64px.png",
				"tray icon"));

		// Create a popup menu components
		MenuItem openItem = new MenuItem("Open");
		MenuItem pushItem = new MenuItem("Push Window");
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(openItem);
		popup.addSeparator();
		popup.add(pushItem);
		popup.addSeparator();
		popup.add(aboutItem);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.setToolTip("TeamScreen");
		trayIcon.setImageAutoSize(true);

		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openMainWindow();
			}
		});
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					openMainWindow();
				}
			}
		});

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MenuItem item = (MenuItem) e.getSource();
				if(verbose)
					System.out.println(item.getLabel());
				if ("Open".equals(item.getLabel())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							openMainWindow();
						}
					});

				} else if ("Push Window".equals(item.getLabel())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							openWindowSelect();
						}
					});
				} else if ("About".equals(item.getLabel())) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop()
									.browse(URI
											.create("http://aptitekk.com/our-products/teamscreen/"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		};

		openItem.addActionListener(listener);
		pushItem.addActionListener(listener);
		aboutItem.addActionListener(listener);

		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
	}

	protected void openMainWindow() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (frame == null)
					frame = new MainFrame(instance);
				frame.setVisible(true);
			}
		});
	}

	public void openSettingsWindow() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (sframe == null)
					sframe = new SettingsFrame(instance);
				sframe.setVisible(true);
			}
		});
	}

	/**
	 * Obtain the image from inside the jar
	 *
	 * @param path
	 * @param description
	 * @return
	 */
	protected static Image createImage(String path, String description) {
		URL imageURL = TeamScreenDaemon.class.getResource(path);

		if (imageURL == null) {
			TeamScreenDaemon.logger.severe("Resource not found: " + path);
			throw new NullPointerException("Resource not found: " + path);
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	private void addTray() {
		try {
			if (trayIcon == null) {
				createTray();
			}
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}
	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		this.keysPressed.add(e.getKeyChar());
		if (this.keysPressed.size() > 1) {
			// More than one key is currently pressed.
			// Iterate over pressed to get the keys.
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		;

	}

	@Override
	public void keyTyped(KeyEvent e) {
		;
	}

	@Override
	public void onHotKey(int identifier) {
		if (enabled)
			if (identifier == 1) {
				// User wants to share :D
				openWindowSelect();
			}

	}

	public void openPushSelect(final WindowInfo window) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pSelect = new PushSelect(instance, window);
				pSelect.setVisible(true);
				windowPushed = window;
			}
		});
	}

	public void openWindowSelect() {
		// Create the GUI on the event-dispatching thread
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				wSelect = new WindowSelect(instance);
				wSelect.setVisible(true);
			}
		});
	}

	public static WindowManager getWindowManager() {
		return wManager;
	}

	/**
	 * Releases memory-hog Swing windows back into the JVM, which will slowly be
	 * released back into system memory.
	 */
	public static void nullifyWindows() {
		wSelect = null;
		pSelect = null;
		frame = null;
		sframe = null;
		System.gc();
	}

	@Override
	public void haltProgram() {
		try {
			properties.saveConfig();
			logger.info("Configuration saved succesfully");
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Configuration could not be saved");
		}
		tray.remove(trayIcon);
		if (FolderManager.isWindows())
			JIntellitype.getInstance().cleanUp();
		this.netHandler.disable();
		TeamScreenDaemon.nullifyWindows();
	}

	public void restartApplication() {
		logger.info("Restart was called!");
		try {
			final File currentFileLocation = new File(TeamScreenDaemon.class
					.getProtectionDomain().getCodeSource().getLocation()
					.toURI());
			if (!currentFileLocation.getName().endsWith(".exe")) {
				; // continue
			} else {
				final ProcessBuilder builder = new ProcessBuilder("start",
						"/d", currentFileLocation.getPath());
				builder.start();
				this.exit();
			}

			final String javaBin = System.getProperty("java.home")
					+ File.separator + "bin" + File.separator + "java";
			/* is it a jar file? */
			if (!currentFileLocation.getName().endsWith(".jar")) {
				System.err.println("Jar was not found");
				this.exit();
			}
			final ProcessBuilder builder = new ProcessBuilder(javaBin, "-jar",
					currentFileLocation.getPath());
			builder.start();
			this.exit();
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			logger.severe("Couldn't restart!");
			TeamScreenDaemon
					.displayPopup(TeamScreenDaemon.trayIcon, "Error!",
							"Could not restart TeamScreen!",
							TrayIcon.MessageType.ERROR);
		}
	}

	public static PropertiesHandler getPropertiesHandler() {
		return TeamScreenDaemon.properties;
	}

	@Override
	public void logMessage(String message, LevelEnum level) {
		switch (level) {
		case SEVERE:
			TeamScreenDaemon.logger.severe(message);
			TeamScreenDaemon.displayPopup(TeamScreenDaemon.trayIcon,
					"Error downloading updates!", message,
					TrayIcon.MessageType.ERROR);
			break;
		case INFO:
			TeamScreenDaemon.logger.info(message);
			break;
		default:
			break;
		}
	}
	
	public static boolean getVerbose() {
		return verbose;
	}

}
