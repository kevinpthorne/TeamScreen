package com.aptitekk.TeamScreen.Frames.Settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.aptitekk.TeamScreen.FolderManager;
import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Util.JArrowButton;
import com.aptitekk.TeamScreen.Frames.Util.JNeighborDisplayPanel;
import com.aptitekk.TeamScreen.Net.NetHandler;
import com.aptitekk.TeamScreen.Net.RadarThread;

public class ConfigureNeighbor extends JFrame implements ListSelectionListener, ActionListener{
	
	TeamScreenDaemon master;
	JNeighborDisplayPanel neighborDisplay;
	
	HashMap<String, String> newMap = new HashMap<String, String>();
	
	JList<?> neighborPick;
	JPanel neighborPane;
	JButton savebtn;
	
	JLabel hostInfo;
	JLabel ipInfo;
	JLabel slotInfo;
	JButton configInfo;
	JButton clearInfo;
	
	JList<?> newNeighborPick;
	public DefaultComboBoxModel<String> aliveNeighborsModel;
	public ArrayList<String> rawAliveNeighborInfo;
	JButton selectNewNeighbor;
	JButton manualSelect;
	JButton cancel;
	RadarThread radarThread;
	
	JTextField manualHost;
	JTextField manualIP;
	JComboBox<String> manualSlot;
	JButton selectManual;
	JButton backtoSelect;
	
	final static String INFOPANEL = "Info";
	final static String SELECTPANEL = "Select";
	final static String MANUALPANEL = "Manual";
	
	public ConfigureNeighbor(TeamScreenDaemon instance, JNeighborDisplayPanel ndisplay) {
		master = instance;
		neighborDisplay = ndisplay;
		
		newMap = ndisplay.getCurrentData();
		
		this.setTitle("Configure Neighbor");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TeamScreen Settings");
		
		this.setSize(300, 500);
		this.setMaximumSize(new Dimension(300, 500));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setIconImage(FolderManager.createImage("/images/TeamScreen-64px.png", "tray icon"));
		this.setLocationRelativeTo(ndisplay);
		
		radarThread = new RadarThread(this);
		radarThread.start();
		
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				radarThread.interrupt();
			}

