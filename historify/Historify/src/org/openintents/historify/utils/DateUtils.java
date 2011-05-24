package org.openintents.historify.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MMM HH:mm");
	
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}
}
