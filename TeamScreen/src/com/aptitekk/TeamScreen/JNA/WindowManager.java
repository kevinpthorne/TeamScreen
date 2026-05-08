package com.aptitekk.TeamScreen.JNA;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.List;

import com.aptitekk.TeamScreen.Util.WindowInfo;

public abstract class WindowManager {
	
	public abstract List<WindowInfo> getWindows();
	public abstract WindowInfo getTopWindow();
	
	public abstract int[] getWindowRect(int hwnd);
	public abstract Rectangle getDisplayRectangle(int hwnd);
	public abstract WindowInfo getWindowbyTitle(String windowTitle) throws WindowNotFoundException;
	
	public abstract WindowInfo updateWindow(WindowInfo w) throws WindowNotFoundException;
	
	public abstract void bringWindowToFront(WindowInfo w) throws WindowNotFoundException;
	
	public BufferedImage captureWindow(WindowInfo w) throws AWTException {
		return new Robot().createScreenCapture(WindowInfo.convertArrayToRectangle(getWindowRect(w.hwnd)));
	}

	
	@SuppressWarnings("serial")
	public static class WindowNotFoundException extends Exception {
		public WindowNotFoundException(String className, String windowName) {
			super(String.format("Window null for className: %s; windowName: %s", 
					className, windowName));
		}
	}

	@SuppressWarnings("serial")
	public static class GetWindowRectException extends Exception {
		public GetWindowRectException(String windowName) {
			super("Window Rect not found for " + windowName);
		}
	}

}
