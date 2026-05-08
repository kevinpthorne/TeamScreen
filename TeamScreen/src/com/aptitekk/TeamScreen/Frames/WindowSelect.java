package com.aptitekk.TeamScreen.Frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Frames.Util.WindowUtil;
import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * This is the window that appears to ask what window you want to select
 *
 * @author kevint.
 *         Created Apr 5, 2014.
 */
public class WindowSelect extends JFrame implements MouseListener, KeyListener{

	TeamScreenDaemon master;
	
	boolean translucencySupported = false;
	
	public WindowSelect(TeamScreenDaemon master) {
		
		this.master = master;
		
		if(TeamScreenDaemon.getWindowManager().getWindows().isEmpty()) {
			JOptionPane.showMessageDialog(null,
				    "No windows were found",
				    "No Windows Found",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Already there
	    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    this.setUndecorated(true);
	    
	    try {
	    	this.setOpacity(0.55f);
	    } catch (UnsupportedOperationException e) {
	    	if(e.getMessage().contains("translucency"))
	    		translucencySupported = false;
	    	else
	    		e.printStackTrace();
	    }
	    
	    this.setBackground(Color.BLACK);
	    this.setGlassPane(new JPanel());
	    this.getGlassPane().setVisible(true);
	    
	    initGui();
	    
	    this.pack();
	    this.setVisible(true);
	    this.addMouseListener(this);
	    this.addKeyListener(this);
	}
	
	private void initGui() {
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		
		Squares squaresPanel = new Squares();
		
		if(!translucencySupported) {
			for(WindowInfo w : TeamScreenDaemon.getWindowManager().getWindows()) {
				try {
					if(w == null)
						throw new NullPointerException("WindowInfo list returned entry is null");
					if(!w.getTitle().equals(""))
						squaresPanel.addSquare(w.getRelativeRectangle().x, w.getRelativeRectangle().y, 
								w.getRelativeRectangle().x + w.getRelativeRectangle().width, 
								w.getRelativeRectangle().y + w.getRelativeRectangle().height);
				} catch (NullPointerException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
						    e.getMessage() + "\n Length: "+TeamScreenDaemon.getWindowManager().getWindows().size(),
						    "NullPointerException",
						    JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		}
		this.add(squaresPanel);
		
		JPanel pane = new JPanel(new BorderLayout());
		JLabel infoText = new JLabel("<html><p style='font-size:40px;height=50%;width=50%;'>Select window to share.</p></html>");
		
		pane.add(infoText);
		
		((JPanel)this.getGlassPane()).add(pane);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//window selection jazz here
		Point where = e.getLocationOnScreen();
		int x = where.x;
		int y = where.y;
		for(WindowInfo w : TeamScreenDaemon.getWindowManager().getWindows()) {
			System.out.println(w.toString());
			//check if point intersects with any window, and remove other crap
			if(!w.getTitle().equals("")) {
				//if(WindowUtil.getTopWindow().equals(w)) {
					if(w.getRelativeRectangle().x <= x && w.getRelativeRectangle().x + w.getRelativeRectangle().width >= x) {
						if(w.getRelativeRectangle().y <= y && w.getRelativeRectangle().y + w.getRelativeRectangle().height >= y) {
							master.openPushSelect(w);
							System.out.println(w.getTitle());
							break;
						}
					} else {
						System.out.println("None found");
					}
				//}
			}
			
			/*for(int c : WindowUtil.getWindowRect(w.getTitle())) {
				if(w.getTitle() == null) {
					//pass
				} else if(w.getTitle().equals("")) {
					//pass
				} else {
					System.out.println("getWindowRect("+w.getTitle()+"): " + c);
				}
				
			}
			for(int c : w.getRect()) {
				if(w.getTitle() == null) {
					//pass
				} else if(w.getTitle().equals("")) {
						//pass
				} else {
					System.out.println("getRect("+w.getTitle()+"): " + c);
				}
				
			}*/
			
		}
		
		this.dispose();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//this.setVisible(false);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
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
	public void keyTyped(KeyEvent arg0) {;}

	class Squares extends JPanel {
		private static final int PREF_W = 500;
		private static final int PREF_H = PREF_W;
		private List<Rectangle> squares = new ArrayList<Rectangle>();

		public void addSquare(int x, int y, int width, int height) {
			Rectangle rect = new Rectangle(x, y, width, height);
			squares.add(rect);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(PREF_W, PREF_H);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			for (Rectangle rect : squares) {
				g2.draw(rect);
			}
		}

	}
}
