package com.aptitekk.TeamScreen.Frames.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.aptitekk.TeamScreen.FolderManager;
import com.aptitekk.TeamScreen.TeamScreenDaemon;

/**
 * Displays the 5 neighbors in what you see in the properties file.
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class JNeighborDisplayPanel extends JPanel{
	
	HashMap<String, String> mappedData = new HashMap<String, String>();
	
	protected HashMap<String, String> getConfigData() {
		try {
			String rightData = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("RNeighbor");
			String leftData = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("LNeighbor");
			String topData = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("TNeighbor");
			String bottomData = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("BNeighbor");
			String projectorData = TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("PNeighbor");
			String centerData = InetAddress.getLocalHost().getHostName() + ":" + InetAddress.getLocalHost().getHostAddress() + ":" +
					TeamScreenDaemon.getPropertiesHandler().getConfig().getProperty("Slot");
			
			HashMap<String, String> returnMap = new HashMap<String, String>();
			
			returnMap.put("Right", rightData);
			returnMap.put("Left", leftData);
			returnMap.put("Top", topData);
			returnMap.put("Bottom", bottomData);
			returnMap.put("Projector", projectorData);
			returnMap.put("Center", centerData);
			
			return returnMap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	public void saveDataToConfig(HashMap<String, String> newData) {
		try {
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("RNeighbor", newData.get("Right"));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("LNeighbor", newData.get("Left"));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("TNeighbor", newData.get("Top"));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("BNeighbor", newData.get("Bottom"));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("PNeighbor", newData.get("Projector"));
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("Name", newData.get("Center").split(":")[0]);
			TeamScreenDaemon.getPropertiesHandler().getConfig().setProperty("Slot", newData.get("Center").split(":")[2]);
			
			TeamScreenDaemon.getPropertiesHandler().saveConfig();
			
			TeamScreenDaemon.logger.info("Configuration saved successfully");
			
		} catch (IOException e) {
			e.printStackTrace();
			TeamScreenDaemon.logger.severe("Configuration could not be saved");
			JOptionPane.showMessageDialog(this, "Could not save configuration", "Configuration Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public HashMap<String, String> getData() {
		return mappedData;
	}
	
	public JNeighborDisplayPanel() {
		initGui(getConfigData());
	}
	
	public void refresh() {
		this.removeAll();
		this.repaint();
		initGui(getConfigData());
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		frame.pack();
		this.repaint();
	}
	public void refresh(HashMap<String, String> newData) {
		this.removeAll();
		this.repaint();
		initGui(newData);
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		frame.pack();
		this.repaint();
	}
	public HashMap<String, String> getCurrentData() {
		return this.mappedData;
	}
	
	private void initGui(HashMap<String, String> newData) {
		this.mappedData = newData;
		
		String rightData = getCurrentData().get("Right");
		String leftData = getCurrentData().get("Left");
		String topData = getCurrentData().get("Top");
		String bottomData = getCurrentData().get("Bottom");
		String projectorData = getCurrentData().get("Projector");
		String centerData = getCurrentData().get("Center");
		
		ImageIcon rightPicture = JArrowButton.getImagefromDirection(JArrowButton.EAST);
		ImageIcon leftPicture = JArrowButton.getImagefromDirection(JArrowButton.WEST);
		ImageIcon topPicture = JArrowButton.getImagefromDirection(JArrowButton.NORTH);
		ImageIcon bottomPicture = JArrowButton.getImagefromDirection(JArrowButton.SOUTH);
		ImageIcon projectorPicture = JArrowButton.getImagefromDirection(JArrowButton.SPECIAL);
		ImageIcon centerPicture = new ImageIcon(FolderManager.createImage("/images/computer.png", "computer"));
		
		JPanel rightPane = new JPanel();
		JPanel leftPane = new JPanel();
		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		JPanel projectorPane = new JPanel();
		JPanel centerPane = new JPanel();
		
		//scale pictures
		ImageIcon[] pictures = new ImageIcon[]{rightPicture, leftPicture, topPicture, bottomPicture, projectorPicture, centerPicture};
		String[] data = new String[]{rightData, leftData, topData, bottomData, projectorData, centerData};
		JPanel[] panes= new JPanel[]{rightPane, leftPane, topPane, bottomPane, projectorPane, centerPane};
		for(int i = 0; i < pictures.length && i < data.length && i < panes.length; i++) {
			Image a = pictures[i].getImage();
			a = a.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
			pictures[i] = new ImageIcon(a);
			
			panes[i].setLayout(new BoxLayout(panes[i], BoxLayout.PAGE_AXIS));
			JLabel picLabel = new JLabel(pictures[i]);
			picLabel.setAlignmentX(CENTER_ALIGNMENT);
			panes[i].add(picLabel);
			String name = data[i].split(":")[0];
			JLabel dataLabel = new JLabel(name);
			dataLabel.setAlignmentX(CENTER_ALIGNMENT);
			panes[i].add(dataLabel);
			
			panes[i].setBackground(Color.WHITE);
			panes[i].setMinimumSize(new Dimension(100,100));
			panes[i].setBorder(BorderFactory.createLineBorder(Color.black));
		}
		
		this.setLayout(new GridLayout(3,5));
		
		this.add(emptyPane());
		this.add(panes[4]);
		this.add(panes[2]);
		this.add(emptyPane());
		this.add(emptyPane());
		
		this.add(emptyPane());
		this.add(panes[1]);
		this.add(panes[5]);
		this.add(panes[0]);
		this.add(emptyPane());
		
		this.add(emptyPane());
		this.add(emptyPane());
		this.add(panes[3]);
		this.add(emptyPane());
		this.add(emptyPane());
	}
	
	private JPanel emptyPane() {
		JPanel returnPanel = new JPanel();
		returnPanel.setBackground(Color.WHITE);
		returnPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		return returnPanel;
	}

}
