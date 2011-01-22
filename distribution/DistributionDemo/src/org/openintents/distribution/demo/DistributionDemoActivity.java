package org.openintents.distribution.demo;

import org.openintents.distribution.AboutDialog;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.UpdateMenu;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class DistributionDemoActivity extends Activity {
	
	private static final String TAG = "DistribtionDemo";

	private static final int MENU_ABOUT = Menu.FIRST + 1;
	private static final int MENU_UPDATE = Menu.FIRST + 2;
	
	private static final int DIALOG_ABOUT = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Log.d(TAG, "onCreate()");

        // Check whether EULA has been accepted.
		if (!EulaActivity.checkEula(this, getIntent())) {
            return;
        }
    }
    
    
    public void onClickResetEula(View view) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(EulaActivity.PREFERENCES_EULA_ACCEPTED, false);
		e.commit();
    }
    

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);

 		UpdateMenu
 				.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.oi_distribution_menu_update);
 		menu.add(0, MENU_ABOUT, 0, R.string.oi_distribution_about).setIcon(
 				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');
 		
 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Intent intent;
		switch (item.getItemId()) {
			
		case MENU_UPDATE:
			UpdateMenu.showUpdateBox(this);
			return true;

		case MENU_ABOUT:
			showAboutBox();
			return true;
			
		}
		return super.onOptionsItemSelected(item);

	}

	private void showAboutBox() {
		//startActivity(new Intent(this, AboutDialog.class));
		AboutDialog.showDialogOrStartActivity(this, DIALOG_ABOUT);
	}
	

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ABOUT:
			return new AboutDialog(this);
			
		}
		return null;
		
	}


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		case DIALOG_ABOUT:
			break;
		}
	}
}