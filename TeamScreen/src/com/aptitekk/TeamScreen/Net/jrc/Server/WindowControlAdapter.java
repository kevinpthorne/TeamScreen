package com.aptitekk.TeamScreen.Net.jrc.Server;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import org.one.stone.soup.remote.control.server.ControlAdapter;

import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * Controls commands for a specific window
 *
 * @author kevint.
 *         Created Apr 7, 2014.
 */
public class WindowControlAdapter implements ControlAdapter {
	
	private Robot robot;
	private WindowInfo window;
	
	public WindowControlAdapter(WindowInfo info)
	{
		this.window = info;
		try{
			this.robot = new Robot();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void keyPress(int code) {
		this.robot.keyPress(code);
	}

	@Override
	public void keyRelease(int code) {
		this.robot.keyRelease(code);
	}

	@Override
	public void mouseMove(int x, int y) {
		this.robot.mouseMove(this.window.getRelativeRectangle().x+x,this.window.getRelativeRectangle().y+y);
	}

	@Override
	public void mousePress(int code) {
		this.robot.mousePress(code);
	}

	@Override
	public void mouseRelease(int code) {
		this.robot.mouseRelease(code);
	}
	
	public void updateWindow(WindowInfo info) {
		this.window = info;
	}
}
