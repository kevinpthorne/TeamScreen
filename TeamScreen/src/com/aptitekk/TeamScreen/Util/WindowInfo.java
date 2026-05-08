package com.aptitekk.TeamScreen.Util;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.X11;


/**
 * Universal class type for window
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
public class WindowInfo {
	
	public static class RECT extends Structure {
		public int left,top,right,bottom;

		@Override
		protected List getFieldOrder() {
			return Arrays.asList(new String[] { "left", "top", "right", "bottom"});
		}
		public int[] asArray() {
			return new int[]{left,top,right,bottom};
		}
	}
	
	public int hwnd;
	private String windowTitle;
	
	public WindowInfo(int hwnd, String windowTitle)
	{ this.hwnd = hwnd; this.windowTitle = windowTitle;}
	
	public String getTitle() {
		return windowTitle;
	}
	
	public Rectangle getAbsRectangle() {
		return TeamScreenDaemon.getWindowManager().getDisplayRectangle(hwnd);
	}
	public Rectangle getRelativeRectangle() {
		return convertArrayToRectangle(TeamScreenDaemon.getWindowManager().getWindowRect(hwnd));
	}
	
	public static Rectangle convertArrayToRectangle(int[] rect) {
		Point topleft = new Point(rect[0], rect[1]);
		Point bottomright = new Point(rect[2], rect[3]);
		return new Rectangle(topleft.x, topleft.y, bottomright.x - topleft.x, bottomright.y - topleft.y);
	}

}
