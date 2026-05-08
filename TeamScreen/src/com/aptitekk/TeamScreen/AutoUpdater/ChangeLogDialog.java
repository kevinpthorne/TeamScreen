package com.aptitekk.TeamScreen.AutoUpdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
/**
 * @author Mitchell Talmadge
 * 
 * Used under permission
 *
 */
public class ChangeLogDialog extends JFrame implements MouseListener
{
    
    boolean userReplied = false;
    boolean userWantsUpdate = false;
    private ImageIcon[] yesButtonIcon;
    private ImageIcon[] noButtonIcon;
    private JLabel yesButtonLabel;
    private JLabel noButtonLabel;
    
	protected Point mouseDownCompCoords;
    
    public ChangeLogDialog(String changeLog, int amountToUpdate)
    {
	this.setResizable(false);
	this.setAlwaysOnTop(false);
	this.setTitle("Update"+(amountToUpdate > 1 ? "s" : "") + "Available!");
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
		userWantsUpdate = false;
		userReplied = true;
		dispose();
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
	headerLabel.setIcon(getIconFromPath("images/ChangeLogDialog/ChangeLogHeader.png"));
	
	mainPanel.add(headerLabel);
	
	JTextPane textPane = new JTextPane();
	textPane.setContentType("text/html");
	textPane.setText(changeLog);
	textPane.setEditable(false);
	
	JScrollPane scrollPane = new JScrollPane(textPane);
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setPreferredSize(new Dimension((int)headerLabel.getWidth()-20, 200));
	scrollPane.setBorder(new EmptyBorder(10,10,10,10));
	scrollPane.setMinimumSize(scrollPane.getPreferredSize());
	scrollPane.setBackground(mainPanel.getBackground());
	
	mainPanel.add(scrollPane);
	
	JLabel downloadNowLabel = new JLabel();
	downloadNowLabel.setIcon(getIconFromPath("images/ChangeLogDialog/InstallNow.png"));
	
	mainPanel.add(downloadNowLabel);
	
	this.yesButtonIcon = new ImageIcon[] {this.getIconFromPath("images/ChangeLogDialog/YesButton.png", 0, 0, 130, 33), this.getIconFromPath("images/ChangeLogDialog/YesButton.png", 0, 33, 130, 33)};
	this.noButtonIcon = new ImageIcon[] {this.getIconFromPath("images/ChangeLogDialog/NoButton.png", 0, 0, 130, 33), this.getIconFromPath("images/ChangeLogDialog/NoButton.png", 0, 33, 130, 33)};
	
	this.yesButtonLabel = new JLabel();
	this.yesButtonLabel.setIcon(this.yesButtonIcon[0]);
	this.yesButtonLabel.setBackground(mainPanel.getBackground());
	this.yesButtonLabel.setBorder(new EmptyBorder(10,10,10,10));
	this.yesButtonLabel.addMouseListener(this);
	
	this.noButtonLabel = new JLabel();
	this.noButtonLabel.setIcon(this.noButtonIcon[0]);
	this.noButtonLabel.setBackground(mainPanel.getBackground());
	this.noButtonLabel.setBorder(new EmptyBorder(10,10,10,10));
	this.noButtonLabel.addMouseListener(this);
	
	JPanel buttonPanel = new JPanel(new GridLayout(1,2));
	buttonPanel.setPreferredSize(new Dimension((int)headerLabel.getWidth(), 53));
	buttonPanel.setMinimumSize(buttonPanel.getPreferredSize());
	buttonPanel.setBackground(mainPanel.getBackground());
	buttonPanel.add(this.yesButtonLabel);
	buttonPanel.add(this.noButtonLabel);
	
	mainPanel.add(buttonPanel);
	
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
		AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.", LevelEnum.SEVERE);
		userWantsUpdate = false;
		userReplied = true;
		dispose();
		return null;
	    }
	    img = ImageIO.read(resource);
	    return new ImageIcon(img);
	}
	catch(IOException e)
	{
	    AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.", LevelEnum.SEVERE);
	    userWantsUpdate = false;
	    userReplied = true;
	    dispose();
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
		AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.", LevelEnum.SEVERE);
		userWantsUpdate = false;
		userReplied = true;
		dispose();
		return null;
	    }
	    img = ImageIO.read(resource);
	    Image image = createImage(new FilteredImageSource(img.getSource(),
		    new CropImageFilter(u, v, w, h)));
	    return new ImageIcon(image);
	}
	catch(IOException e)
	{
	    AutoUpdater.logMessage("AutoUpdater could not start. The program may be corrupted.", LevelEnum.SEVERE);
	    userWantsUpdate = false;
	    userReplied = true;
	    dispose();
	    return null;
	}
    }
    
    public boolean askUserToUpdate()
    {
	while(this.userReplied == false)
	{
	    try
	    {
		Thread.sleep(10);
	    }
	    catch(InterruptedException e)
	    {
		e.printStackTrace();
	    }
	    continue;
	}
	return this.userWantsUpdate;
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
	if(arg0.getSource().equals(this.yesButtonLabel))
	{
	    this.yesButtonLabel.setIcon(this.yesButtonIcon[1]);
	}
	else if(arg0.getSource().equals(this.noButtonLabel))
	{
	    this.noButtonLabel.setIcon(this.noButtonIcon[1]);
	}
    }
    
    @Override
    public void mouseReleased(MouseEvent arg0)
    {
	if(arg0.getSource().equals(this.yesButtonLabel))
	{
	    this.userWantsUpdate = true;
	    this.userReplied = true;
	    this.dispose();
	}
	else if(arg0.getSource().equals(this.noButtonLabel))
	{
	    this.userWantsUpdate = false;
	    this.userReplied = true;
	    this.dispose();
	}
	this.yesButtonLabel.setIcon(this.yesButtonIcon[0]);
	this.noButtonLabel.setIcon(this.noButtonIcon[0]);
    }
    
}
