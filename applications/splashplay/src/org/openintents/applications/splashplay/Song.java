package org.openintents.applications.splashplay;

/**
 * Holds information about a song.
 * 
 * This currently includes chords and times when to play them.
 * 
 * @author Peli
 *
 */
public class Song {

	/** Chords in the song */
	public Chord[] chords;
	
	/** Maximum number of elements in time and events */
	public int mMax;
	
	/** Pointer to current event */
	public int mCur;
	
	/** Time when events happen */
	public int[] times;
	
	/** Events corresponding to time */
	public Event[] events;
	
	
	public Song() {
		chords = new Chord[3];
		chords[0] = new Chord("G", 3, 0, 0, 0, 2, 3);
		chords[1] = new Chord("D", 2, 3, 2, 0, 0, -1);
		chords[2] = new Chord("C", 0, 1, 0, 2, 3, 0);
		
		
		// Some shortcuts:
		Chord G = chords[0];
		Chord D = chords[1];
		Chord C = chords[2];
		
		Chord[] progression = new Chord[] {
				G, G, G, G, C, C, G, G, D, C, G, G,
				G, G, G, G, C, C, G, G, D, D, G, G};
		mMax = progression.length;
		
		events = new Event[mMax];
		times = new int[mMax];
		
		int duration = 41000; // song duration in ms
		for (int i=0; i<mMax; i++) {
			times[i] = i * duration / mMax;
			events[i] = new Event(progression[i]);
		}
	}
	
	/** 
	 * Returns time of next event.
	 * @return time of next event in ms.
	 */
	int getNextTime() {
		if (mCur < mMax - 1) {
			return times[mCur + 1];
		} else {
			// At the end, for now let us just return 
			// last element.
			// (This logic may be changed in the future).
			return times[mMax - 1];
		}
	}
	
	/**
	 * Returns current event.
	 * @return Event at current position.
	 */
	Event getEvent() {
		return events[mCur];
	}
	
	/** Sets the current position to the time.
	 * 
	 * Optimization condition: Quickly return
	 * if time corresponds to current position
	 * or the position thereafter.
	 * 
	 * @param time Time in ms. */
	void setTime(int time) {
		if (time < times[mCur]) {
			// Let's start to search from 
			// the beginning of the song:
			mCur = 0;
			
			// One could now invoke a binary search
			// to speed things up.
		}
		
		// for now, we search element by element.
		// (this is faster than binary search if we
		//  expect the element to be one of the 
		//  neighboring elements).
		// while current position pointer is still behind time
		while (mCur < mMax - 1 && time >= times[mCur + 1]) {
			// advance current position.
			mCur++;
		}
		// now we have found current position
		// or we are at final position.
	}
	
}
