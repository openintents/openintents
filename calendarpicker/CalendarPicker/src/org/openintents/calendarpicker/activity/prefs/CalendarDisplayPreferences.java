package org.openintents.calendarpicker.activity.prefs;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.ColormapSelectionListActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class CalendarDisplayPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


	static final String TAG = "CalendarDisplayPreferences";


	public final static String PREFKEY_ENABLE_TRANSPARENCY = "enable_transparency";
	public final static boolean DEFAULT_ENABLE_TRANSPARENCY = true;

	public final static String PREFKEY_COLORMAP_OVERRIDE = "colormap_override";
	public final static String PREFKEY_ENABLE_COLORMAP_OVERRIDE = "enable_colormap_override";

	public final static String PREFKEY_DARK_WATERMARK = "dark_watermark";
	
	

	public final static String INTENT_EXTRA_COLORMAP_INDEX = "EXTRA_COLORMAP_INDEX";
	
	
	public final static String SHARED_PREFS_NAME = "calendar_display_prefs";

	final static int REQUEST_CODE_COLORMAP_SELECTION = 1;
	
	Preference colormap;
	// ========================================================================
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.calendar_display_settings);
        
        this.colormap = findPreference(PREFKEY_COLORMAP_OVERRIDE);
        this.colormap.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivityForResult(new Intent(CalendarDisplayPreferences.this, ColormapSelectionListActivity.class), REQUEST_CODE_COLORMAP_SELECTION);
				return true;
			}
		});
        
        setColormapPrefSummary();
        
        final SharedPreferences prefs = getSharedPreferences(CalendarDisplayPreferences.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
//        getPreferences(Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

	// ========================================================================
    void setColormapPrefSummary() {

    	final SharedPreferences prefs = getSharedPreferences(CalendarDisplayPreferences.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
//    	int index = getPreferences(Context.MODE_PRIVATE).getInt(PREFKEY_COLORMAP_OVERRIDE, -1);
    	int index = prefs.getInt(PREFKEY_COLORMAP_OVERRIDE, -1);
    	if (index >= 0)
    		this.colormap.setSummary(ColormapSelectionListActivity.COLORLIST_LABELS[index]);
    	else
    		this.colormap.setSummary(R.string.no_colormap_selected);
    }
    
	// ========================================================================
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {
			Log.i(TAG, "==> result " + resultCode + " from subactivity!  Ignoring...");
//            Toast t = Toast.makeText(this, "Action cancelled!", Toast.LENGTH_SHORT);
//            t.show();
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_COLORMAP_SELECTION:
		{
			int colormap_index = data.getIntExtra(INTENT_EXTRA_COLORMAP_INDEX, -1);
			final SharedPreferences prefs = getSharedPreferences(CalendarDisplayPreferences.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
			prefs.edit().putInt(PREFKEY_COLORMAP_OVERRIDE, colormap_index).commit();
			break;
		}
		}
	}

    // ==========================================================
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (PREFKEY_COLORMAP_OVERRIDE.equals(key))
			setColormapPrefSummary();
	}
}
