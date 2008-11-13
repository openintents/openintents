package org.openintents.updatechecker.activity;

import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.EulaActivity;
import org.openintents.updatechecker.AppListInfo;
import org.openintents.updatechecker.OpenMatrixCursor;
import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateCheckerWithNotification;
import org.openintents.updatechecker.db.UpdateInfo;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UpdateListActivity extends ListActivity {

	private static final int MENU_CHECK_ALL = Menu.FIRST;
	private static final int MENU_CHECK_ANDAPPSTORE = Menu.FIRST + 1;
	private static final int MENU_CHECK_VERSIONS = Menu.FIRST + 2;
	private static final int MENU_SHOW_VERSIONS = Menu.FIRST + 3;
	private static final int MENU_REFRESH = Menu.FIRST + 4;
	private static final int MENU_PREFERENCES = Menu.FIRST + 5;
	private static final int MENU_ABOUT = Menu.FIRST + 6;
	private static final String TAG = "UpdateListActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!EulaActivity.checkEula(this)) {
			return;
		}
		
		setContentView(R.layout.app_list);


	}

	@Override
	protected void onResume() {
		super.onResume();
		
		check(false, false);
	}

	private Cursor createList(boolean appsWithNewVersionOnly,
			boolean useAndAppStore) {
		OpenMatrixCursor c = new OpenMatrixCursor(new String[] {
				AppListInfo._ID, AppListInfo.NAME, AppListInfo.PACKAGE_NAME,
				AppListInfo.VERSION_NAME, AppListInfo.VERSION_CODE,
				AppListInfo.UPDATE_URL, AppListInfo.INFO,
				AppListInfo.UPDATE_INTENT, AppListInfo.COMMENT,
				AppListInfo.LATEST_VERSION_NAME,
				AppListInfo.LATEST_VERSION_CODE,
				AppListInfo.IGNORE_VERSION_NAME,
				AppListInfo.IGNORE_VERSION_CODE, AppListInfo.LAST_CHECK,
				AppListInfo.NO_NOTIFICATIONS,
				AppListInfo.IMAGE});
		for (PackageInfo pi : getPackageManager().getInstalledPackages(
				PackageManager.GET_META_DATA)) {
			CharSequence name = getPackageManager().getApplicationLabel(
					pi.applicationInfo);
			String versionName = pi.versionName;
			Drawable image = getPackageManager().getApplicationIcon(pi.applicationInfo);
			String info = null;

			// ignore apps from black list
			if (UpdateInfo.isBlackListed(pi)) {
				continue;
			}

			Intent updateIntent = null;
			int latestVersion = 0;
			String latestVersionName = null;
			String comment = null;
			String updateUrl = null;

			String ignoreVersionName = null;
			int ignoreVersion = 0;

			long lastCheck = 0;
			boolean noNotifications = false;

			// determine update url
			if (useAndAppStore) {
				updateUrl = "http://andappstore.com/AndroidPhoneApplications/updates/!veecheck?p="
						+ pi.packageName;
				info = getString(R.string.checked_against_andappstore);
			} else {
				Cursor cursor = getContentResolver().query(
						UpdateInfo.CONTENT_URI,
						new String[] { UpdateInfo.UPDATE_URL,
								UpdateInfo.IGNORE_VERSION_NAME,
								UpdateInfo.IGNORE_VERSION_CODE,
								UpdateInfo.LAST_CHECK,
								UpdateInfo.NO_NOTIFICATIONS,
								UpdateInfo.LATEST_VERSION,
								UpdateInfo.LATEST_COMMENT},
						UpdateInfo.PACKAGE_NAME + " = ?",
						new String[] { pi.packageName }, null);

				if (cursor.moveToFirst()) {
					updateUrl = cursor.getString(0);
					ignoreVersionName = cursor.getString(1);
					ignoreVersion = cursor.getInt(2);
					lastCheck = cursor.getLong(3);
					noNotifications = cursor.getInt(4) > 0;
					latestVersion = cursor.getInt(5);
					comment = cursor.getString(6);
				} else {

					updateUrl = UpdateInfo.determineUpdateUrlFromPackageName(
							this, pi);
					UpdateInfo
							.insertUpdateInfo(this, pi.packageName, updateUrl);
				}
				cursor.close();
				if (versionName != null) {
					info = getString(R.string.current_version, versionName);
				} else {
					info = getString(R.string.current_version_code, pi.versionCode);
				}
			}

			// check for update if required
			if (updateUrl != null && appsWithNewVersionOnly) {
				UpdateCheckerWithNotification updateChecker = new UpdateCheckerWithNotification(
						this, pi.packageName, name.toString(), pi.versionCode,
						versionName, updateUrl, useAndAppStore,
						ignoreVersionName, ignoreVersion, lastCheck,
						noNotifications);
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
					comment = updateChecker.getComment();
					latestVersion = updateChecker.getLatestVersion();
					latestVersionName = updateChecker.getLatestVersionName();
					updateIntent = updateChecker.createUpdateActivityIntent();
				}

			}

			if (updateUrl != null) {
				// add application
				Object[] row = new Object[] { pi.packageName.hashCode(), name,
						pi.packageName, versionName, pi.versionCode, updateUrl,
						info, updateIntent, comment, latestVersionName,
						latestVersion, ignoreVersionName, ignoreVersion,
						lastCheck, (noNotifications ? 1 : 0), image };
				c.addRow(row);
			}
		}

		return c;

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		OpenMatrixCursor cursor = (OpenMatrixCursor) getListAdapter().getItem(
				position);
		cursor.moveToPosition(position);

		final String updateUrl = cursor.getString(cursor
				.getColumnIndexOrThrow(UpdateInfo.UPDATE_URL));
		final String packageName = cursor.getString(cursor
				.getColumnIndexOrThrow(UpdateInfo.PACKAGE_NAME));
		String appName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
		String currVersionName = (String)cursor.get(cursor
				.getColumnIndexOrThrow(AppListInfo.VERSION_NAME));
		final int currVersion = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.VERSION_CODE));

		int ignoreVersion = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.IGNORE_VERSION_CODE));
		String ignoreVersionName = (String)cursor.get(cursor
				.getColumnIndexOrThrow(AppListInfo.IGNORE_VERSION_NAME));

		int latestVersion = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.LATEST_VERSION_CODE));
		String latestVersionName = (String)cursor.get(cursor
				.getColumnIndexOrThrow(AppListInfo.LATEST_VERSION_NAME));
		String comment = (String)cursor.get(cursor
				.getColumnIndexOrThrow(AppListInfo.COMMENT));
		Intent updateIntent = (Intent) cursor.get(cursor
				.getColumnIndexOrThrow(AppListInfo.UPDATE_INTENT));
		long lastCheck = 0; // always check, do not use stored value cursor.getLong(cursor.getColumnIndexOrThrow(AppListInfo.LAST_CHECK));
		boolean noNotifications = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.NO_NOTIFICATIONS)) > 0;

		if (noNotifications || (ignoreVersion > currVersion)) {
			// Show dialog
			showIgnoredDialog(packageName);
			return;
		}
				
				
		if (updateIntent instanceof Intent) {
			Intent intent = UpdateInfo.createUpdateActivityIntent(this,
					latestVersion, latestVersionName, comment, packageName,
					appName, updateIntent);
			startActivity(intent);
		} else {

			final UpdateCheckerWithNotification updateChecker = new UpdateCheckerWithNotification(
					this, packageName, appName, currVersion, currVersionName,
					updateUrl, false, ignoreVersionName, ignoreVersion,
					lastCheck, noNotifications);

			final ProgressDialog pb = ProgressDialog.show(this,
					getString(R.string.app_name), getString(R.string.checking));

			new Thread() {
				@Override
				public void run() {
					boolean updateRequired = updateChecker
							.checkForUpdateWithOutNotification();

					pb.dismiss();

					if (updateRequired) {
						Intent intent = updateChecker
								.createUpdateActivityIntent();
						startActivity(intent);
					} else {
						if (updateChecker.getLatestVersion() == currVersion) {
							// No new version exists.
							runOnUiThread(new Runnable() {
								public void run() {
									// Application up to date
									Toast.makeText(UpdateListActivity.this,
											R.string.app_up_to_date,
											Toast.LENGTH_SHORT).show();
								};
							});
						} else {
							// No information available
							runOnUiThread(new Runnable() {
								public void run() {
									showUnknownDialog(packageName);
								};
							});
						}

					}

				}
			}.start();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0, MENU_CHECK_ANDAPPSTORE, 0, R.string.check_andappstore)
		// .setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MENU_CHECK_VERSIONS, 0, R.string.check_versions).setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(0, MENU_SHOW_VERSIONS, 0, R.string.show_versions).setIcon(
				android.R.drawable.ic_menu_agenda);
		menu.add(0, MENU_PREFERENCES, 0, R.string.auto_update).setIcon(
				android.R.drawable.ic_menu_rotate);

		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CHECK_ANDAPPSTORE:
			check(true, true);
			break;
		case MENU_CHECK_VERSIONS:
			check(true, false);
			break;
		case MENU_SHOW_VERSIONS:
			check(false, false);
			break;
		case MENU_PREFERENCES:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case MENU_ABOUT:
			showAboutBox();
			return true;
		}
		return true;
	}

	private void showAboutBox() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	private void check(final boolean appsWithNewVersionOnly,
			final boolean useAndAppStore) {
		String msg;
		if (appsWithNewVersionOnly) {
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
						useAndAppStore);
				pb.dismiss();
				runOnUiThread(new Runnable() {

					public void run() {
						/*
						ListAdapter adapter = new SimpleCursorAdapter(
								UpdateListActivity.this,
								android.R.layout.simple_list_item_2, c,
								new String[] { "name", "info" },
								new int[] { android.R.id.text1,
										android.R.id.text2 });
										*/
						
						ListAdapter adapter = new UpdateListCursorAdapter(UpdateListActivity.this, c);
						setListAdapter(adapter);
					}

				});
			}
		}.start();

		if (!appsWithNewVersionOnly) {
			if (useAndAppStore) {
				setTitle(R.string.title_list_all_versions_from_andappstore);
			} else {
				setTitle(R.string.title_list_all_versions);
			}
		} else {
			if (useAndAppStore) {
				setTitle(R.string.title_list_new_versions_from_andappstore);
			} else {
				setTitle(R.string.title_list_new_versions);
			}
		}

	}
	
	private void showIgnoredDialog(final String packageName) {

		new Builder(this).setMessage(R.string.ignore_text)
		.setTitle(R.string.ignore_title)
		.setIcon(R.drawable.ic_ignore)
		.setPositiveButton(android.R.string.ok, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// Undo ignore:

				UpdateInfo.setNoUpdates(UpdateListActivity.this, packageName, false);

				ContentValues values = new ContentValues();
				values.put(UpdateInfo.IGNORE_VERSION_CODE, 0);
				values.put(UpdateInfo.IGNORE_VERSION_NAME, "");
				getContentResolver().update(UpdateInfo.CONTENT_URI, values,
						UpdateInfo.PACKAGE_NAME + " = ? ",
						new String[] { packageName });
				
				check(false, false);
			}
			
		}).setNegativeButton(android.R.string.cancel, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
			}
			
		}).show();		
	}
	

	private void showUnknownDialog(final String packageName) {

		new Builder(this).setMessage(R.string.unknown_text)
		.setTitle(R.string.unknown_title)
		.setIcon(R.drawable.ic_question)
		.setPositiveButton(R.string.search_on_market, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				UpdateCheckerActivity.searchMarketForPackage(UpdateListActivity.this, 
						packageName);
				
			}
			
		}).setNegativeButton(android.R.string.cancel, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
			}
			
		}).show();		
	}
}
