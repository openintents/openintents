package org.openintents.tags.content;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** 
 * Displays a row in the package list.
 *
 */
public class PackageListRow extends RelativeLayout {

	private static final int PACKAGE_ICON = 1;

	private static final int PACKAGE_NAME = 2;
	
	private static final int PACKAGE_PACKAGE_NAME = 3;

	private ImageView mIcon;

	private TextView mName;
	
	private TextView mPackageName;

	/** Small text to display info if no Pick Activity is available. */
	private TextView mAlertInfo;


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
		

		mAlertInfo = new TextView(context);
		mAlertInfo.setGravity(RelativeLayout.CENTER_VERTICAL);
		mAlertInfo.setId(PACKAGE_PACKAGE_NAME);
		
		// set some explicit values for now
		mAlertInfo.setTextSize(12);
		mAlertInfo.setTextColor(0xFFFF0000);
		mAlertInfo.setPadding(5,0,0,0);

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
		
		RelativeLayout.LayoutParams alertinfo = new RelativeLayout.LayoutParams(64, 64);
		alertinfo.addRule(RelativeLayout.ALIGN_WITH_PARENT_TOP);
		alertinfo.addRule(RelativeLayout.ALIGN_WITH_PARENT_RIGHT);
		addView(mAlertInfo, alertinfo);
	}

	public void bindCursor(Cursor cursor) {
		String packageName = cursor.getString(cursor.getColumnIndex("package"));
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String alertinfo = "";

		Drawable icon;
		
		PackageManager pm = mContext.getPackageManager();
		try {
			icon = pm.getApplicationIcon(packageName);
		} catch (Exception e) {
			Log.e("PackageListRow", "bindView", e);
			icon = pm.getDefaultActivityIcon();
		}
		
		// data is the picked content directory
		String data = cursor.getString(2);
		Uri uri = Uri.parse(data);
		Intent intent = new Intent(Intent.PICK_ACTION, uri);
		if (pm.resolveActivity(intent, 0) == null) {
			alertinfo = "no pick activity available";
		};
		
		mIcon.setImageDrawable(icon);
		mName.setText(name);
		mPackageName.setText(packageName);
		mAlertInfo.setText(alertinfo);
		
	}
}
