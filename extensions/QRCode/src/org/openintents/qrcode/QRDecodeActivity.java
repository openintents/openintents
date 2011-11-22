package org.openintents.qrcode;

import org.openintents.intents.NotepadIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRDecodeActivity extends Activity {
	private Intent decodeIntent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		decodeIntent = getIntent();
		if (decodeIntent != null) {

			IntentIntegrator integrator = new IntentIntegrator(this);
			integrator.initiateScan();

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanResult != null && scanResult.getContents() != null) {
			decodeIntent.putExtra(NotepadIntents.EXTRA_TEXT,
					scanResult.getContents());
			setResult(RESULT_OK, decodeIntent);
		}
		finish();
	}
}