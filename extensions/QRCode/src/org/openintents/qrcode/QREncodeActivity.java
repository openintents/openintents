package org.openintents.qrcode;

import org.openintents.intents.NotepadIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class QREncodeActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent != null) {
			String text = intent.getStringExtra(NotepadIntents.EXTRA_TEXT);
			String textBeforeSelection = intent
					.getStringExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION);
			String textAfterSelection = intent
					.getStringExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION);

			if (TextUtils.isEmpty(text) && textBeforeSelection != null
					&& textAfterSelection != null) {
				// Nothing had been selected, so let us select all:
				text = textBeforeSelection + textAfterSelection;
				textBeforeSelection = "";
				textAfterSelection = "";
			}

			Intent decodeIntent = new Intent(
					"com.google.zxing.client.android.ENCODE");
			decodeIntent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
			decodeIntent.putExtra("ENCODE_DATA", textBeforeSelection + text
					+ textAfterSelection);
			startActivity(decodeIntent);
			setResult(RESULT_OK, intent);
		}

		finish();
	}
}