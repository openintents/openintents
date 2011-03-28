package org.openintents.newsreader.channels;
/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class ActionSpinner extends Spinner {

	private OnClickListener actionPerformer;

	public ActionSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ActionSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ActionSpinner(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean performClick() {
		if (actionPerformer != null) {
			actionPerformer.onClick(this);
		}
		return true;
	}

	public void setActionPerformer(OnClickListener onClickListener) {
		actionPerformer = onClickListener;

	}
}
