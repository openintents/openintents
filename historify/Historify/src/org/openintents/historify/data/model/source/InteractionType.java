package org.openintents.historify.data.model.source;

import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.net.Uri;

public class InteractionType {

	private Uri mEventIcon;
	private String mActionTitle;
	
	private String mIntentAction;
	
	public InteractionType() {}
	
	public InteractionType(Uri eventIcon, String actionTitle, String intentAction) {
		init(eventIcon,actionTitle,intentAction);
	}
	
	protected void init(Uri eventIcon, String actionTitle, String intentAction) {
		mEventIcon = eventIcon;
		mActionTitle = actionTitle;
		mIntentAction = intentAction;
	}

	public Uri getEventIcon() {
		return mEventIcon;
	}
	
	public String getActionTitle() {
		return mActionTitle;
	}
		
	public Intent crateIntent(String contactLookupKey) {
		
		Intent intent = new  Intent();
		intent.setAction(mIntentAction);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);
		
		return intent;
	}
}
