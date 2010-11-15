/*
 * Copyright (C) 2010 Karl Ostmo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
