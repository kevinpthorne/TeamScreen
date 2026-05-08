package com.aptitekk.TeamScreen.Util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

/**
 * Duplicates log entries into a JTextArea given
 *
 * @author kevint.
 *         Created Feb 23, 2014.
 */
public class SwingHandler extends Handler{
	
	private JTextArea destination;
	
	public SwingHandler(JTextArea textarea) {
		super();
		this.destination = textarea;
		this.setFormatter(new LogFormat());
	}

	@Override
	public void close() throws SecurityException {
		//nothing!
	}

	@Override
	public void flush() {
		this.destination.setText(null);
		
	}

	@Override
	public void publish(LogRecord record) {
		this.destination.append(this.getFormatter().format(record));
		this.destination.setCaretPosition(this.destination.getText().length());
		
	}

}
