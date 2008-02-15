package org.openintents.tags.content;

import java.net.URISyntaxException;
import java.util.List;


import org.openintents.provider.ContentIndex;
import org.openintents.provider.ContentIndex.Entry;
import org.openintents.provider.Tag.Tags;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContentListRow extends RelativeLayout {

	private static final int CONTENT_ICON = 1;
	private static final int CONTENT_URI = 2;
	private static final int CONTENT_TYPE = 3;
	private ImageView mIcon;
	private TextView mName;
	private TextView mType;	
	private DirectoryRegister mDirectoryRegister;

	public ContentListRow(Context context, DirectoryRegister register) {
		super(context);

		mIcon = new ImageView(context);
		mIcon.setPadding(2, 2, 2, 2);
		mIcon.setId(CONTENT_ICON);

		mName = new TextView(context);
		mName.setGravity(RelativeLayout.CENTER_VERTICAL);
		mName.setId(CONTENT_URI);

		mType = new TextView(context);
		mType.setGravity(RelativeLayout.CENTER_VERTICAL);
		mType.setId(CONTENT_TYPE);

		RelativeLayout.LayoutParams icon = new RelativeLayout.LayoutParams(30,
				30);
		icon.addRule(ALIGN_WITH_PARENT_LEFT);
		addView(mIcon, icon);

		RelativeLayout.LayoutParams name = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 30);
		name.addRule(POSITION_TO_RIGHT, CONTENT_ICON);
		addView(mName, name);

		RelativeLayout.LayoutParams type = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, 30);
		type.addRule(ALIGN_WITH_PARENT_RIGHT);
		addView(mType, type);
		
		mDirectoryRegister = register;
	}

	public void bindCursor(Cursor cursor) {
		String uri = cursor.getString(cursor.getColumnIndex(Tags.URI_2));
		updateContentFrom(uri);
	}

	public void updateContentFrom(String uri) {
		Uri contentUri = null;
		String type = null;
		Intent intent = null;

		try {
			contentUri = Uri.parse(uri);
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			mType.setText("U");
		}

		if (contentUri != null) {
			intent = new Intent(Intent.VIEW_ACTION, contentUri);
			try {
				type = getContext().getContentResolver().getType(contentUri);
			} catch (SecurityException e) {
				e.printStackTrace();
				mType.setText("S");
			}
		}

		Drawable icon = getIconForUri(contentUri, type, intent);
		mIcon.setImageDrawable(icon);

		String text = getTextForUri(contentUri, type, intent);
		mName.setText(text);

	}

	private String getTextForUri(Uri uri, String type, Intent intent) {
		String[] result = mDirectoryRegister.getContentBody(uri);
		if (result == null || result.length < 1){
			result = new String[]{"nothing found ("+ uri.toString() + ")"}; 
		}			
		return result[1];
	}

	private Drawable getIconForUri(Uri uri, String type, Intent intent) {
		Drawable icon = null;

		PackageManager pm = getContext().getPackageManager();

		if (intent != null) {

			try {
				icon = pm.getActivityIcon(intent);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
				mType.setText("N");
			}

			if (icon == null) {
				List<ResolveInfo> providerInfo = null;
				try {
					providerInfo = pm.queryIntentActivities(intent, 0);
				} catch (SecurityException e2) {
					e2.printStackTrace();
					mType.setText("S");
				}

				if (providerInfo != null && providerInfo.size() > 0) {
					try {
						icon = pm
								.getApplicationIcon(providerInfo.get(0).activityInfo.applicationInfo.packageName);
					} catch (NameNotFoundException e) {
						Log.e("ContentListRow", "bindCursor", e);
						mType.setText("N");
					}
				}
			}
		}

		if (icon == null) {
			icon = pm.getDefaultActivityIcon();
		}

		return icon;
	}

}
