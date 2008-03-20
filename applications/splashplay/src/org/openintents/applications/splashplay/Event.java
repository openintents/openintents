package org.openintents.applications.splashplay;

/**
 * Event in a song.
 * 
 * Currently only for guitar:
 * Holds chord.
 * Later may hold note or lyrics.
 * 
 * @author Peli
 *
 */
public class Event {
	///** Time of event in ms */
	//public int time;
	
	/** Chord */
	public Chord chord;
	
	/** Creates new event. */
	public Event () {
		//time = 0;
		chord = null;
	}
	
	/** Assigns a chord. */
	public Event (Chord chord) {
		//this.time = time;
		this.chord = chord;
	}
}
