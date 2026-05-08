package com.aptitekk.TeamScreen.Frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Util.JArrowButton;
import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * Prompts where to push a window
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class PushSelect extends JFrame implements ActionListener, KeyListener {
	
	TeamScreenDaemon master;
	WindowInfo window;
	
	JArrowButton btnnorth;
	JArrowButton btnsouth;
	JArrowButton btneast;
	JArrowButton btnwest;
	JArrowButton btndisplay;
	JArrowButton btnexit;
	
	public PushSelect(TeamScreenDaemon master, WindowInfo window) {
		
		this.master = master;
		this.window = window;
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Already there
	    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    this.setUndecorated(true);
	    
	    try {
	    	this.setOpacity(0.55f);
	    } catch (UnsupportedOperationException e) {
	    	if(e.getMessage().contains("translucency")) {
	    		;
	    	}
	    	else 
	    		e.printStackTrace();
	    }
	    
	    this.setBackground(Color.BLACK);
	    
	    initGui();
	    
	    this.addKeyListener(this);
	    
	    this.pack();
	    this.setVisible(true);
	    this.repaint();
	}
	
	private void initGui() {
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		
		JPanel pane = new JPanel(new BorderLayout());
		JPanel top = new JPanel();
		
		btnnorth = new JArrowButton(JArrowButton.NORTH);
		btnsouth = new JArrowButton(JArrowButton.SOUTH);
		btnwest = new JArrowButton(JArrowButton.WEST);
		btneast = new JArrowButton(JArrowButton.EAST);
		btndisplay = new JArrowButton(JArrowButton.SPECIAL);
		btnexit = new JArrowButton(JArrowButton.X);
		
		btnnorth.addActionListener(this);
		btnsouth.addActionListener(this);
		btnwest.addActionListener(this);
		btneast.addActionListener(this);
		btndisplay.addActionListener(this);
		btnexit.addActionListener(this);
		
		top.add(btndisplay);
		top.add(btnnorth);
		pane.add(top, BorderLayout.NORTH);
		
		pane.add(btnsouth, BorderLayout.SOUTH);
		pane.add(btnwest, BorderLayout.WEST);
		pane.add(btneast, BorderLayout.EAST);
		
		pane.setBorder(BorderFactory.createEmptyBorder(150, 150, 150, 150));
		
		String windowTitleString = this.window.getTitle();
		if(windowTitleString.contains(" - ")) {
			String[] wtS = windowTitleString.split(" - ");
			windowTitleString = wtS[wtS.length-1];
		}
		if(windowTitleString.equals("Program Manager")) {
			windowTitleString = "Desktop";
		}
		
		JPanel center = (JPanel) this.getGlassPane();
		center.setAlignmentX(0.5f);
		center.setAlignmentY(0.5f);
		center.setLayout(new GridBagLayout());
		JLabel infoText = new JLabel("<html><p style='font-size:40px'>Select where you want to push this window.</p><br></html>");
		JLabel windowTitle = new JLabel("<html><br><p style='font-size:40px'><i>"+windowTitleString+"</i></p></html>");
		infoText.setAlignmentX(0.5f);
		infoText.setAlignmentY(0.5f);
		windowTitle.setAlignmentX(0.5f);
		windowTitle.setAlignmentY(0.5f);
		GridBagConstraints c = new GridBagConstraints();
		
		center.add(infoText, c);
		c.gridy = 1;
		center.add(windowTitle,c);
		c.gridy = 2;
		center.add(btnexit, c);
		center.setVisible(true);
		
		System.out.println("Window Info:" + this.window.toString());
		
		this.add(pane);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btndisplay)) {
			TeamScreenDaemon.logger.info("Display Selected!");
			TeamScreenDaemon.windowPushed = this.window;
			this.master.netHandler.requestConnecttoNeighbor(JArrowButton.SPECIAL);
			this.close();
		} else if (e.getSource().equals(btnnorth)) {
			TeamScreenDaemon.logger.info("North Selected!");
			TeamScreenDaemon.windowPushed = this.window;
			this.master.netHandler.requestConnecttoNeighbor(JArrowButton.NORTH);
			this.close();
		}else if (e.getSource().equals(btnsouth)) {
			TeamScreenDaemon.logger.info("South Selected!");
			TeamScreenDaemon.windowPushed = this.window;
			this.master.netHandler.requestConnecttoNeighbor(JArrowButton.SOUTH);
			this.close();
		}else if (e.getSource().equals(btnwest)) {
			TeamScreenDaemon.logger.info("West Selected!");
			TeamScreenDaemon.windowPushed = this.window;
			this.master.netHandler.requestConnecttoNeighbor(JArrowButton.WEST);
			this.close();
		}else if (e.getSource().equals(btneast)) {
			TeamScreenDaemon.logger.info("East Selected!");
			TeamScreenDaemon.windowPushed = this.window;
			this.master.netHandler.requestConnecttoNeighbor(JArrowButton.EAST);
			this.close();
		} else if(e.getSource().equals(btnexit)) {
			TeamScreenDaemon.logger.info("Canceled");
			this.close();
		}
	}
	
	private void close() {
		this.dispose();
		TeamScreenDaemon.nullifyWindows();
	}

	@Override
	public void keyPressed(KeyEvent e) {;}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.dispose();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {;}

}
