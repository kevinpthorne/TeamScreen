package com.aptitekk.TeamScreen.Frames;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.aptitekk.TeamScreen.FolderManager;
import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Settings.ConfigureNeighbor;
import com.aptitekk.TeamScreen.Frames.Util.JNeighborDisplayPanel;

/**
 * Settings JFrame of the TeamScreen application
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class SettingsFrame extends JFrame implements ActionListener {
	
	TeamScreenDaemon master;
	
	JPanel cardPanel;
	
	JPanel localSettingsPane;
	JPanel topRow;
	JPanel bottomRow;
	JCheckBox autoUpdate;
	JCheckBox log;
	JComboBox<String> notificationLevel;
	JComboBox<String> slot;
	
	JButton configure;
	JNeighborDisplayPanel neighborPanel;
	JPanel mainPane = new JPanel();
	JButton close;
	JButton more;
	JButton exit;
	JButton save;
	
	ConfigureNeighbor subframe;
	
	final static String NEIGHBOR_PANEL = "nP";
	final static String LOCAL_PANEL = "lP";
	boolean localPanelShowing = false;
	
	public SettingsFrame(TeamScreenDaemon master) {
		this.master = master;
		
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TeamScreen Settings");
		this.setTitle("TeamScreen Settings");
		
		this.setSize(475, 600);
		this.setMaximumSize(new Dimension(475, 600));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setIconImage(FolderManager.createImage("/images/TeamScreen-64px.png", "tray icon"));
		
		initGui();
		
		this.setLocationRelativeTo(null);
	}
	
	private void initGui() {
		this.mainPane.setLayout(new BoxLayout(this.mainPane, BoxLayout.PAGE_AXIS));
		
		this.cardPanel = new JPanel();
		this.cardPanel.setLayout(new CardLayout());
		
		this.neighborPanel = new JNeighborDisplayPanel();
		
		this.localSettingsPane = new JPanel();
		this.localSettingsPane.setLayout(new BoxLayout(this.localSettingsPane, BoxLayout.PAGE_AXIS));
		this.topRow = new JPanel(new FlowLayout());
		this.autoUpdate = new JCheckBox("Auto Update");
		this.log = new JCheckBox("Log");
		this.topRow.add(autoUpdate);
		this.topRow.add(log);
		this.bottomRow = new JPanel(new FlowLayout());
		JPanel notifPanel = new JPanel();
		JLabel notifLabel = new JLabel("Notification Level: ");
		this.notificationLevel = new JComboBox<String>(new String[]{"info", "warning", "error"});
		this.notificationLevel.setToolTipText("Sets the level to show notifications from the tray.");
		this.notificationLevel.setEditable(false);
		notifPanel.add(notifLabel);
		notifPanel.add(this.notificationLevel);
		JPanel slotPanel = new JPanel();
		JLabel slotLabel = new JLabel("Slot: ");
		this.slot = new JComboBox<String>(new String[]{"A","B","C","D","E","F"});
		this.slot.setToolTipText("Sets slot you listen to for other TeamScreen users on the network");
		this.slot.setEditable(false);
		slotPanel.add(slotLabel);
		slotPanel.add(this.slot);
		this.bottomRow.add(notifPanel);
		this.bottomRow.add(slotPanel);
		this.localSettingsPane.add(this.topRow);
		this.localSettingsPane.add(this.bottomRow);
		this.localSettingsPane.add(new JLabel("A restart may be neccessary to apply new settings."));
		
		this.cardPanel.add(this.neighborPanel, NEIGHBOR_PANEL);
		this.cardPanel.add(this.localSettingsPane, LOCAL_PANEL);
		((CardLayout) this.cardPanel.getLayout()).show(this.cardPanel, NEIGHBOR_PANEL);
		
		this.mainPane.add(this.cardPanel);
		
		
		
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.LINE_AXIS));
		this.configure = new JButton("Configure");
		this.configure.setAlignmentX(RIGHT_ALIGNMENT);
		this.configure.addActionListener(this);
		configPanel.add(new JPanel());
		configPanel.add(this.configure);
		this.mainPane.add(configPanel);
		
		this.mainPane.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		this.close = new JButton("Close");
		this.exit = new JButton("Exit");
		this.more = new JButton("More >");
		this.save = new JButton("Save");
		this.close.addActionListener(this);
		this.save.addActionListener(this);
		this.more.addActionListener(this);
		this.exit.addActionListener(this);
		bottomPanel.add(this.save);
		bottomPanel.add(new JPanel());
		bottomPanel.add(this.more);
		bottomPanel.add(new JPanel());
		bottomPanel.add(this.close);
		bottomPanel.add(new JPanel());
		bottomPanel.add(this.exit);
		this.mainPane.add(bottomPanel);
		
		this.add(this.mainPane);
		this.pack();
		
		loadData();
	}
	
	private void loadData() {
		try {
			this.autoUpdate.setSelected(Boolean.valueOf(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("AutoUpdate")));
			this.log.setSelected(Boolean.valueOf(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Log")));
			this.slot.setSelectedItem(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot"));
			this.notificationLevel.setSelectedItem(TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("NotificationLevel"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void saveData() {
		try {
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("AutoUpdate", String.valueOf(this.autoUpdate.isSelected()));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("Log", String.valueOf(this.log.isSelected()));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("Slot", String.valueOf(this.slot.getSelectedItem()));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("NotificationLevel", 
					String.valueOf(this.notificationLevel.getSelectedItem()));
			TeamScreenDaemon.getPropertiesHandler().saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.close)) {
			this.dispose();
		} else if(e.getSource().equals(this.exit)) {
			this.master.exit();
		} else if(e.getSource().equals(this.configure)) {
			SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                	if(subframe == null)
                		subframe = new ConfigureNeighbor(master, neighborPanel);
                	subframe.setVisible(true);
                }
            });
		} else if(e.getSource().equals(this.save)) {
			int restart = JOptionPane.showOptionDialog(this, "Do you want to restart and apply settings?", 
					"Restart required", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			if(restart == JOptionPane.YES_OPTION) {
				this.neighborPanel.saveDataToConfig(this.neighborPanel.getCurrentData());
				this.saveData();
				this.dispose();
				this.master.restartApplication();
			} else if(restart == JOptionPane.NO_OPTION) {
				this.neighborPanel.saveDataToConfig(this.neighborPanel.getCurrentData());
				this.saveData();
				this.dispose();
			} else {
				return;
			}
			
		} else if(e.getSource().equals(this.more)) {
			if(!localPanelShowing) {
				((CardLayout) this.cardPanel.getLayout()).show(this.cardPanel, LOCAL_PANEL);
				localPanelShowing = true;
				this.more.setText("< Back");
				this.configure.setEnabled(false);
			} else {
				((CardLayout) this.cardPanel.getLayout()).show(this.cardPanel, NEIGHBOR_PANEL);
				localPanelShowing = false;
				this.more.setText("More >");
				this.configure.setEnabled(true);
			}
		}
	}

}
