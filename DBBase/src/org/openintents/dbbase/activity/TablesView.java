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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TablesView extends ListActivity {

	private static final String TAG = "TablesView";
	private static final int MENU_ADD_TABLE = Menu.FIRST;
	private static final int MENU_ADMIN_TABLE = Menu.FIRST + 1;
	private static final int MENU_EDIT_CONTENT = Menu.FIRST + 2;
	private static final int MENU_DELETE_TABLE = Menu.FIRST + 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		createList();
	}

	private void createList() {
		Uri uri = Tables.CONTENT_URI;
		Cursor cursor = managedQuery(uri, new String[] { Tables._ID,
				Tables.TABLE_NAME }, null, null, null);
		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursor,
				new String[] { Tables.TABLE_NAME },
				new int[] { android.R.id.text1 });
		setListAdapter(adapter);
		registerForContextMenu(getListView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADD_TABLE, 0, R.string.add_table).setIcon(
				android.R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_TABLE:
			addTableWithDialog();
			break;
		}
		return true;
	}

	private void addTableWithDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.add_table_dialog, null);
		final TextView textView = (TextView) view.findViewById(R.id.text);
		AlertDialog dialog = new AlertDialog.Builder(this).setIcon(
				android.R.drawable.alert_dark_frame).setTitle(
				R.string.add_table_dialog_title).setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								addTable(textView.getText().toString());
							}
						}).setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).show();

	}

	protected void addTable(String tableName) {
		Uri uri = Tables.CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(Tables.TABLE_NAME, tableName);
		getContentResolver().insert(uri, values);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ColumnsView.class);
		intent.setData(Uri.withAppendedPath(Tables.CONTENT_URI, String
				.valueOf(id)));
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, MENU_EDIT_CONTENT, 0, R.string.edit_content);
		menu.add(0, MENU_ADMIN_TABLE, 0, R.string.admin_table);
		menu.add(0, MENU_DELETE_TABLE, 0, R.string.delete_table);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		long id = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		switch (item.getItemId()) {
		case MENU_DELETE_TABLE:
			Uri url = Uri.withAppendedPath(Tables.CONTENT_URI, String
					.valueOf(id));
			getContentResolver().delete(url, null, null);
			break;
		case MENU_ADMIN_TABLE:
			Intent intent = new Intent(this, ColumnsView.class);
			intent.setData(Uri.withAppendedPath(Tables.CONTENT_URI, String
					.valueOf(id)));
			startActivity(intent);
			break;
		case MENU_EDIT_CONTENT:
			intent = new Intent(this, SingleRowFormView.class);
			Uri uri = Tables.CONTENT_URI.buildUpon().appendPath(
					String.valueOf(id)).appendPath("rows").build();
			intent.setData(uri);
			startActivity(intent);
			break;
		}
		return true;
	}
}