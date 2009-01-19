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
		
		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_CREATE_SHORTCUT)) {
			setTitle(R.string.pick_list_for_shortcut);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String action = getIntent().getAction();
		
		if (getCallingActivity() != null) {
			if (Intent.ACTION_PICK.equals(action)) {
				Intent data = new Intent();
				data.setData(Uri.withAppendedPath(Lists.CONTENT_URI, String
						.valueOf(id)));
				setResult(RESULT_OK, data);
				finish();
			} else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
				Intent data = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.withAppendedPath(Lists.CONTENT_URI, String
						.valueOf(id));
				data.setData(uri);
				
				String title = getTitle(uri);
				
				Intent shortcut = new Intent(Intent.ACTION_CREATE_SHORTCUT);
				shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
				shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, data);
				Intent.ShortcutIconResource sir = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon_shoppinglist);
				shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, sir);
				
				
				setResult(RESULT_OK, shortcut);
				finish();
			}
		}
	}
	
	private String getTitle(Uri uri) {
		Cursor c = getContentResolver().query(uri, new String[] {Shopping.Lists.NAME}, null, null, null);
		if (c != null && c.moveToFirst()) {
			return c.getString(0);
		}
		
		// If there was a problem retrieving the note title
		// simply use the application name
		return getString(R.string.app_name);
	}
}
