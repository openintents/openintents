package org.openintents.newsreader.reader;


import org.openintents.newsreader.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * Displays the Eula for the first time.
 * 
 * @author Peli
 *
 */
public class EulaActivity extends Activity {

	/** TAG for log messages. */
	static final String TAG = "EulaActivity";
	
	static final String PREFERENCES_EULA_ACCEPTED = "eula_accepted";
	
	Button mAgree;
	Button mDisagree;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.newsreader_eula);

		//mIntroContinue = (Button) findViewById(R.id.intro_continue);
		mAgree = (Button) findViewById(R.id.button1);
		mAgree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptEula();
			}
		});
		
		mDisagree = (Button) findViewById(R.id.button2);
		mDisagree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				refuseEula();
			}
		});
		
	}
	
	/**
	 * Accept EULA and proceed with main application.
	 */
	public void acceptEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, true);
		e.commit();
		
		Intent i = new Intent(this, Newsreader.class);
		startActivity(i);
		finish();
	}
	
	/**
	 * Refuse EULA.
	 */
	public void refuseEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, false);
		e.commit();
		
		finish();
	}
}
