package org.openintents.tags.content;

import org.openintents.R;
import org.openintents.provider.ContentIndex;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
		}

		refreshPackages();
	}

	private void refreshPackages() {
		int n = mCursor.count();
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
		menu.add(0, PACKAGE_ADD_ID, R.string.menu_package_add, R.drawable.new_doc).setShortcut('3', 'i');
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = mCursor.count() > 0;

		menu.removeGroup(Menu.SELECTED_ALTERNATIVE);

		if (haveItems) {
			menu.add(Menu.SELECTED_ALTERNATIVE, PACKAGE_DEL_ID,
					R.string.menu_package_del, R.drawable.trash).setShortcut('5', 'u');
			menu.setDefaultItem(PACKAGE_ADD_ID);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
		case PACKAGE_DEL_ID:
			deletePackage();
			return true;
		case PACKAGE_ADD_ID:
			addPackage();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		if (getCallingActivity() != null) {
			l.setSelection(position);
			setResult(RESULT_OK, ((Cursor) l.getSelectedItem()).getString(2));
			finish();
		}
	}

	private void deletePackage() {
		OnClickListener c1 = new OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				if (whichButton == AlertDialog.BUTTON2) {
					mCursor.moveTo(PackageList.this.getSelectedItemPosition());
					int packageIndex = mCursor.getColumnIndex("package");
					String packageName = mCursor.getString(packageIndex);
					Log.d(TAG, "Delete package " + packageName);
					mContentIndex.deletePackage(packageName);
					mCursor.requery();
					refreshPackages();
				} else {
					Log.d(TAG, "Cancel");
				}
			}
		};
		OnCancelListener c2 = new OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "onCancel");
			}
		};

		String ok = getString(R.string.dialog_ok);
		String cancel = getString(R.string.dialog_cancel);
		String title = getString(R.string.dialog_title_package_del);
		String msg = getString(R.string.dialog_message_package_del);
		AlertDialog.show(this, title, AlertDialog.BUTTON1, msg, cancel, c1, ok, c1, true, c2);
	}

	private void addPackage() {
		startActivity(new Intent(this, PackageAdd.class));
	}
}
