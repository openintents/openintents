/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.main;

import org.openintents.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		// from com.google.android.samples.app.TranslucentFancyActivity
		
        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.BLUR_BEHIND_FLAG,
                WindowManager.LayoutParams.BLUR_BEHIND_FLAG);
        
        // Apply a tint to any windows behind this one.  Doing a tint this
        // way is more efficient than using a translucent background.  Note
        // that the tint color really should come from a resource.
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // TODO: Put value into resource colors.xml
        lp.tintBehind = 0x60000820;
        getWindow().setAttributes(lp);
        
		setTheme(android.R.style.Theme_Dialog);
		setContentView(R.layout.about);
	}

}
