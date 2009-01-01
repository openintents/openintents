/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of OI About.
 *
 *   OI About is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OI About is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OI About.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   
 *   
 *   Based on veecheck-sample.
 */

package org.openintents.about.versioncheck;

import org.openintents.about.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Checkable;

import com.tomgibara.android.veecheck.VeecheckActivity;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.PrefState;

public class VersioncheckActivity extends VeecheckActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.versioncheck);
	}
	
	@Override
	protected VeecheckState createState() {
		return new PrefState(this);
	}
	
	@Override
	protected View getNoButton() {
		return findViewById(R.id.no);
	}
	
	@Override
	protected View getYesButton() {
		return findViewById(R.id.yes);
	}

	@Override
	protected Checkable getStopCheckBox() {
		return (Checkable) findViewById(R.id.stop);
	}
}
