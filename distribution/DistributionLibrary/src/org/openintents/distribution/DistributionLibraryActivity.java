package org.openintents.distribution;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DistributionLibraryActivity extends Activity {

	static final int MENU_DISTRIBUTION_START = Menu.FIRST;
	
	static final int DIALOG_DISTRIBUTION_START = 1;

	DistributionLibrary mDistribution;
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDistribution = new DistributionLibrary(this, MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
        
        // Check whether EULA has been accepted
        // or information about new version can be presented.
		if (mDistribution.showEulaOrNewVersion()) {
            return;
        }
    }

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);

 		mDistribution.onCreateOptionsMenu(menu);
 		
 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDistribution.onOptionsItemSelected(item)) {
			return true;
		}
		/*
		switch (item.getItemId()) {
			// check other cases
		}
		*/
		return super.onOptionsItemSelected(item);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d = mDistribution.onCreateDialog(id);
		if (d != null) {
			return d;
		}
		/*
		switch (id) {
			// check other cases
		}
		*/
		return null;
		
	}


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		mDistribution.onPrepareDialog(id, dialog);
		/*
		switch (id) {
			// check other cases
		}
		*/
	}
}
