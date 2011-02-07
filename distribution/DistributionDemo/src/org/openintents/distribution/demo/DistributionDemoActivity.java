package org.openintents.distribution.demo;

import org.openintents.distribution.DistributionLibraryActivity;
import org.openintents.distribution.EulaOrNewVersion;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;

// Extend your activity from DistributionLibraryActivity
// or from DistributionLibraryListActivity.
public class DistributionDemoActivity extends DistributionLibraryActivity {
	
	private static final String TAG = "DistribtionDemo";
	
	private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
	
	private static final int DIALOG_DISTRIBUTION_START = 100; // MUST BE LAST
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistribution.setFirst(MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
        
        // Check whether EULA has been accepted
        // or information about new version can be presented.
        if (mDistribution.showEulaOrNewVersion()) {
            return;
        }
        
        setContentView(R.layout.main);
    }

    // Optionally override onCreateOptionsMenu()
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		// Add other menu items here....
 		
 		// Add distribution menu items last.
 		mDistribution.onCreateOptionsMenu(menu);
 		
 		return true;
 	}
 	
 	// Optionally override onCreateDialog()
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d = super.onCreateDialog(id);
		
		// Based on id, create your own dialogs.
		
		return d;
	}
 	
	// Called by the buttons in this activity:
    public void onClickResetEula(View view) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(EulaOrNewVersion.PREFERENCES_EULA_ACCEPTED, false);
		e.commit();
    }
    
    public void onClickResetVersion(View view) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putInt(EulaOrNewVersion.PREFERENCES_VERSION_NUMBER, 0);
		e.commit();
    }
    
    public void onClickShowInfoActivity(View view) {
    	Intent i = new Intent(this, InfoActivity.class);
    	startActivity(i);
    }
}