package org.openintents.voicenotes;

import android.net.Uri;
import android.provider.BaseColumns;

public class VoiceNote implements BaseColumns {
    public static final String AUTHORITY = "org.openintents.voicenotes";

	public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.voicenotes/voicenotes");
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openintents.voicenote.voicenote";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openintents.voicenote.voicenote";

	public static final String DATA_URI = "data_uri";
	public static final String VOICE_URI = "voice_uri";

	public static final String DEFAULT_SORT_ORDER = "voice_uri DESC";
}
