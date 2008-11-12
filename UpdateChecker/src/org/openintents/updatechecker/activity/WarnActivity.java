package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.db.UpdateInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WarnActivity extends Activity {
	private Intent mIntent;

	private OnClickListener mOkListener = new OnClickListener() {

		public void onClick(View v) {
			startActivity(mIntent);
			finish();
		}

	};

	private OnClickListener mCancleListener = new OnClickListener() {

		public void onClick(View v) {
			finish();
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warn);
		mIntent = (Intent) getIntent().getParcelableExtra(
				UpdateInfo.EXTRA_WARN_INTENT);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(mOkListener);

		button = (Button) findViewById(R.id.cancle);
		button.setOnClickListener(mCancleListener);

	}

}
