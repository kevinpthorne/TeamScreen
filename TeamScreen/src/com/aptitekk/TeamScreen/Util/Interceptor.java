package com.aptitekk.TeamScreen.Util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author kevint
 *
 */
public class Interceptor extends PrintStream{
	
	public static final int ERR = 1;
	public static final int OUT = 2;
	private Logger logger;
	private int type;
	
	/**
	 * Intercepts System.out and System.err to the logger
	 * 
	 * @param OutputStream out
	 * @param Logger logger
	 * @param int type
	 */
	public Interceptor(OutputStream out, Logger logger, int type) {
		super(out, true);
		this.logger = logger;
		this.type = type;
	}
	
	public void print(String s) {
		if(type == ERR) {
			logger.severe("INTERCEPTED - " + s);
		} else {
			logger.fine("INTERCEPTED - " + s);
		}
	}

}
