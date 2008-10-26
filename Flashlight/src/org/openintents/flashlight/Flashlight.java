package org.openintents.flashlight;

import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.Update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;

public class Flashlight extends Activity {
	
	private static final String TAG = "Flashlight";

	private static final int MENU_ABOUT = Menu.FIRST + 1;
	
	
	private LinearLayout mBackground;
	

	private PowerManager.WakeLock mWakeLock;
	private boolean mWakeLockLocked = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		if (!EulaActivity.checkEula(this)) {
			return;
		}
		Update.check(this);

		// Turn off the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        setContentView(R.layout.main);
        
        mBackground = (LinearLayout) findViewById(R.id.background);
        
        mBackground.setBackgroundColor(0xffffffff);
        

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"Flashlight");
    }
    
    

	@Override
	protected void onPause() {
		super.onPause();
		
		wakeUnlock();
	}



	@Override
	protected void onResume() {
		super.onResume();
		
		wakeLock();
	}

	/////////////////////////////////////////////////////
	// Menu
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		 menu.add(0, MENU_ABOUT, 0, R.string.about)
		  .setIcon(android.R.drawable.ic_menu_info_details) .setShortcut('0', 'a');

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAboutBox();
			return true;

		}
		return super.onOptionsItemSelected(item);

	}

	/////////////////////////////////////////////////////
	// Other functions
	
	private void wakeLock() {
		if (!mWakeLockLocked) {
			Log.d(TAG, "WakeLock: locking");
			mWakeLock.acquire();
			mWakeLockLocked = true;
		}
	}

	private void wakeUnlock() {
		if (mWakeLockLocked) {
			Log.d(TAG, "WakeLock: unlocking");
			mWakeLock.release();
			mWakeLockLocked = false;
		}
	}
	

	private void showAboutBox() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	
}