package org.openintents.historify.data.adapters;

import java.util.Date;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.utils.DateUtils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentlyContactedAdapter extends ContactsAdapter {
	
	public RecentlyContactedAdapter(Activity context) {
		super(context, new ContactLoader.RecentlyContactedLoadingStrategy());
		mContactIconHelper = new ContactIconHelper(mContext,R.drawable.contact_default_large);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Contact contact = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.listitem_recently_contacted, null);
		}

		TextView tv = (TextView) convertView
				.findViewById(R.id.contacts_listitem_txtName);
		tv.setText(contact.getName());

		
		ImageView iv = (ImageView)convertView.findViewById(R.id.contacts_listitem_imgIcon);
		iv.setImageResource(R.drawable.contact_default_large);
		mContactIconHelper.loadContactIcon(contact, iv);
		
		tv = (TextView)convertView.findViewById(R.id.contacts_listitem_txtContacted);
		tv.setText(DateUtils.formatPrettyDate(new Date(contact.getLastTimeContacted())));
		
		return convertView;

	}

}
