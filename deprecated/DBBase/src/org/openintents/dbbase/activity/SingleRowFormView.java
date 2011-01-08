package org.openintents.dbbase.activity;

import org.openintents.dbbase.R;
import org.openintents.dbbase.DBBase.Columns;
import org.openintents.dbbase.DBBase.Tables;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * alls user to edit/insert new data to a user table.
 * 
 * @author muef
 * 
 */
public class SingleRowFormView extends Activity {

	private static final String TAG = "SingleRowFormView";
	private static final int MENU_NEW_ROW = Menu.FIRST;
	private long mTableId;
	private String mDomain = "row"; // TODO read domain name from meta table
	private long mRowId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row_form);
		View root = findViewById(R.id.form_root);
		LayoutInflater inflater = LayoutInflater.from(this);

		mTableId = Long.parseLong(getIntent().getData().getPathSegments()
				.get(1));
		
		
		Uri newUri = null;

		if (getIntent().getData().getPathSegments().size() == 3) {
			newUri = getContentResolver().insert(getIntent().getData(), new ContentValues());
			if (newUri != null) {
				mRowId = Long.parseLong(newUri.getLastPathSegment());
			}
		} else {
			mRowId = Long.parseLong(getIntent().getData().getPathSegments().get(3));
		}

		if (newUri == null) {
			newUri = getIntent().getData();
		}

		Cursor colCursor = getContentResolver()
				.query(
						Columns.CONTENT_URI,
						new String[] { Columns._ID, Columns.COL_NAME,
								Columns.COL_TYPE }, Columns.TABLE_ID + " = ?",
						new String[] { String.valueOf(mTableId) }, null);
		Cursor contentCursor = getContentResolver().query(
				newUri, null, null, null, null);
		if (contentCursor.moveToNext()) {

			int i = 0;

			while (colCursor.moveToNext()) {
				// TODO switch col_type

				View part = inflater.inflate(R.layout.row_form_part,
						(ViewGroup) root);
				TextView label = (TextView) part.findViewById(R.id.label);
				label.setText(colCursor.getString(1));
				EditText value = (EditText) part.findViewById(R.id.value);
				value.setText(contentCursor.getString(i));
				value.setTag(colCursor.getLong(0));

				i++;
			}
		}
		colCursor.close();
		contentCursor.close();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String newDomain = getString(R.string.new_row, mDomain);
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