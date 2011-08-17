package org.openintents.historify.data.model.source;

import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.net.Uri;

/**
 * 
 * Model class representing a method of interaction displayed in the contact
 * popup window of the timeline.
 * 
 * @author berke.andras
 */
public class InteractionType {

	// the icon and the title used to display this interaction type
	private Uri mEventIcon;
	private String mActionTitle;

	// the action of Intent fired when the user selects a type
	private String mIntentAction;

	public InteractionType() {
	}

	/**
	 * Constructor.
	 * 
	 * @param eventIcon
	 *            Icon used when displaying this type.
	 * @param actionTitle
	 *            Title used when displaying this type.
	 * @param intentAction
	 *            Action fired when the user selects the type.
	 */
	public InteractionType(Uri eventIcon, String actionTitle,
			String intentAction) {
		init(eventIcon, actionTitle, intentAction);
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

	/**
	 * Creates an Intent instance based on the Intent action of this interaction
	 * type. The lookup key of the currently selected contact will be added as
	 * an Intent extra.
	 * 
	 * @param contactLookupKey
	 * @return the Intent that could be fired for interaction.
	 */
	public Intent crateIntent(String contactLookupKey) {

		Intent intent = new Intent();
		intent.setAction(mIntentAction);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);

		return intent;
	}
}
