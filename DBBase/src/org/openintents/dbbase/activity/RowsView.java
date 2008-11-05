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
 * shows rows of a user table
 * @author muef
 *
 */
public class RowsView extends ListActivity {

	private static final String TAG = "RowsView";
	private static final int MENU_ADD_COLUMN = Menu.FIRST;
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
		// TODO
	}


}