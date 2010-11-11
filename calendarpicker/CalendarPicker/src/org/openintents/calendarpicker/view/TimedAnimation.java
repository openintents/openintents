package org.openintents.calendarpicker.view;

public class TimedAnimation {
	long start_time;
	float duration_ms;
	TimedAnimation(long start_time, float duration_ms) {
		this.start_time = start_time;
		this.duration_ms = duration_ms;
	}
	
	public boolean isFinished(long now) {
		return now >= this.start_time + this.duration_ms;
	}
	
	public float getFraction(long now) {
		return Math.min(1, (now - this.start_time)/this.duration_ms);
	}
}
