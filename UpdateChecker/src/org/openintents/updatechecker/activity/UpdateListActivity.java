package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateCheckerWithNotification;
import org.openintents.updatechecker.UpdateInfo;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateListActivity extends ListActivity {

	private static final String ORG_OPENINTENTS = "org.openintents";
	private static final int MENU_CHECK_ALL = Menu.FIRST;
	private static final int MENU_CHECK_ANDAPPSTORE = Menu.FIRST + 1;
	private static final int MENU_CHECK_VERSIONS = Menu.FIRST + 2;
	private static final int MENU_SHOW_VERSIONS = Menu.FIRST + 3;
	private static final int MENU_REFRESH = Menu.FIRST + 4;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.app_list);

		check(false, false, false);

	}

	private Cursor createList(boolean appsWithNewVersionOnly,
			boolean useAndAppStore, boolean ignoreDbUrl) {
		MatrixCursor c = new MatrixCursor(new String[] { UpdateInfo._ID,
				"name", UpdateInfo.PACKAGE_NAME, "version_name",
				"version_code", "update_url", "info" });
		for (PackageInfo pi : getPackageManager().getInstalledPackages(0)) {
			CharSequence name = getPackageManager().getApplicationLabel(
					pi.applicationInfo);
			String versionName = pi.versionName;
			String info = null;

			// ignore apps from black list
			if ((versionName == null && pi.versionCode == 0)
					|| pi.packageName.startsWith("com.android")) {
				continue;
			}

			// determine update url
			String updateUrl = null;
			if (useAndAppStore) {
				updateUrl = "http://andappstore.com/AndroidPhoneApplications/updates/!veecheck?p="
						+ pi.packageName;
				info = getString(R.string.checked_against_andappstore);
			} else {
				Cursor cursor = getContentResolver().query(
						UpdateInfo.CONTENT_URI,
						new String[] { UpdateInfo.UPDATE_URL,
								UpdateInfo.LAST_CHECK_VERSION_NAME,
								UpdateInfo.LAST_CHECK_VERSION_CODE },
						UpdateInfo.PACKAGE_NAME + " = ?",
						new String[] { pi.packageName }, null);

				if (!ignoreDbUrl && cursor.moveToFirst()) {
					updateUrl = cursor.getString(0);
				} else {
					if (pi.packageName.startsWith(ORG_OPENINTENTS)) {
						updateUrl = "http://www.openintents.org/apks/"
								+ pi.packageName + ".txt";
					}
				}
				cursor.close();
				info = getString(R.string.current_version, versionName);
			}

			// check for update if required
			if (appsWithNewVersionOnly) {
				UpdateCheckerWithNotification updateChecker = new UpdateCheckerWithNotification(
						this, pi.packageName, name.toString(), pi.versionCode,
						versionName, updateUrl, useAndAppStore);
				boolean updateRequired = updateChecker
						.checkForUpdateWithOutNotification();
				
				if (!updateRequired) {
					// null url implies "do not show"
					updateUrl = null;
				} else {
					if (updateChecker.getLatestVersionName() != null) {
						if (updateChecker.getComment() != null) {
							info = getString(R.string.newer_version,
									updateChecker.getLatestVersionName(),
									updateChecker.getComment());
						} else {
							info = getString(R.string.newer_version_comment,
									updateChecker.getLatestVersionName());
						}
					} else {
						if (updateChecker.getComment() != null) {
							info = getString(R.string.newer_version,
									updateChecker.getComment());
						} else {
							info = getString(R.string.newer_version_available);
						}

					}
				}

			}

			if (updateUrl != null) {
				// add application
				Object[] row = new Object[] { pi.packageName.hashCode(), name,
						pi.packageName, versionName, pi.versionCode, updateUrl,
						info };
				c.addRow(row);
			}
		}

		return c;

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		MatrixCursor cursor = (MatrixCursor) getListAdapter().getItem(position);
		cursor.moveToPosition(position);

		final String updateUrl = cursor.getString(cursor
				.getColumnIndexOrThrow(UpdateInfo.UPDATE_URL));
		String packageName = cursor.getString(cursor
				.getColumnIndexOrThrow(UpdateInfo.PACKAGE_NAME));
		String appName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
		String currVersionName = cursor.getString(cursor
				.getColumnIndexOrThrow("version_name"));
		int currVersion = cursor.getInt(cursor
				.getColumnIndexOrThrow("version_code"));

		final UpdateCheckerWithNotification updateChecker = new UpdateCheckerWithNotification(
				this, packageName, appName, currVersion, currVersionName,
				updateUrl, false);

		final ProgressDialog pb = ProgressDialog.show(this,
				getString(R.string.app_name), getString(R.string.checking));

		new Thread() {
			@Override
			public void run() {
				boolean updateRequired = updateChecker
						.checkForUpdateWithOutNotification();

				pb.dismiss();

				if (updateRequired) {
					Intent intent = updateChecker.createUpdateActivityIntent();
					startActivity(intent);
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							// we don't know whether the lookup failed or no
							// newer version available
							Toast
									.makeText(UpdateListActivity.this,
											R.string.app_up_to_date,
											Toast.LENGTH_SHORT).show();
						};
					});

				}

			}
		}.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_CHECK_ANDAPPSTORE, 0, R.string.check_andappstore);
		menu.add(0, MENU_CHECK_VERSIONS, 0, R.string.check_versions);
		menu.add(0, MENU_SHOW_VERSIONS, 0, R.string.show_versions);
		menu.add(0, MENU_REFRESH, 0, R.string.refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CHECK_ANDAPPSTORE:
			check(true, true, false);
			break;
		case MENU_CHECK_VERSIONS:
			check(true, false, false);
			break;
		case MENU_SHOW_VERSIONS:
			check(false, false, false);
			break;
		case MENU_REFRESH:
			check(false, false, true);
			break;
		}
		return true;
	}

	private void check(final boolean appsWithNewVersionOnly,
			final boolean useAndAppStore, final boolean ignoreDbUrl) {
		String msg;
		if (appsWithNewVersionOnly){
			msg = getString(R.string.checking);
		} else {
			msg = getString(R.string.building_app_list);
		}
		final ProgressDialog pb = ProgressDialog.show(this,
				getString(R.string.app_name), msg);

		new Thread() {
			@Override
			public void run() {
				final Cursor c = createList(appsWithNewVersionOnly,
						useAndAppStore, ignoreDbUrl);
				pb.dismiss();
				runOnUiThread(new Runnable() {

					public void run() {
						ListAdapter adapter = new SimpleCursorAdapter(
								UpdateListActivity.this,
								android.R.layout.simple_list_item_2, c,
								new String[] { "name", "info" },
								new int[] { android.R.id.text1,
										android.R.id.text2 });
						setListAdapter(adapter);
					}

				});
			}
		}.start();
		
		if (!appsWithNewVersionOnly){
			if (useAndAppStore){				
				setTitle(R.string.title_list_all_versions_from_andappstore);
			} else {
				setTitle(R.string.title_list_all_versions);				
			}
		} else {
			if (useAndAppStore){				
				setTitle(R.string.title_list_new_versions_from_andappstore);
			} else {
				setTitle(R.string.title_list_new_versions);				
			}
		}

	}
}
