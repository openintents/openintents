package org.openintents.voicenotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class VoiceNoteActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Cursor cursor = getContentResolver().query(VoiceNote.CONTENT_URI,
				new String[] { VoiceNote.VOICE_URI },
				VoiceNote.DATA_URI + " = ?", new String[] { getIntent().getDataString() }, null);
		
		if (cursor.getCount() == 0){
			// record
			Intent intent = new Intent(this, VoicePlayNote.class);
			intent.setData(getIntent().getData());
			startActivity(intent);
			finish();
		} else {
			// show list
			// TODO
		}
	}
}
