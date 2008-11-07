package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateCheckService;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private CheckBoxPreference mAutoUpdate;
	private ListPreference mInterval;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		mAutoUpdate = (CheckBoxPreference) getPreferenceScreen()
				.findPreference("auto_update");
		mInterval = (ListPreference) getPreferenceScreen().findPreference(
				"update_interval");
		mAutoUpdate.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object obj) {
		if (obj instanceof Boolean) {
			if (((Boolean) obj).booleanValue()) {
				Intent intent = new Intent(this, UpdateCheckService.class);
				intent.setAction(UpdateCheckService.ACTION_SET_ALARM);
				intent.putExtra(UpdateCheckService.EXTRA_INTERVAL, Integer
						.parseInt(mInterval.getValue()));
				startService(intent);
			} else {
				Intent intent = new Intent(this, UpdateCheckService.class);
				intent.setAction(UpdateCheckService.ACTION_UNSET_ALARM);
				startService(intent);
			}
		}
		return true;
	}
}
