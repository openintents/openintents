package org.openintents.distribution;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class LaunchFileManager {
	
	private static final String TAG = "LaunchFileManager";

	public static AlertDialog createDialog(final Context context) {
		return new AlertDialog.Builder(context).setMessage(RD.string.filemanager_not_available).setPositiveButton(
				RD.string.filemanager_get_oi_filemanager, new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(context.getString(RD.string.filemanager_market_uri)));
						startSaveActivity(context, intent);

					}

				})
		.create();
	}
	
	/**
	 * Start an activity but prompt a toast if activity is not found
	 * (instead of crashing).
	 * 
	 * @param context
	 * @param intent
	 */
	private static void startSaveActivity(Context context, Intent intent) {
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context,
					RD.string.update_error,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Error starting activity.", e);
		}
	}
}
