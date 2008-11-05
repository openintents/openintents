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

/**
 * alls user to edit/insert new data to a user table.
 * @author muef
 *
 */
public class SingleRowFormView extends ListActivity {

	private static final String TAG = "SingleRowFormView";
	private static final int MENU_NEW_ROW = Menu.FIRST;
	private long mTableId;
	private String mDomain = "row"; // TODO read domain name from meta table 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String newDomain = getString(R.string.new_row, mDomain );
		menu.add(0, MENU_NEW_ROW, 0, newDomain).setIcon(
				android.R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_NEW_ROW:
			insertNewRow();
			break;
		}
		return true;
	}

	private void insertNewRow() {
	}

}