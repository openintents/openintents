/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.intents;

/**
 * 
 * @author Peli
 * @version 1.1.0
 */
public class CountdownIntents {

	/**
	 * String extra containing the action to be performed.
	 * 
	 * <p>Constant Value: "org.openintents.extra.ACTION"</p>
	 */
	public static final String EXTRA_ACTION = "org.openintents.extra.ACTION";

	/**
	 * String extra containing the data on which to perform the action.
	 * 
	 * <p>Constant Value: "org.openintents.extra.DATA"</p>
	 */
	public static final String EXTRA_DATA = "org.openintents.extra.DATA";

	/**
	 * Task to be used in EXTRA_ACTION.
	 * 
	 * <p>Constant Value: "STOP_COUNTDOWN"</p>
	 */
	public static final String TASK_START_COUNTDOWN = "org.openintents.countdown.task.start";
	
	/**
	 * Task to be used in EXTRA_ACTION.
	 * 
	 * <p>Constant Value: "STOP_COUNTDOWN"</p>
	 */
	public static final String TASK_STOP_COUNTDOWN = "org.openintents.countdown.task.stop";
	
}
