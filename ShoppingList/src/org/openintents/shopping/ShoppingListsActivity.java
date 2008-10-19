package org.openintents.shopping;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Lists;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ShoppingListsActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor cursor = managedQuery(Shopping.Lists.CONTENT_URI, new String[] {
				Lists._ID, Lists.NAME }, null, null, Lists.DEFAULT_SORT_ORDER);
		setListAdapter(new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursor,
				new String[] { Lists.NAME }, new int[] { android.R.id.text1 }));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (getCallingActivity() != null
				&& Intent.ACTION_PICK.equals(getIntent().getAction())) {
			Intent data = new Intent();
			data.setData(Uri.withAppendedPath(Lists.CONTENT_URI, String
					.valueOf(id)));
			setResult(RESULT_OK, data);
			finish();
		}
	}
}
