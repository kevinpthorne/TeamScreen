package com.AptiTekk.AutoUpdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class UpdaterFrame extends JFrame
{
    
    public UpdaterFrame()
    {
	this.setResizable(false);
	this.setAlwaysOnTop(true);
	this.setTitle("Installing Updates...");
	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	this.setIconImage(getIconFromPath("images/TeamScreen-64px.png")
		.getImage());
	this.setUndecorated(true);
	
	JPanel mainPanel = new JPanel();
	this.setContentPane(mainPanel);
	
	mainPanel.setBackground(Color.WHITE);
	mainPanel.setBorder(new MatteBorder(10,10,10,10,new Color(0,120,255)));
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	
	JLabel headerLabel = new JLabel();
	headerLabel.setIcon(getIconFromPath("images/InstallingHeader.png"));
	headerLabel.setBorder(new EmptyBorder(0,0,25,0));
	
	this.add(headerLabel);
	
	JProgressBar progressBar = new JProgressBar();
	progressBar.setIndeterminate(true);
	progressBar.setPreferredSize(new Dimension(headerLabel.getWidth(),
		(int) progressBar.getPreferredSize().getHeight()));
	progressBar.setMinimumSize(progressBar.getPreferredSize());
	progressBar.setBorderPainted(false);
	
	this.add(progressBar);
	
	this.pack();
	this.setLocationRelativeTo(null);
	
	this.setVisible(true);
	this.setAlwaysOnTop(true);
	this.setAlwaysOnTop(false);
    }
    
    private ImageIcon getIconFromPath(String path)
    {
	BufferedImage img = null;
	try
	{
	    URL resource = getClass().getResource(path);
	    if(resource == null)
	    {
		JOptionPane
		.showMessageDialog(
			null,
			"AutoUpdater could not start. The program may be corrupted.",
			"Error", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
		return null;
	    }
	    img = ImageIO.read(resource);
	    return new ImageIcon(img);
	}
	catch(IOException e)
	{
	    JOptionPane
	    .showMessageDialog(
		    null,
		    "AutoUpdater could not start. The program may be corrupted.",
		    "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	    return null;
	}
    }
    
    private ImageIcon getIconFromPath(String path, int u, int v, int w, int h)
    {
	BufferedImage img = null;
	try
	{
	    URL resource = getClass().getResource(path);
	    if(resource == null)
	    {
		JOptionPane
		.showMessageDialog(
			null,
			"AutoUpdater could not start. The program may be corrupted.",
			"Error", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
		return null;
	    }
	    img = ImageIO.read(resource);
	    Image image = createImage(new FilteredImageSource(img.getSource(),
		    new CropImageFilter(u, v, w, h)));
	    return new ImageIcon(image);
	}
	catch(IOException e)
	{
	    JOptionPane
	    .showMessageDialog(
		    null,
		    "AutoUpdater could not start. The program may be corrupted.",
		    "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	    return null;
	}
    }
    
}
