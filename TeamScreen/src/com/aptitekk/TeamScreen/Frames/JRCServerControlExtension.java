package com.aptitekk.TeamScreen.Frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.aptitekk.TeamScreen.Net.jrc.DynamicJRCServerApplication;

public class JRCServerControlExtension extends JFrame{
	
	DynamicJRCServerApplication master;
	
	public JRCServerControlExtension(DynamicJRCServerApplication instance) {
		master = instance;
		
		this.setMaximumSize(new Dimension(50, 450));
		this.setMinimumSize(new Dimension(50, 450));
		setResizable(false);
		setLocationRelativeTo(null);
		
        this.setUndecorated(true);
        this.setVisible(true);
        this.setBounds(0, 0, 480, 70);
        this.setAlwaysOnTop(true);
	    
		initGui();
		
		this.getContentPane().setBackground(Color.WHITE);
		((JPanel) this.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(0, 120, 255)),
				new EmptyBorder(10, 10, 10, 10)));
		
		this.pack();
	}
	
	private void initGui() {
		
	}

}
