package org.openintents.dbbase.activity;

import org.openintents.dbbase.R;
import org.openintents.dbbase.DBBase.Columns;
import org.openintents.dbbase.DBBase.Tables;
import org.openintents.dbbase.R.id;
import org.openintents.dbbase.R.layout;
import org.openintents.dbbase.R.string;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ColumnsView extends ListActivity {

	private static final String TAG = "ColumnsView";
	private static final int MENU_ADD_COLUMN = Menu.FIRST;
	private static final int MENU_CREATE_TABLE = Menu.FIRST + 1;
	private long mTableId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTableId = Long.parseLong(getIntent().getData().getLastPathSegment());
		createList();
	}

	private void createList() {
		Uri uri = Columns.CONTENT_URI;
		Cursor cursor = managedQuery(uri, new String[] { Columns._ID,
				Columns.COL_NAME }, Columns.TABLE_ID + " = ?",
				new String[] { String.valueOf(mTableId) }, null);
		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursor,
				new String[] { Columns.COL_NAME },
				new int[] { android.R.id.text1 });
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADD_COLUMN, 0, R.string.add_column).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, MENU_CREATE_TABLE, 0, R.string.create_table).setIcon(
				android.R.drawable.ic_menu_save);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_COLUMN:
			addColumnWithDialog();
			break;
		case MENU_CREATE_TABLE:
			createTable();
			break;
		}
		return true;
	}

	private void addColumnWithDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.add_column_dialog, null);
		final TextView textView = (TextView) view.findViewById(R.id.text);
		AlertDialog dialog = new AlertDialog.Builder(this).setIcon(
				android.R.drawable.alert_dark_frame).setTitle(
				R.string.add_column_dialog_title).setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								addColumn(textView.getText().toString(), "TEXT");
							}
						}).setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).show();

	}

	protected void addColumn(String colName, String colType) {
		Uri uri = Columns.CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(Columns.COL_NAME, colName);
		values.put(Columns.COL_TYPE, colType);
		values.put(Columns.TABLE_ID, mTableId);
		getContentResolver().insert(uri, values);
	}

	protected void createTable() {
		Uri uri = Tables.CONTENT_URI.buildUpon().appendPath(
				String.valueOf(mTableId)).appendQueryParameter(
				Tables.QUERY_CREATE_TABLE, "true").build();
		getContentResolver().update(uri, null, null, null);
	}

}