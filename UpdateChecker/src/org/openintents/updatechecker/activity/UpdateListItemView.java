package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UpdateListItemView extends LinearLayout {
	
	private static final String TAG = "UpdateListListItemView";

	Context mContext;
	
	private TextView mName;
	private TextView mInfo;
	private ImageView mImage;
	private ImageView mStatus;
	
	public static final int STATUS_OK = 1;
	public static final int STATUS_DOWNLOAD = 2;
	public static final int STATUS_UNKNOWN = 3;
	public static final int STATUS_IGNORE = 4;
	
	
	public UpdateListItemView(Context context) {
		super(context);
		mContext = context;

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.app_list_item, this, true);
		
		mName = (TextView) findViewById(R.id.name);
		mInfo = (TextView) findViewById(R.id.info);
		mImage = (ImageView) findViewById(R.id.icon);
		mStatus = (ImageView) findViewById(R.id.status);
	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setName(String name) {
		mName.setText(name);
	}
	
	public void setInfo(String info) {
		mInfo.setText(info);
	}
	
	public void setImage(Drawable image) {
		mImage.setImageDrawable(image);
	}
	
	public void setStatus(int status) {
		switch(status) {
		case STATUS_OK:
			mStatus.setImageResource(R.drawable.ic_ok);
			break;
		case STATUS_DOWNLOAD:
			mStatus.setImageResource(R.drawable.ic_download);
			break;
		case STATUS_UNKNOWN:
			mStatus.setImageResource(R.drawable.ic_question);
			break;
		case STATUS_IGNORE:
			mStatus.setImageResource(R.drawable.ic_ignore);
			break;
		default:
			Log.e(TAG, "Unknown status " + status);
			mStatus.setImageResource(R.drawable.ic_question);
		}
	}
}
