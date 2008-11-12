package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.util.AlarmUtils;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	
	private static final String TAG = "PreferencesActivity";

	private CheckBoxPreference mAutoUpdate;
	private ListPreference mInterval;
	
	public final static String PREFERENCE_AUTO_UPDATE = "auto_update";
	public final static String PREFERENCE_UPDATE_INTERVAL = "update_interval";
	
	/**
	 * Timestamp of last update.
	 */
	public final static String PREFERENCE_LAST_UPDATE = "last_update";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		mAutoUpdate = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(PREFERENCE_AUTO_UPDATE);
		mAutoUpdate.setOnPreferenceChangeListener(this);
		
		mInterval = (ListPreference) getPreferenceScreen().findPreference(
				PREFERENCE_UPDATE_INTERVAL);
		mInterval.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object obj) {

		if (PREFERENCE_AUTO_UPDATE.equals(preference.getKey())) {
			boolean autoupdate = (Boolean) obj;
			if (! autoupdate) {
				showWarningDialog();
				
				// Don't set preference yet..
				return false;
			}
		}
		
		refreshUpdateAlarm();
		
		return true;
	}

	
	private void showWarningDialog() {
		new Builder(this).setMessage(R.string.preference_warning_text)
		.setTitle(R.string.preference_warning_title)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.ok, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// Set preference
				mAutoUpdate.setChecked(false);
				refreshUpdateAlarm();
			}
			
		}).setNegativeButton(android.R.string.cancel, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
			}
			
		}).show();		
	}

	/**
	 * Refresh the update alarm after a short delay.
	 */
	private void refreshUpdateAlarm() {
		// After a delay, set the timer according to new preferences.
		(new Handler()).postDelayed(new Runnable() {
			@Override
			public void run() {
				AlarmUtils.refreshUpdateAlarm(PreferencesActivity.this);
			}
		}, 500);
	}
}
