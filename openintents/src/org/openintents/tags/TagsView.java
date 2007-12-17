package org.openintents.tags;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TagsView extends Activity {

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

		Button button = (Button) findViewById(R.id.add_tag_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				EditText newTag = (EditText) findViewById(R.id.new_tag);
				EditText newContent = (EditText) findViewById(R.id.new_content);

				insertTag(newTag.getText().toString(), newContent.getText()
						.toString());

			}

		});
		fillData();

	}

	protected void insertTag(String tag, String content) {
		ContentValues values = new ContentValues(2);
		values.put(Tags.URI_1, tag);
		values.put(Tags.URI_2, content);
				
		try {
			getContentResolver().insert(Tags.CONTENT_URI, values);
		} catch (Exception e) {
			Log.i(TAG, "insert failed", e);
			return;
		}

		fillData();

	}

	private void fillData() {

		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.URI_1, Tags.URI_2 }, null, null,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mList.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the NAME column in the people database to...
				new String[] { Tags.URI_1, Tags.URI_2 },
				// The "text1" view defined in the XML template
				new int[] { R.id.tag_tag, R.id.tag_content });
		mList.setAdapter(adapter);
	}

}