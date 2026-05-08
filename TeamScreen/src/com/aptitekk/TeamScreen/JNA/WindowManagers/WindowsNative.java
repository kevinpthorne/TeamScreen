package com.aptitekk.TeamScreen.JNA.WindowManagers;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aptitekk.TeamScreen.JNA.WindowManager;
import com.aptitekk.TeamScreen.JNA.WindowManager.GetWindowRectException;
import com.aptitekk.TeamScreen.JNA.WindowManager.WindowNotFoundException;
import com.aptitekk.TeamScreen.Util.WindowInfo;
import com.aptitekk.TeamScreen.Util.WindowInfo.RECT;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class WindowsNative extends WindowManager {
	
	private static final int WIDTH_PADDING = 10;
	private static final int HEIGHT_PADDING = 10;

	@Override
	public List<WindowInfo> getWindows() {
		final List<WindowInfo> inflList = new ArrayList<WindowInfo>();
		final List<Integer> order = new ArrayList<Integer>();
		int top = User32.instance.GetTopWindow(0);
		while (top != 0) {
			order.add(top);
			top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
		}
		User32.instance.EnumWindows(new WndEnumProc() {
			@Override
			public boolean callback(int hWnd, int lParam) {
				if (User32.instance.IsWindowVisible(hWnd)) {
					RECT r = new RECT();
					User32.instance.GetWindowRect(hWnd, r);
					if (r.left > -32000) { // minimized
						byte[] buffer = new byte[1024];
						User32.instance.GetWindowTextA(hWnd, buffer,
								buffer.length);
						String title = Native.toString(buffer);
						inflList.add(new WindowInfo(hWnd, title));
					}
				}
				return true;
			}
		}, 0);
		Collections.sort(inflList, new Comparator<WindowInfo>() {
			@Override
			public int compare(WindowInfo o1, WindowInfo o2) {
				return order.indexOf(o1.hwnd) - order.indexOf(o2.hwnd);
			}
		});
		return inflList;
	}

	@Override
	public WindowInfo getTopWindow() {
		final List<WindowInfo> topWindowList = new ArrayList<WindowInfo>();
		int top = User32.instance.GetTopWindow(0);
		User32.instance.EnumWindows(new WndEnumProc() {
			@Override
			public boolean callback(int hWnd, int lParam) {
				if (User32.instance.IsWindowVisible(hWnd)) {
					RECT r = new RECT();
					User32.instance.GetWindowRect(hWnd, r);
					if (r.left > -32000) { // minimized
						byte[] buffer = new byte[1024];
						User32.instance.GetWindowTextA(hWnd, buffer,
								buffer.length);
						String title = Native.toString(buffer);
						topWindowList.add(new WindowInfo(hWnd, title));
					}
				}
				return true;
			}
		}, 0);
		return topWindowList.get(0);
	}

	public WindowInfo getWindowbyTitle(String windowTitle)
			throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for (WindowInfo window : windows) {
			if (window.getTitle().equals(windowTitle)) {
				return window;
			}
		}
		throw new WindowNotFoundException("", windowTitle);
	}

	private int[] getRect(int hwnd) throws WindowNotFoundException,
			GetWindowRectException {
		RECT r = new RECT();
		int result = User32.instance.GetWindowRect(hwnd, r);
		if (result == 0) {
			throw new GetWindowRectException("" + hwnd);
		}
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
	
	public Rectangle getDisplayRectangle(int hwnd) {
		WinDef.RECT bounds = new WinDef.RECT();
		User32.instance.GetClientRect(hwnd, bounds);

        int width = bounds.right - bounds.left + WIDTH_PADDING;
        int height = bounds.bottom - bounds.top + HEIGHT_PADDING;
        
        return new Rectangle(bounds.top, bounds.left, width, height);
	}
	
	@Override
	public BufferedImage captureWindow(WindowInfo w) {
		int hwnd = w.hwnd;
		
		HDC hdcWindow = User32.instance.GetWindowDC(hwnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
		
		WinDef.RECT bounds = new WinDef.RECT();
		User32.instance.GetClientRect(hwnd, bounds);

        int width = bounds.right - bounds.left + WIDTH_PADDING;
        int height = bounds.bottom - bounds.top + HEIGHT_PADDING;

        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);

        HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);

        BITMAPINFO bmi = new BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory(width * height * 4);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        User32.instance.ReleaseDC(hwnd, hdcWindow);

        return image;
	}

	@Override
	public synchronized WindowInfo updateWindow(WindowInfo w)
			throws WindowNotFoundException {
		List<WindowInfo> windows = getWindows();
		for (WindowInfo window : windows) {
			if (window.hwnd == w.hwnd) {
				return window;
			}
		}
		throw new WindowNotFoundException("", w.getTitle());
	}

	static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
		boolean callback(int hWnd, int lParam);
	}

	static interface User32 extends StdCallLibrary {
		final User32 instance = (User32) Native.loadLibrary("user32",
				User32.class);
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
				W32APIOptions.DEFAULT_OPTIONS);

		boolean EnumWindows(WndEnumProc wndenumproc, int lParam);

		boolean IsWindowVisible(int hWnd);

		int GetWindowRect(int hWnd, RECT r);

		void GetWindowTextA(int hWnd, byte[] buffer, int buflen);

		int GetTopWindow(int hWnd);

		int GetWindow(int hWnd, int flag);

		boolean SetForegroundWindow(int hWnd);

		HWND FindWindow(String lpClassName, String lpWindowName);

		int GetWindowRect(HWND handle, int[] rect);
		
		public HDC GetWindowDC(int hWnd);

	    public boolean GetClientRect(int hWnd, WinDef.RECT rect);
	    
	    int ReleaseDC(int hWnd, HDC hdcWindow);
	    
		final int GW_HWNDNEXT = 2;
	}

	static interface WinGDIExtra extends WinGDI {
		public DWORD SRCCOPY = new DWORD(0x00CC0020);
	}

	static interface GDI32Extra extends GDI32 {

		GDI32Extra INSTANCE = (GDI32Extra) Native.loadLibrary("gdi32",
				GDI32Extra.class, W32APIOptions.DEFAULT_OPTIONS);

		public boolean BitBlt(HDC hObject, int nXDest, int nYDest, int nWidth,
				int nHeight, HDC hObjectSource, int nXSrc, int nYSrc,
				DWORD dwRop);

	}

	@Override
	public void bringWindowToFront(WindowInfo w) {
		User32.instance.SetForegroundWindow(w.hwnd);
	}

}
