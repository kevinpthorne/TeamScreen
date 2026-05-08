package com.aptitekk.TeamScreen.JNA.WindowManagers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.aptitekk.TeamScreen.JNA.WindowManager;
import com.aptitekk.TeamScreen.JNA.WindowManager.GetWindowRectException;
import com.aptitekk.TeamScreen.JNA.WindowManager.WindowNotFoundException;
import com.aptitekk.TeamScreen.JNA.WindowManagers.X.X11Exception;
import com.aptitekk.TeamScreen.Util.WindowInfo;
import com.aptitekk.TeamScreen.Util.WindowInfo.RECT;
import com.sun.jna.Library;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.WindowByReference;
import com.sun.jna.platform.unix.X11.XWindowAttributes;
import com.sun.jna.ptr.IntByReference;

public class X11Native extends WindowManager {
	
	private X.Display display = new X.Display();

	@Override
	public List<WindowInfo> getWindows() {
		final List<WindowInfo> inflList = new ArrayList<WindowInfo>();

		com.aptitekk.TeamScreen.JNA.WindowManagers.X.Window[] windowList = null;
		try {
			windowList = display.getWindows();
		} catch (X11Exception e) {
			System.err.println("Could not retrieve windows");
			e.printStackTrace();
			return null;
		}
        
        for(X.Window win : windowList) {
        	inflList.add(getWindow(win));
        }

        return inflList;
	}
	private WindowInfo getWindow(int hwnd) {
		return getWindow(new X.Window(display, hwnd));
	}
	private WindowInfo getWindow(X.Window window) {
		String wName = null;
		try {
			wName = window.getTitle();
		} catch (X11Exception e) {
			e.printStackTrace();
		}
		if(wName == null) {
			System.err.println("Window Title null!");
			return null;
		}
	    XWindowAttributes wInfo = new XWindowAttributes();
	    IntByReference x = null, y = null;
	    WindowByReference child = null;
    	int status = X11Ext.instance.XGetWindowAttributes(display.getX11Display(), window.getX11Window(), wInfo);
    	boolean status_= X11Ext.instance.XTranslateCoordinates(display.getX11Display(), window.getX11Window(),
    			display.getRootWindow().getX11Window(), 0, 0, x, y, child);
		WindowInfo.RECT rect = new WindowInfo.RECT();
		rect.top = y.getValue() - wInfo.y;
		rect.bottom = (y.getValue() - wInfo.y)+wInfo.height;
		rect.left = x.getValue() - wInfo.x;
		rect.right= (x.getValue() - wInfo.x)+wInfo.width;
		return new WindowInfo(window.getID(),
				wName);
	}
	
	
	@Override
	public WindowInfo getTopWindow() {
		return getWindows().get(getWindows().size()-1);
	}
	
	public WindowInfo getWindowbyTitle(String windowTitle) throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for(WindowInfo window : windows) {
			if(window.getTitle().equals(windowTitle)) {
				return window;
			}
		}
		throw new WindowNotFoundException("", windowTitle);
	}

	private int[] getRect(int hwnd) throws WindowNotFoundException,
	GetWindowRectException {
		RECT r = new RECT();
		X11.XWindowAttributes windowAttrib = new XWindowAttributes();
		int result = X11Ext.instance.XGetWindowAttributes(X11Ext.x11Display, new X11.Window(hwnd), windowAttrib);
		if(result == 0) {
			throw new GetWindowRectException("" + hwnd);
		}
		r.top = windowAttrib.y;
		r.bottom = windowAttrib.y+windowAttrib.height;
		r.left = windowAttrib.x;
		r.right= windowAttrib.x+windowAttrib.width;
		return r.asArray();
	}

	public int[] getWindowRect(int hwnd) {
		int[] rect;
		try {
			rect = getRect(hwnd);
			return rect;
		} catch (WindowNotFoundException e) {
			e.printStackTrace();
		} catch (GetWindowRectException e) {
			e.printStackTrace();
		}      
		return null;
	}
	
	@Override
	public synchronized WindowInfo updateWindow(WindowInfo w) throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for(WindowInfo window : windows) {
			if(window.getTitle().equals(w.getTitle())) {
				return window;
			}
		}
		throw new WindowNotFoundException("", w.getTitle());
	}
	
	
	static interface X11Ext extends Library
	{
		final X11 instance = X11.INSTANCE;
		X11.Display x11Display = instance.XOpenDisplay(null);
	}


	@Override
	public void bringWindowToFront(WindowInfo w) throws WindowNotFoundException {
		//
	}
	@Override
	public Rectangle getDisplayRectangle(int hwnd) {
		int[] rect = null;
		try {
			rect = getRect(hwnd);
		} catch (WindowNotFoundException | GetWindowRectException e) {
			e.printStackTrace();
		}
		return new Rectangle(rect[0], rect[1], rect[2], rect[3]);
	}
}
