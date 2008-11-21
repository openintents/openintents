package org.openintents.voicenotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.Toast;

public class VoicePlayNote extends Activity {

	private static final String TAG = "VoicePlayNote";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = getVoiceUri(getIntent().getDataString());
		intent.setData(uri);

		Log.v(TAG, "play " + uri);
		if (uri != null) {
			startActivity(intent);
		} else {
			Toast.makeText(this, getText(R.string.no_voice_memo), Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	private Uri getVoiceUri(String dataUri) {
		Cursor cursor = getContentResolver().query(VoiceNote.CONTENT_URI,
				new String[] { VoiceNote.VOICE_URI },
				VoiceNote.DATA_URI + " = ?", new String[] { dataUri }, null);
		Uri result = null;
		if (cursor.moveToNext()) {
			result = Uri.parse(cursor.getString(0));
		}
		cursor.close();
		return result;

	}

}