package org.openintents.tags.content;

import org.openintents.provider.ContentIndex;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;

public class PackageListAdapter extends CursorAdapter implements Filterable {

	public PackageListAdapter(Cursor c, Context context) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		PackageListRow row = (PackageListRow) view;

		final String packageName = cursor.getString(cursor
				.getColumnIndexOrThrow(ContentIndex.Dir.PACKAGE));
		String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
		// data is the picked content directory
		final String data = cursor.getString(cursor
				.getColumnIndexOrThrow(ContentIndex.Dir.URI));
		int flags = cursor.getInt(cursor
				.getColumnIndexOrThrow(ContentIndex.Dir.FLAGS));
		row.bind(packageName, name , data, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		PackageListRow row = new PackageListRow(context);
		bindView(row, context, cursor);
		return row;
	}

}
