package org.openintents.historify.data.adapters;

import java.util.HashSet;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactsDialogAdapter extends ContactsAdapter {

	HashSet<String> mDisabledKeys;
	String mDisabledMessage;
	
	public ContactsDialogAdapter(Activity context, HashSet<String> disabledKeys, String disabledMessage) {
		super(context, false);
		
		mDisabledKeys = disabledKeys;
		mDisabledMessage = disabledMessage;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return !mDisabledKeys.contains(getItem(position).getLookupKey());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retval =  super.getView(position, convertView, parent);
		
		TextView tv = (TextView)retval.findViewById(android.R.id.text2);
		tv.setText(isEnabled(position) ? "" : mDisabledMessage);
		
		return retval;
	}
}
