package org.openintents.tags.content;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PackageListRow extends RelativeLayout {

	private static final int PACKAGE_ICON = 1;

	private static final int PACKAGE_NAME = 2;

	private ImageView mIcon;

	private TextView mName;

	public PackageListRow(Context context) {
		super(context);

		setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		mIcon = new ImageView(context);
		mIcon.setPadding(2, 2, 2, 2);
		mIcon.setId(PACKAGE_ICON);

		mName = new TextView(context);
		mName.setGravity(RelativeLayout.CENTER_VERTICAL);
		mName.setId(PACKAGE_NAME);

		RelativeLayout.LayoutParams icon = new RelativeLayout.LayoutParams(30, 30);
		icon.addRule(ALIGN_WITH_PARENT_LEFT);
		addView(mIcon, icon);

		RelativeLayout.LayoutParams name = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 30);
		name.addRule(POSITION_TO_RIGHT, PACKAGE_ICON);
		addView(mName, name);
	}

	public void bindCursor(Cursor cursor) {
		String packageName = cursor.getString(cursor.getColumnIndex("package"));

		Drawable icon;
		
		PackageManager pm = mContext.getPackageManager();
		try {
			icon = pm.getApplicationIcon(packageName);
		} catch (Exception e) {
			Log.e("PackageListRow", "bindView", e);
			icon = pm.getDefaultActivityIcon();
		}
		
		mIcon.setImageDrawable(icon);
		mName.setText(packageName);
	}
}
