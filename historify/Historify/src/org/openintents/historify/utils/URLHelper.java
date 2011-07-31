package org.openintents.historify.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class URLHelper {

	public String getMoreInfoURL() {
		return "http://www.example.com/";
	}
	
	public void navigateToMoreInfo(Context context) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getMoreInfoURL()));
		context.startActivity(intent);
	}
}
