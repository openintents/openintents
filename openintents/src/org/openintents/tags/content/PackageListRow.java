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
	
	private static final int PACKAGE_PACKAGE_NAME = 3;

	private ImageView mIcon;

	private TextView mName;
	
	private TextView mPackageName;

	public PackageListRow(Context context) {
		super(context);

		setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		mIcon = new ImageView(context);
		mIcon.setPadding(2, 2, 2, 2);
		mIcon.setId(PACKAGE_ICON);

		mName = new TextView(context);
		mName.setGravity(RelativeLayout.CENTER_VERTICAL);
		mName.setId(PACKAGE_NAME);
		
		// set some explicit values for now
		mName.setTextSize(24);
		mName.setTextColor(0xFFFFFFFF);
		mName.setPadding(5,5,0,0);
		
		mPackageName = new TextView(context);
		mPackageName.setGravity(RelativeLayout.CENTER_VERTICAL);
		mPackageName.setId(PACKAGE_PACKAGE_NAME);
		
		// set some explicit values for now
		mPackageName.setTextSize(12);
		mPackageName.setTextColor(0xFFAAAAAA);
		mPackageName.setPadding(5,0,0,0);
		

		RelativeLayout.LayoutParams icon = new RelativeLayout.LayoutParams(64, 64);
		icon.addRule(ALIGN_WITH_PARENT_LEFT);
		addView(mIcon, icon);

		RelativeLayout.LayoutParams name = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 40);
		name.addRule(POSITION_TO_RIGHT, PACKAGE_ICON);
		addView(mName, name);

		RelativeLayout.LayoutParams packagename = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 24);
		packagename.addRule(POSITION_BELOW, PACKAGE_NAME);
		packagename.addRule(POSITION_TO_RIGHT, PACKAGE_ICON);
		addView(mPackageName, packagename);
	}

	public void bindCursor(Cursor cursor) {
		String packageName = cursor.getString(cursor.getColumnIndex("package"));
		String name = cursor.getString(cursor.getColumnIndex("name"));

		Drawable icon;
		
		PackageManager pm = mContext.getPackageManager();
		try {
			icon = pm.getApplicationIcon(packageName);
		} catch (Exception e) {
			Log.e("PackageListRow", "bindView", e);
			icon = pm.getDefaultActivityIcon();
		}
		
		mIcon.setImageDrawable(icon);
		mName.setText(name);
		mPackageName.setText(packageName);
	}
}
