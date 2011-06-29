package org.openintents.historify.ui;

import org.openintents.historify.ui.fragments.QuickPostSourcesFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class QuickPostsConfigActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			
			QuickPostSourcesFragment fragment = new QuickPostSourcesFragment();
			getSupportFragmentManager().beginTransaction().add(
					android.R.id.content, fragment).commit();
		}

	}

}
