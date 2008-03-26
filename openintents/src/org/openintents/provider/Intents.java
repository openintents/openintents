package org.openintents.provider;

import android.net.Uri;

public abstract class Intents {
	public static final Uri CONTENT_URI = Uri.parse("openintents://intents");
	public static final String EXTRA_TYPE = "type";
	public static final String EXTRA_ACTION = "action";
	public static final String EXTRA_URI = "uri";
}
