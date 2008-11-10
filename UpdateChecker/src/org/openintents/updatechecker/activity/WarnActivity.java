package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WarnActivity extends Activity implements OnClickListener {
	private Intent mIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warn);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		mIntent = (Intent) getIntent().getParcelableExtra(
				UpdateInfo.EXTRA_WARN_INTENT);

	}

	public void onClick(View arg0) {
		startActivity(mIntent);
		finish();
	}
}
