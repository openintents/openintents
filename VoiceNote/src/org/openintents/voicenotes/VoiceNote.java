package org.openintents.voicenotes;

import android.net.Uri;
import android.provider.BaseColumns;

public class VoiceNote implements BaseColumns {

	public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.voicenotes/voicenotes");
	public static final String DATA_URI = "data_uri";
	public static final String VOICE_URI = "voice_uri";

}
