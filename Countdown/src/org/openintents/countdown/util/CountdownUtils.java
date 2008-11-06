package org.openintents.countdown.util;

public class CountdownUtils {

	/**
	 * Given a duration in milliseconds, returns a string in
	 * hours, minutes, and seconds.
	 * 
	 * @param duration in milliseconds
	 * @return
	 */
	public static String getDurationString(long duration) {
		int seconds = (int) (duration / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int hours = minutes / 60;
		minutes = minutes % 60;
		
		return "" + hours + ":" 
			+ (minutes < 10 ? "0" : "")
			+ minutes + ":"
			+ (seconds < 10 ? "0" : "")
			+ seconds;
	}

}
