package com.aptitekk.TeamScreen.AutoUpdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.one.stone.soup.reloader.UpdateListener;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class DownloadDialog extends JFrame implements DLProgressListener, MouseListener
{
    
    private JProgressBar overallProgressBar;
    private JProgressBar currentProgressBar;
    private int amountToDownload;
    private int currentDownload;
    private ImageIcon[] cancelButtonIcon;
    private ImageIcon[] okayButtonIcon;
    private JLabel buttonLabel;
    private boolean canceled = false;
    private JLabel progressLabel;
    
	protected Point mouseDownCompCoords;
    
    public DownloadDialog(int amountToDownload)
    {
	this.amountToDownload = amountToDownload;
	this.currentDownload = 0;
	
	this.setResizable(false);
	this.setAlwaysOnTop(false);
	this.setTitle("Downloading Updates...");
	this.setIconImage(AutoUpdater.windowIcon);
	this.setUndecorated(true);
	
	this.addMouseListener(new MouseListener(){
        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }
        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseClicked(MouseEvent e) {
        }
    });

    this.addMouseMotionListener(new MouseMotionListener(){
        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    });
	
	JPanel mainPanel = new JPanel();
	this.setContentPane(mainPanel);
	
	mainPanel.setBackground(Color.WHITE);
	mainPanel.setBorder(new MatteBorder(10,10,10,10,new Color(0,120,255)));
	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	this.addWindowListener(new WindowAdapter()
	{
	    
	    @Override
	    public void windowClosing(WindowEvent e)
	    {
		canceled = true;
		dispose();
		AutoUpdater.notifyObject();
	    }
	});
	this.addWindowFocusListener(new WindowFocusListener() {

		@Override
		public void windowGainedFocus(WindowEvent e) {
			((JPanel) getContentPane()).setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(0, 120, 255)),
					new EmptyBorder(10, 10, 10, 10)));
		}

		@Override
		public void windowLostFocus(WindowEvent e) {
			((JPanel) getContentPane()).setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(138, 138, 138)),
					new EmptyBorder(10, 10, 10, 10)));
		}
		
	});
	
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	
	JLabel headerLabel = new JLabel();
	headerLabel.setIcon(this.getIconFromPath("images/DownloadDialog/DownloadHeader.png"));
	
	this.add(headerLabel);
	
	this.overallProgressBar = new JProgressBar();
	this.overallProgressBar.setPreferredSize(new Dimension(headerLabel.getWidth(), (int) this.overallProgressBar.getPreferredSize().getHeight()));
	this.overallProgressBar.setMinimumSize(this.overallProgressBar.getPreferredSize());
	this.overallProgressBar.setBackground(mainPanel.getBackground());
	this.overallProgressBar.setBorderPainted(false);
	
	this.add(this.overallProgressBar);
	
	this.currentProgressBar = new JProgressBar();
	this.currentProgressBar.setPreferredSize(this.overallProgressBar.getPreferredSize());
	this.currentProgressBar.setMinimumSize(this.overallProgressBar.getPreferredSize());
	this.currentProgressBar.setBackground(mainPanel.getBackground());
	this.currentProgressBar.setBorderPainted(false);
	
	this.add(this.currentProgressBar);
	
	this.progressLabel = new JLabel("<html><div style='margin-left: 10px'>Downloading Updates... (File 1 of "+amountToDownload+")</div></html>");
	this.progressLabel.setForeground(Color.BLACK);
	this.progressLabel.setPreferredSize(new Dimension(headerLabel.getWidth(), 20));
	this.progressLabel.setMinimumSize(this.progressLabel.getPreferredSize());
	
	this.add(progressLabel);
	
	this.cancelButtonIcon = new ImageIcon[] {this.getIconFromPath("images/DownloadDialog/CancelButton.png", 0, 0, 130, 33), this.getIconFromPath("images/DownloadDialog/CancelButton.png", 0, 33, 130, 33)};
	this.okayButtonIcon = new ImageIcon[] {this.getIconFromPath("images/DownloadDialog/OkayButton.png", 0, 0, 130, 33), this.getIconFromPath("images/DownloadDialog/OkayButton.png", 0, 33, 130, 33)};
	
	this.buttonLabel = new JLabel();
	this.buttonLabel.setIcon(this.cancelButtonIcon[0]);
	this.buttonLabel.setBackground(mainPanel.getBackground());
	this.buttonLabel.setBorder(new EmptyBorder(10,85,10,85));
	this.buttonLabel.addMouseListener(this);
	
	this.add(this.buttonLabel);
	
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
	    URL resource = AutoUpdater.getResourceByPath(path);
	    if(resource == null)
	    {
		AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.",
			LevelEnum.SEVERE);
		canceled = true;
		dispose();
		AutoUpdater.notifyObject();
		return null;
	    }
	    img = ImageIO.read(resource);
	    return new ImageIcon(img);
	}
	catch(IOException e)
	{
	    AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.",
			LevelEnum.SEVERE);
	    canceled = true;
		dispose();
		AutoUpdater.notifyObject();
	    return null;
	}
    }
    
    private ImageIcon getIconFromPath(String path, int u, int v, int w, int h)
    {
	BufferedImage img = null;
	try
	{
	    URL resource = AutoUpdater.getResourceByPath(path);
	    if(resource == null)
	    {
		AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.",
			LevelEnum.SEVERE);
		canceled = true;
		dispose();
		AutoUpdater.notifyObject();
		return null;
	    }
	    img = ImageIO.read(resource);
	    Image image = createImage(new FilteredImageSource(img.getSource(),
		    new CropImageFilter(u, v, w, h)));
	    return new ImageIcon(image);
	}
	catch(IOException e)
	{
	    AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.",
			LevelEnum.SEVERE);
	    canceled = true;
		dispose();
		AutoUpdater.notifyObject();
	    return null;
	}
    }
    
    @Override
    public void progressChanged(int newProgress)
    {
	this.currentProgressBar.setValue(newProgress);
	this.overallProgressBar.setValue((int)(((this.currentDownload-1) * (1f/(float)this.amountToDownload)) * 100) + (int)(((1f/(float)this.amountToDownload) * ((float)newProgress/(float)100)) * (float)100));
    }
    
    @Override
    public void downloadStarted()
    {
	this.currentDownload++;
	this.currentProgressBar.setValue(0);
	this.progressLabel.setText("<html><div style='margin-left: 10px'>Downloading Updates... (File "+currentDownload+" of "+amountToDownload+")</div></html>");
    }
    
    @Override
    public void downloadFailed(String reason)
    {
	AutoUpdater.deleteUpdatesFolder();
	this.currentProgressBar.setIndeterminate(true);
	this.buttonLabel.setIcon(okayButtonIcon[0]);
	this.progressLabel.setPreferredSize(new Dimension(this.progressLabel.getWidth(), 35));
	this.progressLabel.setMinimumSize(this.progressLabel.getPreferredSize());
	this.progressLabel.setText("<html><div style='margin-left: 10px'>Could not Download.<br>Reason: "+reason+"</div></html>");
	this.pack();
    }
    
    @Override
    public void downloadCompleted()
    {
	if(this.currentDownload == this.amountToDownload)
	{
	    if(!canceled)
	    {
		this.dispose();
		try {
		    AutoUpdater.haltProgram();
		    new ProcessBuilder("java", "-jar", AutoUpdater.getAbsDir()+"/AutoUpdater.jar", "TeamScreen.jar").start();
		    System.exit(0);
		} catch (IOException e) {
		    e.printStackTrace();
		    AutoUpdater.notifyObject();
		}
	    }
	}
    }
    
    @Override
    public boolean shouldCancelDownload()
    {
	if(this.canceled)
	{
	    AutoUpdater.deleteUpdatesFolder();
	}
	return this.canceled;
    }
    
    @Override
    public void mouseClicked(MouseEvent arg0)
    {
    }
    
    @Override
    public void mouseEntered(MouseEvent arg0)
    {
    }
    
    @Override
    public void mouseExited(MouseEvent arg0)
    {
    }
    
    @Override
    public void mousePressed(MouseEvent arg0)
    {
	if(arg0.getSource().equals(this.buttonLabel))
	{
	    if(this.currentProgressBar.isIndeterminate())
	    {
		this.buttonLabel.setIcon(this.okayButtonIcon[1]);
	    }
	    else 
	    {
		this.buttonLabel.setIcon(this.cancelButtonIcon[1]);
	    }
	}
    }
    
    @Override
    public void mouseReleased(MouseEvent arg0)
    {
	if(arg0.getSource().equals(this.buttonLabel))
	{
	    if(this.currentProgressBar.isIndeterminate()) //Error / Okay
	    {
		this.dispose();
	    }
	    else //Cancel
	    {
		this.canceled = true;
		this.dispose();
	    }
	    AutoUpdater.notifyObject();
	}
	if(this.currentProgressBar.isIndeterminate())
	{
	    this.buttonLabel.setIcon(this.okayButtonIcon[0]);
	}
	else 
	{
	    this.buttonLabel.setIcon(this.cancelButtonIcon[0]);
	}
    }
    
}
