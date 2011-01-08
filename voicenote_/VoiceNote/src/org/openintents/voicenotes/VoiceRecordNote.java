package org.openintents.voicenotes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class VoiceRecordNote extends Activity {

	private static final int RECORD = 1;
	private static final String TAG = "VoiceNotes";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(Media.RECORD_SOUND_ACTION);
		startActivityForResult(intent, RECORD);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case RECORD:
				saveVoiceUri(getIntent().getDataString(), data.getDataString());

				break;
			}
		}
		finish();
	}

	private void saveVoiceUri(String dataUri, String voiceUri) {
		Cursor cursor = getContentResolver().query(VoiceNote.CONTENT_URI,
				new String[] { VoiceNote._ID }, VoiceNote.DATA_URI + " = ?",
				new String[] { dataUri }, null);
		String id = null;
		if (cursor.moveToNext()) {
			id = cursor.getString(0);
		}
		cursor.close();

		Log.v(TAG, dataUri + " " + voiceUri);

		ContentValues values = new ContentValues();
		values.put(VoiceNote.DATA_URI, dataUri);
		values.put(VoiceNote.VOICE_URI, voiceUri);

		if (id != null) {
			getContentResolver().update(
					Uri.withAppendedPath(VoiceNote.CONTENT_URI, id), values, null, null);
		} else {
			getContentResolver().insert(VoiceNote.CONTENT_URI, values);
		}

	}
}