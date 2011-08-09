package org.openintents.historify.data.model.source;

import org.openintents.historify.utils.UriUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class DefaultInteractionType extends InteractionType {

	private static final Uri DEFAULT_EVENT_ICON = UriUtils.drawableToUri("interact_default_normal");
	private static final String DEFAULT_ACTION_TITLE = "Interact";
	private static final String DEFAULT_INTENT_ACTION = null;
	
	private Context mContext;
	
	public DefaultInteractionType(Context context) {
		super(DEFAULT_EVENT_ICON, DEFAULT_ACTION_TITLE,  DEFAULT_INTENT_ACTION);
		mContext = context;
	}

	@Override
	public Intent crateIntent(String contactLookupKey) {
		return createIntent(mContext, contactLookupKey);
	}
	
	public static Intent createIntent(Context context, String contactLookupKey) {
		Intent retval = new Intent();
		retval.setAction(Intent.ACTION_VIEW);
		retval.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Uri contactUri = 
			Contacts.lookupContact(context.getContentResolver(),
					Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactLookupKey));
		
		retval.setData(contactUri);
		
		return retval;
	}
	
	

}
