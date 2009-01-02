package org.openintents.notepad.noteslist;

import org.openintents.notepad.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotesListItemView extends LinearLayout {
	
	private static final String TAG = "NotesListItemView";

	Context mContext;
	
	private TextView mTitle;
	private TextView mInfo;
	private ImageView mStatus;
	
	
	public NotesListItemView(Context context) {
		super(context);
		mContext = context;

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.noteslist_item, this, true);
		
		mTitle = (TextView) findViewById(R.id.title);
		mInfo = (TextView) findViewById(R.id.info);
		mStatus = (ImageView) findViewById(R.id.status);
	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}
	
	public void setTags(String tags) {
		mInfo.setText(tags);
	}
	
	public void setEncrypted(long encrypted) {
		if (encrypted > 0) {
			mStatus.setImageResource(android.R.drawable.ic_lock_lock);
		} else {
			mStatus.setImageBitmap(null);
		}
	}
}
