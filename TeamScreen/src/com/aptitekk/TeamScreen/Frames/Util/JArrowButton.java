package com.aptitekk.TeamScreen.Frames.Util;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.aptitekk.TeamScreen.FolderManager;

/**
 * The cute little buttons for pushing windows on the last overlay dialog
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class JArrowButton extends JButton{
	
	public static final int SPECIAL = 0;
	public static final int NORTH = 1;
	public static final int SOUTH = 2;
	public static final int EAST = 3;
	public static final int WEST = 4;
	public static final int X = 5;
	
	public JArrowButton(int direction) {
		super(getImagefromDirection(direction));
		
		this.setOpaque(true);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setContentAreaFilled(false);
		
	}
	
	public static ImageIcon getImagefromDirection(int direction) {
		if(direction == NORTH) {
			return new ImageIcon(FolderManager.createImage("/images/arrownorth.png", "north arrow"));
		} else if (direction == SOUTH) {
			return new ImageIcon(FolderManager.createImage("/images/arrowsouth.png", "south arrow"));
		} else if (direction == EAST) {
			return new ImageIcon(FolderManager.createImage("/images/arroweast.png", "east arrow"));
		} else if (direction == WEST) {
			return new ImageIcon(FolderManager.createImage("/images/arrowwest.png", "west arrow"));
		} else if(direction == SPECIAL) {
			return new ImageIcon(FolderManager.createImage("/images/arrowdisplay.png", "special arrow"));
		} else if(direction == X) {
			return new ImageIcon(FolderManager.createImage("/images/x.png", "exit arrow"));
		}
		else {
			return null;
		}
	}

}
