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


//import org.openintents.news.services.NewsReaderService;
import org.openintents.lib.ConfirmDialogPreference;
import org.openintents.newsreader.R;
import org.openintents.provider.News;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class NewsServiceSettings extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private CheckBoxPreference mRunning;

	private ConfirmDialogPreference mConfirmCompressCategories;
	public static final String COMPRESS_CATEGORIES = "compress_categories";

	private static final String _TAG = "NewsServiceSetting";
	
	public static final String PREFS_ENCODING = "default_encoding";
	public static final String PREFS_ENCODING_DEFAULT = "UTF-8";
	
	// private NotificationManager mNM;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.newsreader_preferences);

		mRunning = (CheckBoxPreference) getPreferenceScreen().findPreference(
				"running_now");
		mRunning.setChecked(NewsreaderService.isAlive());
		mRunning.setOnPreferenceChangeListener(this);

		mConfirmCompressCategories = (ConfirmDialogPreference) getPreferenceScreen()
				.findPreference(COMPRESS_CATEGORIES);

		// mDialogPreference.
		mConfirmCompressCategories
				.setOnConfirmDialogPreferenceListener(new ConfirmDialogPreference.OnConfirmDialogPreferenceListener() {

					public void onConfirmDialogPreference() {
						Log.i(_TAG, "Compress categories");
						News.compressCategories(getContentResolver());
					}

				});

	}

	public boolean onPreferenceChange(Preference preference, Object obj) {
		if (mRunning == preference) {
			Intent intent = new Intent(getApplicationContext(),
					org.openintents.newsreader.services.NewsreaderService.class);
			if (obj instanceof Boolean) {
				if ((Boolean) obj) {
					Log.v(_TAG, "start service");
					startService(intent);
				} else {
					Log.v(_TAG, "stop service");
					stopService(intent);
				}
			}
		}
		return true;
	}
	

	/**
	 * Returns the sort order for the notes list based on the user preferences.
	 * Performs error-checking.
	 * 
	 * @param context
	 *            The context to grab the preferences from.
	 */
	static public String getDefaultEncodingFromPrefs(Context context) {
		String encoding = PREFS_ENCODING_DEFAULT;
		encoding = PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						PREFS_ENCODING, PREFS_ENCODING_DEFAULT);

		return encoding;
	}


}/* eoc */