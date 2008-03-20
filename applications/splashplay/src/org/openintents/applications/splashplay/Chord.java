package org.openintents.applications.splashplay;

/**
 * Holds information about a single chord.
 * 
 * Currently this holds information about a guitar chord.
 * Later this could be split into two classes:
 * An abstract base chord class, and derived Guitar chord class.
 * (There could be also a piano chord class.)
 * 
 * @author Peli
 * 
 */
public class Chord {
	/**
	 * Name of the chord.
	 * 
	 * For example, A, Gmin, Cadd2, ...
	 */
	public String name;
	
	/** Markers on the fretboard that show position
	 * where to place fingers.
	 * 
	 * 1, 2, 3, ...: Finger at fret 1, 2, 3...
	 * 0: no finger
	 * MARKER_VOID: String should not sound.
	 */
	public int[] markerList;
	
	public Chord() {
		name = null;
		markerList = null;
	}
	
	/**
	 * Initializes names and strings to be picked.
	 * 
	 * @param name Name of chord.
	 * @param e
	 * @param b
	 * @param g
	 * @param d
	 * @param A
	 * @param lowE
	 */
	public Chord(String name, 
			int e, int b, int g, int d, int A, int lowE) {
		this.name = name;
		this.markerList = new int[] {e, b, g, d, A, lowE};
	}
	
}
