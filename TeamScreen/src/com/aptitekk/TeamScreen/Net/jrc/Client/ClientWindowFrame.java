package com.aptitekk.TeamScreen.Net.jrc.Client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.one.stone.soup.grfx.ImageFactory;
import org.one.stone.soup.remote.control.client.RemoteControlClient;

import com.aptitekk.TeamScreen.Net.jrc.ClientApplication;

/**
 * Shows the window pushed onto the computer
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class ClientWindowFrame extends JFrame implements RemoteControlClient {
	
	private ClientApplication controller;
	private ImageIcon viewIcon;
	public JLabel picLabel;
	private boolean firstFrame = true;
	
	public ClientWindowFrame(final ClientApplication controller, String title) {
		this.controller = controller;
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controller.disconnect();
			}
		});
		
		this.setTitle(title + " - TeamScreen");
		//this.setUndecorated(true);
		this.setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		this.setIconImage(ImageFactory.loadImage("jar://images/TeamScreen-64px.png"));
		
		this.viewIcon = new ImageIcon( ImageFactory.loadImage("jar://images/TeamScreen-64px.png") );
		
		this.picLabel = new JLabel(this.viewIcon);
		add(this.picLabel);
		
		this.setSize(this.picLabel.getSize().width, this.picLabel.getSize().height+60);
		this.setVisible(true);
		this.pack();
		this.setResizable(false);
	}
	
	@Override
	public void showNewImage(Image image) {
		if(image==null)
		{
			System.out.println("Image is null!");
			return;
		}
		
		this.viewIcon.setImage(image);
		this.picLabel.setIcon(this.viewIcon);
		
		if(this.firstFrame && image.getWidth(this)!=-1 && image.getHeight(this)!=-1)
		{
			this.controller.doLayout();
			this.firstFrame=false;
		}
		
		this.setSize(new Dimension(image.getWidth(this)+15, image.getHeight(this)+40));
		this.picLabel.repaint();
		this.repaint(0);
	}
	@Override
	public void playerStopped() {
		this.dispose();
	}
	@Override
	public void newFrame() {
		this.controller.requestNextFrame();
	}
	@Override
	public void setFullscreen(boolean state) {
		;
	}
	@Override
	public Component getView() {
		return this;
	}

}
