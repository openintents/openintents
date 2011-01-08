package org.openintents.tags.content;

import java.util.List;

import org.openintents.provider.ContentIndex;
import org.openintents.provider.ContentIndex.Dir;
import org.openintents.tags.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PackageList extends ListActivity {

	private static final String TAG = "PackageList";

	public static final int PACKAGE_ADD_ID = Menu.FIRST;

	public static final int PACKAGE_DEL_ID = Menu.FIRST + 1;

	private Cursor mCursor;

	private TextView mLabel;

	private ContentIndex mContentIndex;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.package_list);

		mLabel = (TextView) findViewById(R.id.label);

		mContentIndex = new ContentIndex(getContentResolver());

		try {
			mCursor = mContentIndex.getPackageNames();
			ListAdapter adapter = new PackageListAdapter(mCursor, this);
			setListAdapter(adapter);
		} catch (Exception e) {
			Log.e(TAG, "onCreate", e);
			finish();
			return;
		}

		registerForContextMenu(getListView());
		refreshPackages();
		
//		PackageManager pm = getPackageManager();
//		List<PackageInfo> packages = pm
//				.getInstalledPackages(PackageManager.GET_PROVIDERS);
//		for (PackageInfo p : packages) {
//			if (p.providers != null) {
//				for (ProviderInfo pi : p.providers) {
//					Log.i(TAG, pi.name + " " + p.packageName);
//				}
//			} else {
//				Log.i(TAG, "-- " + p.packageName);
//			}
//		}
		
	}

	private void refreshPackages() {
		int n = mCursor.getCount();
		String label;
		if (getCallingActivity() != null) {
			label = getString(R.string.label_packages_pick, String.valueOf(n));
		} else {
			label = getString(R.string.label_packages, String.valueOf(n));
		}

		mLabel.setText(label);
	}

	@Override
	protected void onRestart() {
		mCursor.requery();
		refreshPackages();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		mCursor.requery();
		refreshPackages();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PACKAGE_ADD_ID, 0, R.string.menu_package_add).setIcon(
				R.drawable.new_doc).setShortcut('3', 'i');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PACKAGE_ADD_ID:
			addPackage();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		int pos = ((AdapterContextMenuInfo) menuInfo).position;
		mCursor.moveToPosition(pos );
		String menuText = getString(R.string.menu_package_del, mCursor.getString(mCursor.getColumnIndexOrThrow(Dir.NAME)));
		menu.add(0, PACKAGE_DEL_ID, 0, menuText).setShortcut(
				'5', 'u');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		int pos = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		switch (item.getItemId()) {
		case PACKAGE_DEL_ID:
			deletePackage(pos);
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		if (getCallingActivity() != null) {
			setResult(RESULT_OK, new Intent(((Cursor) l
					.getItemAtPosition(position)).getString(2)));
			finish();
		}
	}

	private void deletePackage(int pos) {
		mCursor.moveToPosition(pos);
		OnClickListener c1 = new OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				if (whichButton == AlertDialog.BUTTON1) {
					int uriIndex = mCursor.getColumnIndexOrThrow(Dir.URI);
					String uri = mCursor.getString(uriIndex);
					Log.d(TAG, "Delete package " + uri);
					mContentIndex.deleteContentType(uri);
					mCursor.requery();
					refreshPackages();
				} else {
					Log.d(TAG, "Cancel");
				}
			}
		};

		String title = getString(R.string.dialog_title_package_del);
		String msg = getString(R.string.dialog_message_package_del, mCursor.getString(mCursor.getColumnIndexOrThrow(Dir.NAME)));
		new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
				.setPositiveButton(R.string.dialog_ok, c1).setNegativeButton(
						R.string.dialog_cancel, c1).show();
	}

	private void addPackage() {
		startActivity(new Intent(this, PackageAdd.class));
	}
}
