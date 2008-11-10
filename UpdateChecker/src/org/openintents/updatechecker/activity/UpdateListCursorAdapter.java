package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.AppListInfo;
import org.openintents.updatechecker.OpenMatrixCursor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class UpdateListCursorAdapter extends CursorAdapter {
	private static final String TAG = "UpdateListCursorAdapter";


	Context mContext;
	
	public UpdateListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		UpdateListListItemView cliv = (UpdateListListItemView) view;
		

		String name = cursor.getString(cursor
				.getColumnIndexOrThrow(AppListInfo.NAME));
		String info = cursor.getString(cursor
				.getColumnIndexOrThrow(AppListInfo.INFO));

		
		OpenMatrixCursor omc = (OpenMatrixCursor) cursor;
		
		Drawable image = (Drawable) omc.get(cursor
				.getColumnIndexOrThrow(AppListInfo.IMAGE));
		
		int no_notifications = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.NO_NOTIFICATIONS));
		
		int versioncode = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.VERSION_CODE));
		int latestversioncode = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.LATEST_VERSION_CODE));
		int ignoreversioncode = cursor.getInt(cursor
				.getColumnIndexOrThrow(AppListInfo.IGNORE_VERSION_CODE));
		
		Log.i(TAG, name + " version: " + latestversioncode + ", " + versioncode + ", " + ignoreversioncode);
		
		if (ignoreversioncode > versioncode) {
			// Also show "Ignore" sign, if current version is being ignored.
			no_notifications = 1;
		}
		
		cliv.setName(name);
		cliv.setInfo(info);
		cliv.setImage(image);
		cliv.setNoNotifications(no_notifications);
		

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new UpdateListListItemView(context);
	}

}
