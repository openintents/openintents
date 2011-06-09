/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify;

import org.openintents.historify.fragments.TimeLineFragment;
import org.openintents.historify.uri.Actions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 
 * Displays Historify timeline.
 * 
 * @author berke.andras
 * 
 */
public class TimeLineActivity extends FragmentActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {

			TimeLineFragment fragment = new TimeLineFragment();
			Bundle arguments = getIntent().getExtras();
			fragment.setArguments(arguments == null ? new Bundle() : arguments);
			getSupportFragmentManager().beginTransaction().add(
					android.R.id.content, fragment).commit();
		}

	}

}
