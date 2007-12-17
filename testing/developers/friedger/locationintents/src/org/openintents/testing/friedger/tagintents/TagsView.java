package org.openintents.testing.friedger.tagintents;

import org.openintents.R;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class TagsView extends Activity implements OnItemClickListener {

	/** tag for logging */
	private static final String TAG = "tagView";
	private static final String TAG_ACTION = "TAG";

	private ListView mList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tags);

		mList = (ListView) findViewById(R.id.tags);
		mList.setOnItemClickListener(this);
		fillData();

		Button button = (Button) findViewById(R.id.add_tag_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ContentValues values = new ContentValues(2);
				EditText newTag = (EditText) findViewById(R.id.new_tag);
				values.put(Contents.URI, "string://"
						+ newTag.getText().toString());
				// TODO provider missing
				try {
					getContentResolver().insert(Tags.CONTENT_URI, values);
				} catch (Exception e) {
					Log.i(TAG, "insert failed", e);
					return;
				}

				fillData();

			}

		});

	}

	private void fillData() {

		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.URI_1, Tags.URI_2 }, null, null,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.i(TAG, "missing tag provider");
			mList.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, new String[] { "A",
							"B" }));
			return;
		}

		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the NAME column in the people database to...
				new String[] { Contents.URI },
				// The "text1" view defined in the XML template
				new int[] { R.id.name });
		mList.setAdapter(adapter);
	}

	public void onItemClick(AdapterView l, View v, int position, long id) {
		insertTagUri(id);

		setResult(0, String.valueOf(id));
		finish();

	}

	/**
	 * @param id
	 */
	private void insertTagUri(long id) {
		ContentValues values = new ContentValues(2);
		values.put(Contents.URI, getIntent().getData().toString());
		values.put(Tags.TAG_ID, id);
		try {
			// TODO does not work yet.
			//getContentResolver().insert(Tags.CONTENT_URI, values);
		} catch (Exception e) {
			Log.i(TAG, "insertTag failed", e);
		}
	}

}