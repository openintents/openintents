package org.openintents.locations;

import org.openintents.provider.Location;
import org.openintents.provider.Tag;
import org.openintents.provider.Tag.Tags;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TagsViewBinder implements ViewBinder {

	Tag mTag;

	public TagsViewBinder(Context context) {
		mTag = new Tag(context);
	}

	public boolean setViewValue(View view, Cursor cursor, int i) {		
		if (view instanceof TextView && "_id".equals(cursor.getColumnName(i))) {
			Cursor tags = mTag.findTags(ContentUris.withAppendedId(
					Location.Locations.CONTENT_URI, cursor.getLong(i))
					.toString());
			StringBuffer sb = new StringBuffer();
			int colIndex = tags.getColumnIndex(Tags.URI_1);
			while (tags.next()) {
				sb.append(tags.getString(colIndex));
				sb.append(" ");
			}
			((TextView) view).setText(sb.toString());
			return true;
		} else {
			return false;
		}
	}

}
