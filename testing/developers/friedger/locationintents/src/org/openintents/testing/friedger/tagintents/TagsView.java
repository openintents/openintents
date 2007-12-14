package org.openintents.testing.friedger.tagintents;

import org.openintents.R;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
		setContentView(R.layout.main);

		mList = (ListView) findViewById(R.id.tags);
		mList.setOnItemClickListener(this);
		fillData();

		Button button = (Button) findViewById(R.id.add_tag_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ContentValues values = new ContentValues(2);
				EditText newTag = (EditText) findViewById(R.id.new_tag);
				values.put(Tags.NAME, newTag.getText().toString());
				values.put(Tags.LABEL, newTag.getText().toString());
				// TODO provider missing
				try {
					getContentResolver().insert(Tags.CONTENT_URI, values);
				} catch (Exception e) {
					showAlert("insert failed", e.toString(), "ok", false);
					return;
				}
				
				fillData();
				
			}

		});

	}

	private void fillData() {

		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.NAME }, null, null,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null){
			showAlert("missing provider", "tag provider", "ok", false);
			return;
		}
		
		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the NAME column in the people database to...
				new String[] { Tags.NAME },
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
		values.put("URI", getIntent().getData().toString());
		values.put(Contents.URI, id);
		getContentResolver().insert(Tags.CONTENT_URI, values );
	}

}