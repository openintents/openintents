package org.openintents.newsreader.services;
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
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.util.Log;

import org.openintents.newsreader.R;
import org.openintents.provider.News;


public class PurgeDataPreference extends Preference {
	// This is the constructor called by the inflater
	public PurgeDataPreference(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	@Override
	protected void onClick() {
		new android.app.AlertDialog.Builder(
				getContext()).setNegativeButton(R.string.cancel,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(android.content.DialogInterface dialog,
							int which) {
					}

				}).setPositiveButton(R.string.ok,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(android.content.DialogInterface dialog,
							int which) {
						
							purgeData();
						}
				}).setMessage(
					R.string.dialog_message_purge					
				).create().show();	
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		// This preference type's value type is Integer, so we read the default
		// value from the attributes as an Integer.
		return null;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		// do nothing
	}

	@Override
	protected Parcelable onSaveInstanceState() {

		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}


	protected void purgeData(){
		
		News news=new News(getContext().getContentResolver());

		int res=news.delete(
			News.Contents.CONTENT_URI,
			News.Contents.READ_STATUS+"="+News.STATUS_READ
			+" OR "+News.Contents.READ_STATUS+"="+News.STATUS_DELETED
			,null
		);
		new android.app.AlertDialog.Builder(
			getContext()
			).setNegativeButton(R.string.cancel,
			new android.content.DialogInterface.OnClickListener() {
				public void onClick(android.content.DialogInterface dialog,
						int which) {
				}

			}).setPositiveButton(R.string.ok,
			new android.content.DialogInterface.OnClickListener() {
				public void onClick(android.content.DialogInterface dialog,
						int which) {
					

					}
			}).setMessage(
				getContext().getString(R.string.purged_entrys,String.valueOf(res))
			).create().show();	
	}

}
