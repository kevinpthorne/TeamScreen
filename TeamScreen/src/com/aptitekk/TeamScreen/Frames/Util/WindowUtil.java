package com.aptitekk.TeamScreen.Frames.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aptitekk.TeamScreen.Util.WindowInfo;
import com.aptitekk.TeamScreen.Util.WindowInfo.RECT;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Allows Java with JNA to get all open windows and a specified window rectangle
 *
 * @author kevint.
 *         Created Apr 6, 2014.
 */
@Deprecated
public class WindowUtil {

	public static List<WindowInfo> getWindows() {
		final List<WindowInfo> inflList = new ArrayList<WindowInfo>();
		final List<Integer> order = new ArrayList<Integer>();
		int top = User32.instance.GetTopWindow(0);
		while (top!=0) {
			order.add(top);
			top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
		}
		User32.instance.EnumWindows(new WndEnumProc()
		{
			@Override
			public boolean callback(int hWnd, int lParam)
			{
				if (User32.instance.IsWindowVisible(hWnd)) {
					RECT r = new RECT();
					User32.instance.GetWindowRect(hWnd, r);
					if (r.left>-32000) {     // minimized
						byte[] buffer = new byte[1024];
						User32.instance.GetWindowTextA(hWnd, buffer, buffer.length);
						String title = Native.toString(buffer);
						inflList.add(new WindowInfo(hWnd, title));
					}
				}
				return true;
			}
		}, 0);
		Collections.sort(inflList, new Comparator<WindowInfo>()
				{
			@Override
			public int compare(WindowInfo o1, WindowInfo o2) {
				return order.indexOf(o1.hwnd)-order.indexOf(o2.hwnd);
			}
				});
		return inflList;
	}

	public static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
		boolean callback (int hWnd, int lParam);
	}

	public static interface User32 extends StdCallLibrary
	{
		final User32 instance = (User32) Native.loadLibrary ("user32", User32.class);
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
	               W32APIOptions.DEFAULT_OPTIONS);
		boolean EnumWindows (WndEnumProc wndenumproc, int lParam);
		boolean IsWindowVisible(int hWnd);
		int GetWindowRect(int hWnd, RECT r);
		void GetWindowTextA(int hWnd, byte[] buffer, int buflen);
		int GetTopWindow(int hWnd);
		int GetWindow(int hWnd, int flag);
		
		HWND FindWindow(String lpClassName, String lpWindowName);
	    int GetWindowRect(HWND handle, int[] rect);
	    
		final int GW_HWNDNEXT = 2;
	}
	
	public static WindowInfo getTopWindow() {
		final List<WindowInfo> topWindowList = new ArrayList<WindowInfo>();
		int top = User32.instance.GetTopWindow(0);
		User32.instance.EnumWindows(new WndEnumProc()
		{
			@Override
			public boolean callback(int hWnd, int lParam)
			{
				if (User32.instance.IsWindowVisible(hWnd)) {
					RECT r = new RECT();
					User32.instance.GetWindowRect(hWnd, r);
					if (r.left>-32000) {     // minimized
						byte[] buffer = new byte[1024];
						User32.instance.GetWindowTextA(hWnd, buffer, buffer.length);
						String title = Native.toString(buffer);
						topWindowList.add(new WindowInfo(hWnd, title));
					}
				}
				return true;
			}
		}, 0);
		return topWindowList.get(0);
	}
	
	public static WindowInfo getWindowbyTitle(String windowTitle) throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for(WindowInfo window : windows) {
			if(window.getTitle().equals(windowTitle)) {
				return window;
			}
		}
		throw new WindowNotFoundException("", windowTitle);
	}

	private static int[] getRect(String windowName) throws WindowNotFoundException,
	GetWindowRectException {
		int hwnd = -1;
		List<WindowInfo> windows = getWindows();
		for(WindowInfo window : windows) {
			if(window.getTitle().equals(windowName)) {
				hwnd = window.hwnd;
			}
		}
		if(hwnd == -1) {
			throw new WindowNotFoundException("", windowName);
		}
		RECT r = new RECT();
		int result = User32.instance.GetWindowRect(hwnd, r);
		if(result == 0) {
			throw new GetWindowRectException(windowName);
		}
		return r.asArray();
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

	public static int[] getWindowRect(String windowName) {
		int[] rect;
		try {
			rect = getRect(windowName);
			return rect;
		} catch (WindowNotFoundException e) {
			e.printStackTrace();
		} catch (GetWindowRectException e) {
			e.printStackTrace();
		}      
		return null;
	}
	
	public synchronized static WindowInfo updateWindow(WindowInfo w) throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for(WindowInfo window : windows) {
			if(window.hwnd == w.hwnd) {
				return window;
			}
		}
		throw new WindowNotFoundException("", w.getTitle());
	}

}