			@Override
			public void windowOpened(WindowEvent e) {;}
			@Override
			public void windowClosed(WindowEvent e) {;}
			@Override
			public void windowIconified(WindowEvent e) {;}
			@Override
			public void windowDeiconified(WindowEvent e) {;}
			@Override
			public void windowActivated(WindowEvent e) {;}
			@Override
			public void windowDeactivated(WindowEvent e) {;}
			
		});
		
		initGui();
		this.pack();
	}
	
	public void refreshAliveHosts() {
		if(rawAliveNeighborInfo.size() != aliveNeighborsModel.getSize()) {
			rawAliveNeighborInfo.clear();
			aliveNeighborsModel.removeAllElements();
		}
	}
	
	private void initGui() {
		this.getContentPane().setLayout(new BorderLayout());
		
		neighborPick = new JList<Image>(initNeighborComboBox());
		neighborPick.addListSelectionListener(this);
		neighborPick.setCellRenderer(new ImageListRenderer());
		this.getContentPane().add(neighborPick, BorderLayout.LINE_START);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
		
		JPanel hostPanel = new JPanel();
		JLabel hostLabel = new JLabel("Hostname: ");
		hostInfo = new JLabel();
		hostPanel.add(hostLabel);
		hostPanel.add(hostInfo);
		
		JPanel ipPanel = new JPanel();
		JLabel ipLabel = new JLabel("IP: ");
		ipInfo   = new JLabel();
		ipPanel.add(ipLabel);
		ipPanel.add(ipInfo);
		
		JPanel slotPanel = new JPanel();
		JLabel slotLabel = new JLabel("Slot: ");
		slotInfo = new JLabel();
		slotPanel.add(slotLabel);
		slotPanel.add(slotInfo);
		
		JPanel configPanel = new JPanel();
		configInfo = new JButton("Configure");
		configInfo.addActionListener(this);
		configInfo.setEnabled(false);
		clearInfo = new JButton("Clear");
		clearInfo.addActionListener(this);
		clearInfo.setEnabled(false);
		configPanel.add(new JPanel());
		configPanel.add(clearInfo);
		configPanel.add(configInfo);
		infoPanel.add(hostPanel);
		infoPanel.add(ipPanel);
		infoPanel.add(slotPanel);
		infoPanel.add(configPanel);
		
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new BorderLayout());
		newNeighborPick = new JList<String>(aliveNeighborsModel);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		selectNewNeighbor = new JButton("Select");
		selectNewNeighbor.addActionListener(this);
		manualSelect = new JButton("Manual");
		manualSelect.addActionListener(this);
		buttonPanel.add(cancel);
		buttonPanel.add(selectNewNeighbor);
		buttonPanel.add(manualSelect);
		selectPanel.add(newNeighborPick, BorderLayout.CENTER);
		selectPanel.add(buttonPanel, BorderLayout.PAGE_END);
		
		
		JPanel manualPanel = new JPanel();
		manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
		
		JPanel _hostPanel = new JPanel();
		JLabel _hostLabel = new JLabel("Hostname: ");
		manualHost = new JTextField(15);
		_hostPanel.add(_hostLabel);
		_hostPanel.add(manualHost);
		
		JPanel _ipPanel = new JPanel();
		JLabel _ipLabel = new JLabel("IP: ");
		manualIP   = new JTextField(16);
		_ipPanel.add(_ipLabel);
		_ipPanel.add(manualIP);
		
		JPanel _slotPanel = new JPanel();
		JLabel _slotLabel = new JLabel("Slot: ");
		manualSlot = new JComboBox<String>(new String[]{"A","B","C","D","E","F"});
		manualSlot.setEditable(false);
		_slotPanel.add(_slotLabel);
		_slotPanel.add(manualSlot);
		
		JPanel manualButtonPanel = new JPanel(new FlowLayout());
		selectManual = new JButton("Select");
		selectManual.addActionListener(this);
		backtoSelect = new JButton("Back");
		backtoSelect.addActionListener(this);
		manualButtonPanel.add(selectManual);
		manualButtonPanel.add(backtoSelect);
		manualPanel.add(_hostPanel);
		manualPanel.add(_ipPanel);
		manualPanel.add(_slotPanel);
		manualPanel.add(manualButtonPanel);
		
		neighborPane = new JPanel(new CardLayout());
		neighborPane.add(infoPanel, INFOPANEL);
		neighborPane.add(selectPanel, SELECTPANEL);
		neighborPane.add(manualPanel, MANUALPANEL);
		this.getContentPane().add(neighborPane, BorderLayout.CENTER);
		
		this.savebtn = new JButton("Save");
		this.savebtn.setAlignmentX(RIGHT_ALIGNMENT);
		this.savebtn.addActionListener(this);
		this.getContentPane().add(this.savebtn, BorderLayout.PAGE_END);
	}
	
	private ListModel<Image> initNeighborComboBox() {
		DefaultComboBoxModel<Image> model = new DefaultComboBoxModel<Image>();
		
		ImageIcon rightPicture = JArrowButton.getImagefromDirection(JArrowButton.EAST);
		ImageIcon leftPicture = JArrowButton.getImagefromDirection(JArrowButton.WEST);
		ImageIcon topPicture = JArrowButton.getImagefromDirection(JArrowButton.NORTH);
		ImageIcon bottomPicture = JArrowButton.getImagefromDirection(JArrowButton.SOUTH);
		ImageIcon projectorPicture = JArrowButton.getImagefromDirection(JArrowButton.SPECIAL);
		ImageIcon centerPicture = new ImageIcon(FolderManager.createImage("/images/computer.png", "computer"));
		
		ImageIcon[] imageIcons = new ImageIcon[]{rightPicture, leftPicture, topPicture, bottomPicture, projectorPicture, centerPicture};
		for(int i = 0; i < imageIcons.length; i++) {
			Image img = imageIcons[i].getImage();
			img = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
			model.addElement(img);
		}
		
		return model;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource().equals(neighborPick)) {
			this.configInfo.setEnabled(true);
			this.clearInfo.setEnabled(true);
			
			this.manualIP.setEditable(true);
			this.selectNewNeighbor.setEnabled(true);
			String[] data = null;
			if(neighborPick.getSelectedIndex() == 0) { // right
				data = newMap.get("Right").split(":");
			} else if(neighborPick.getSelectedIndex() == 1) { //left
				data = newMap.get("Left").split(":");
			} else if(neighborPick.getSelectedIndex() == 2) {
				data = newMap.get("Top").split(":");
			} else if(neighborPick.getSelectedIndex() == 3) {
				data = newMap.get("Bottom").split(":");
			} else if(neighborPick.getSelectedIndex() == 4) {
				data = newMap.get("Projector").split(":");
			} else { //center
				data = newMap.get("Center").split(":");
				this.manualIP.setEditable(false);
				this.selectNewNeighbor.setEnabled(false);
				this.clearInfo.setEnabled(false);
			} try {
				this.hostInfo.setText(data[0]);
				this.manualHost.setText(data[0]);
				this.manualIP.setText(data[1]);
				this.ipInfo.setText(data[1]);
				this.slotInfo.setText(data[2]);
				this.manualSlot.setSelectedItem(data[2]);
			} catch (ArrayIndexOutOfBoundsException ex) {
				this.hostInfo.setText("");
				this.manualHost.setText("");
				this.manualIP.setText("");
				this.ipInfo.setText("");
				this.slotInfo.setText("");
				this.manualSlot.setSelectedItem("");
			}
		}
	}
	
	private void refreshInfo() {
		String[] data = null;
		if(neighborPick.getSelectedIndex() == 0) { // right
			data = newMap.get("Right").split(":");
		} else if(neighborPick.getSelectedIndex() == 1) { //left
			data = newMap.get("Left").split(":");
		} else if(neighborPick.getSelectedIndex() == 2) {
			data = newMap.get("Top").split(":");
		} else if(neighborPick.getSelectedIndex() == 3) {
			data = newMap.get("Bottom").split(":");
		} else if(neighborPick.getSelectedIndex() == 4) {
			data = newMap.get("Projector").split(":");
		} else { //center
			data = newMap.get("Center").split(":");
		} try {
			this.hostInfo.setText(data[0]);
			this.manualHost.setText(data[0]);
			this.manualIP.setText(data[1]);
			this.ipInfo.setText(data[1]);
			this.slotInfo.setText(data[2]);
			this.manualSlot.setSelectedItem(data[2]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			this.hostInfo.setText("");
			this.manualHost.setText("");
			this.manualIP.setText("");
			this.ipInfo.setText("");
			this.slotInfo.setText("");
			this.manualSlot.setSelectedItem("");
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.configInfo)) {
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, SELECTPANEL);
		} else if(e.getSource().equals(this.clearInfo)) {
			int goodToClear = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear this entry?", 
					"Clear Entry", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
			if(goodToClear == JOptionPane.YES_OPTION) {
				newMap.put(indexToString(neighborPick.getSelectedIndex()), "");
				this.neighborPick.setSelectedIndex(neighborPick.getSelectedIndex());
				this.refreshInfo();
				this.repaint();
			}
			else 
				return;
		}
		else if(e.getSource().equals(this.selectNewNeighbor)) {
			if(neighborPick.getSelectedIndex() == 5) { // CENTER
				JOptionPane.showMessageDialog(this, "Cannot replace your own information", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String rawData = this.rawAliveNeighborInfo.get(newNeighborPick.getSelectedIndex());
			rawData = rawData.replace(NetHandler.PACKET_SEPARATOR_CONSTANT, ":");
			rawData = rawData.trim();
			//rawData = rawData.replaceAll("[\\n\\t\\s\\r ]", "");
			StringBuilder data = new StringBuilder();
			data.append(rawData.split(":")[0]);
			data.append(":");
			data.append(rawData.split(":")[1]);
			data.append(":");
			data.append("A");
			//data.append(newMap.get("Center").split(":")[2]);
			System.out.println("Data: " + data.toString());
			newMap.put(indexToString(neighborPick.getSelectedIndex()), data.toString());
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, INFOPANEL);
			this.neighborPick.setSelectedIndex(neighborPick.getSelectedIndex());
			this.refreshInfo();
			this.repaint();
		} else if(e.getSource().equals(this.selectManual)) {
			String data = manualHost.getText() + ":" + manualIP.getText() + ":" + manualSlot.getSelectedItem();
			// ----------------------- Validation -------------------------
			if(manualHost.getText().isEmpty() && manualIP.getText().isEmpty()) { //needs to clear neighbor
				data = "";
			} else if(manualHost.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please fill in the hostname information", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else if(manualIP.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please fill in the IP information", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else if(!NetHandler.validateIP(manualIP.getText())) {
				JOptionPane.showMessageDialog(this, "Please type a valid IP address", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(neighborPick.getSelectedIndex() == 5) { //CENTER
				JOptionPane.showMessageDialog(this, "Your IP field will be disregarded but all other information will be written", "Warning", JOptionPane.WARNING_MESSAGE);
				data = manualHost.getText() + ":" + newMap.get("Center").split(":")[1] + ":" + manualSlot.getSelectedItem();
			}
			// ---------------------- End Validation ------------------------
			newMap.put(indexToString(neighborPick.getSelectedIndex()), data);
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, INFOPANEL);
			this.neighborPick.setSelectedIndex(neighborPick.getSelectedIndex());
			this.refreshInfo();
			this.repaint();
		} else if(e.getSource().equals(this.savebtn)) {
			this.neighborDisplay.refresh(newMap);
			if(radarThread != null)
				radarThread.interrupt();
			radarThread = null;
			this.dispose();
		} else if(e.getSource().equals(this.manualSelect)) {
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, MANUALPANEL);
		} else if(e.getSource().equals(this.cancel)) {
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, INFOPANEL);
		} else if(e.getSource().equals(this.backtoSelect)) {
			((CardLayout) this.neighborPane.getLayout()).show(this.neighborPane, SELECTPANEL);
		}
	}
	
	private String indexToString(int index) {
		if(neighborPick.getModel().getSize() == 6) {
			if(index == 0) {
				return "Right";
			} else if(index == 1) {
				return "Left";
			} else if(index == 2) {
				return "Top";
			} else if(index == 3) {
				return "Bottom";
			} else if(index == 4) {
				return "Projector";
			} else if(index == 5) {
				return "Center";
			} else {
				return null;
			}
		}
		return null;
	}
	
	public class ImageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(new ImageIcon((Image) value));
            label.setText("");
            return label;
        }
    }

}
