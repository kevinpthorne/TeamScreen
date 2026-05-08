package com.aptitekk.TeamScreen.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats the logging output
 *
 * @author kevint.
 *         Created Feb 23, 2014.
 */
public class LogFormat extends Formatter{

	@Override
	public synchronized String format(LogRecord log) {
		StringBuilder finallog = new StringBuilder();

		java.util.Date date= new java.util.Date();
		
		finallog.append("[");
		
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatted_date = DATE_FORMAT.format(date);
		
		finallog.append(formatted_date)
			.append("]")
            .append(" ");
		
		String message = formatMessage(log);
		
		// Level
	    finallog.append(log.getLevel().getLocalizedName());
	    finallog.append(": ");

	    // Indent - the more serious, the more indented.
	    //sb.append( String.format("% ""s") );
	    int iOffset = (1000 - log.getLevel().intValue()) / 100;
	    for( int i = 0; i < iOffset;  i++ ){
	    	finallog.append(" ");
	    }

	    finallog.append(message);
	    finallog.append(System.getProperty("line.separator"));

        if (log.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                log.getThrown().printStackTrace(pw);
                pw.close();
                finallog.append(sw.toString());
            } catch (Exception ex) {
                // ignore
            }
        }

        return finallog.toString();
	}

}
