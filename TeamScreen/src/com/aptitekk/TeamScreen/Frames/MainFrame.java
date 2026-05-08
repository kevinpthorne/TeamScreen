package com.aptitekk.TeamScreen.Frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.aptitekk.TeamScreen.FolderManager;
import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Util.ImageButton;

/**
 * Main GUI, it'll kinda be like synergy's with simpler, nicer look
 *
 * @author kevint.
 *         Created Apr 3, 2014.
 */
public class MainFrame extends JFrame implements ActionListener, MouseListener{
	
	final int iconSize = 32;
	
	TeamScreenDaemon master;
	static Point mouseDownCompCoords;
	
	JPanel mainPane;
	
	ImageButton sharePane;
	ImageButton settingsPane;
	ImageButton joinPane;
	ImageButton leavePane;
	ImageButton pinPane;
	ImageButton exitPane;
	
	boolean pinned = false;
	boolean inMeeting = false;
	
	public MainFrame(TeamScreenDaemon master) {
		this.master = master;
		
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TeamScreen");
		this.setTitle("TeamScreen");
		this.setMaximumSize(new Dimension((int) (iconSize*7.5), iconSize+6));
		this.setMinimumSize(new Dimension((int) (iconSize*7.5), iconSize+6));
		setResizable(false);
		setLocationRelativeTo(null);

		this.setIconImage(FolderManager.createImage("/images/TeamScreen-64px.png", "tray icon"));
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Already there
	    
	    mouseDownCompCoords = null;
        this.setUndecorated(true);
        this.setVisible(true);
        this.setBounds(0, 0, 480, 70);
        this.setAlwaysOnTop(pinned);

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
	    
		initGui();
		
		this.getContentPane().setBackground(Color.WHITE);
		((JPanel) this.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(0, 120, 255)),
				new EmptyBorder(10, 10, 10, 10)));
		this.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
				((JPanel) getContentPane()).setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(0, 120, 255)),
						new EmptyBorder(10, 10, 10, 10)));
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				if(!pinned)
					((JPanel) getContentPane()).setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(138, 138, 138)),
							new EmptyBorder(10, 10, 10, 10)));
			}
			
		});
		
		this.pack();
	}
	
	private void initGui() {
		mainPane = new JPanel(new FlowLayout());
		mainPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		Image logo = FolderManager.createImage("/images/TeamScreen-64px.png", "logo icon");
		logo = logo.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		JLabel logoPane = new JLabel(new ImageIcon(logo));
		mainPane.add(logoPane);
		
		Image join = FolderManager.createImage("/images/join-enabled.png", "join icon");
		join = join.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		joinPane = new ImageButton(new ImageIcon(join), "Join");
		mainPane.add(joinPane.getIconPane());
		Image leave = FolderManager.createImage("/images/leave-disabled.png", "leave icon");
		leave = leave.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		leavePane = new ImageButton(new ImageIcon(leave), false, "Leave");
		mainPane.add(leavePane.getIconPane());
		
		//space
		mainPane.add(Box.createHorizontalStrut(125));
		
		Image share = FolderManager.createImage("/images/share-enabled.png", "share icon");
		share = share.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		sharePane = new ImageButton(new ImageIcon(share), "Share");
		mainPane.add(sharePane.getIconPane());
		
		Image setting=FolderManager.createImage("/images/settings.png", "settings icon");
		setting = setting.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		settingsPane = new ImageButton(new ImageIcon(setting), "Settings");
		mainPane.add(settingsPane.getIconPane());
		
		Image pin;
		if(pinned)
			pin = FolderManager.createImage("/images/pinned.png", "pinned icon");
		else
			pin = FolderManager.createImage("/images/pin.png", "pin icon");
		pin = pin.getScaledInstance((iconSize/2), (iconSize/2), Image.SCALE_SMOOTH);
		pinPane = new ImageButton(new ImageIcon(pin), "Pin");
		mainPane.add(pinPane.getIconPane());
		
		Image exit = FolderManager.createImage("/images/exit.png", "exit icon");
		exit = exit.getScaledInstance((iconSize/2), (iconSize/2), Image.SCALE_SMOOTH);
		exitPane = new ImageButton(new ImageIcon(exit), "Exit");
		mainPane.add(exitPane.getIconPane());
		
		joinPane.getIconPane().addMouseListener(this);
		leavePane.getIconPane().addMouseListener(this);
		sharePane.getIconPane().addMouseListener(this);
		settingsPane.getIconPane().addMouseListener(this);
		pinPane.getIconPane().addMouseListener(this);
		exitPane.getIconPane().addMouseListener(this);
		
		this.add(mainPane);
	}
	
	public void updateStatus() {
		if(this.master.isEnabled()) {
			//disable buttons
			this.repaint();
		} else {
			this.repaint();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/*if(e.getSource().equals(this.enableToggle)) {
			if(this.master.isEnabled()) {
				this.master.disable();
			} else {
				this.master.enable();
			}
		}*/
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource().equals(joinPane.getIconPane())) {
			if(joinPane.getEnabled())
				JOptionPane.showMessageDialog(this, "Meetings haven't been implemented yet!", "Unimplemented Error",
						JOptionPane.ERROR_MESSAGE);
		} else if (e.getSource().equals(leavePane.getIconPane())) {
			if(leavePane.getEnabled())
				JOptionPane.showMessageDialog(this, "Meetings haven't been implemented yet!", "Unimplemented Error",
						JOptionPane.ERROR_MESSAGE);
		} else if(e.getSource().equals(exitPane.getIconPane())) {
			this.dispose();
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} else if(e.getSource().equals(pinPane.getIconPane())) {
			if(pinned) {
				pinned = false;
				Image pin = FolderManager.createImage("/images/pin.png", "pin icon");
				pin = pin.getScaledInstance((iconSize/2), (iconSize/2), Image.SCALE_SMOOTH);
				pinPane.getIconPane().setIcon(new ImageIcon(pin));
				this.repaint();
			} else {
				pinned = true;
				Image pin = FolderManager.createImage("/images/pinned.png", "pinned icon");
				pin = pin.getScaledInstance((iconSize/2), (iconSize/2), Image.SCALE_SMOOTH);
				pinPane.getIconPane().setIcon(new ImageIcon(pin));
				this.repaint();
			}
			this.setAlwaysOnTop(pinned);
		} else if(e.getSource().equals(settingsPane.getIconPane())) {
			final MainFrame instance = this;
			SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                	master.openSettingsWindow();
                }
            });
		} else if(e.getSource().equals(sharePane.getIconPane())) {
			if(!inMeeting) {
				SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		                master.openWindowSelect();
		            }
		        });
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	

}
