package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.UpdateCheckerWithNotification;
import org.openintents.updatechecker.UpdateCheckerWithNotificationVeecheck;
import org.openintents.updatechecker.UpdateInfo;

import android.app.ListActivity;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class UpdateListActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createList();
	}

	private void createList() {
		MatrixCursor c = new MatrixCursor(new String[] { UpdateInfo._ID, "name",
				UpdateInfo.PACKAGE_NAME, "version_name", "version_code", "update_url" });
		for (PackageInfo pi : getPackageManager().getInstalledPackages(0)) {
			CharSequence name = getPackageManager().getApplicationLabel(
					pi.applicationInfo);
			String versionName = pi.versionName;
			if (versionName == null) {
				versionName = String.valueOf(pi.versionCode);
			}
			if (versionName == null) {
				versionName = pi.packageName;
			}

			Cursor cursor = getContentResolver().query(UpdateInfo.CONTENT_URI,
					new String[] { UpdateInfo.UPDATE_URL, UpdateInfo.LAST_CHECK_VERSION_NAME, UpdateInfo.LAST_CHECK_VERSION_CODE },
					UpdateInfo.PACKAGE_NAME + " = ?",
					new String[] { pi.packageName }, null);
			String updateUrl;
			if (cursor.moveToFirst()) {
				updateUrl  = cursor.getString(0);
			} else {
				updateUrl = "http://andappstore.com/AndroidPhoneApplications/updates/"+pi.packageName+"!veecheck";
			}

			Object[] row = new Object[] { pi.packageName.hashCode(), name,
					pi.packageName, versionName, pi.versionCode, updateUrl};
			c.addRow(row);
		}

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, c, new String[] { "name",
						"update_url" }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		setListAdapter(adapter);

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		MatrixCursor cursor = (MatrixCursor) getListAdapter().getItem(position);
		cursor.moveToPosition(position);
		
		String updateUrl = cursor.getString(cursor.getColumnIndexOrThrow(UpdateInfo.UPDATE_URL));
		String packageName = cursor.getString(cursor.getColumnIndexOrThrow(UpdateInfo.PACKAGE_NAME));
		String appName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
		String currVersionName = cursor.getString(cursor.getColumnIndexOrThrow("version_name"));
		int currVersion = cursor.getInt(cursor.getColumnIndexOrThrow("version_code"));
		
		
		UpdateCheckerWithNotification updateChecker;
		boolean veecheck = false;
		if (!veecheck ) {
			updateChecker = new UpdateCheckerWithNotification(this,
					packageName, appName, currVersion, currVersionName);

		} else {
			updateChecker = new UpdateCheckerWithNotificationVeecheck(this,
					packageName, appName, currVersion, currVersionName);

		}		
		updateChecker.checkForUpdateWithOutNotification(updateUrl);
		
		startActivity(updateChecker.getUpdateIntent());
	}
}
