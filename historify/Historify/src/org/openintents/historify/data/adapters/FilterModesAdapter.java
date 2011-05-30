package org.openintents.historify.data.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.loaders.FilterLoader;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FilterModesAdapter extends BaseAdapter {

	private Activity mContext;

	private FilterLoader mLoader;
	private List<Contact> mItems;
	private final String DEFAULT, NEW;

	public FilterModesAdapter(Activity context) {

		mContext = context;
		mLoader = new FilterLoader();
		mItems = new ArrayList<Contact>();

		DEFAULT = mContext.getString(R.string.sources_filter_mode_default);
		NEW = mContext.getString(R.string.sources_filter_mode_new);
		
		load();
	}

	public int getCount() {
		return 2 + mItems.size();
	}

	public boolean isDefault(int position) {
		return position == 0;
	}

	public boolean isAddNew(int position) {
		return position == mItems.size() + 1;
	}

	public Contact getItem(int position) {
		return (!isDefault(position) && !isAddNew(position)) ? mItems
				.get(position - 1) : null;
	}

	public List<Contact> getItems() {
		List<Contact> retval = new ArrayList<Contact>();
		retval.addAll(mItems);
		return retval;
	}

	public long getItemId(int position) {
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mContext.getLayoutInflater().inflate(
					android.R.layout.simple_spinner_item, null);
		}

		setItemText((TextView) convertView, position);
		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mContext.getLayoutInflater().inflate(
					android.R.layout.simple_spinner_dropdown_item, null);
		}

		setItemText((TextView) convertView, position);
		return convertView;
	}

	private void setItemText(TextView txtItem, int position) {

		if (isDefault(position))
			txtItem.setText(DEFAULT);
		else if (isAddNew(position))
			txtItem.setText(NEW);
		else
			txtItem.setText(mItems.get(position - 1).getName());
	}
	
	private void load() {
		
		mItems.clear();
		String[] contactLookupKeys = mLoader.loadFilterModeLookupKeys(mContext);
		
		if(contactLookupKeys.length!=0) {
			ContactLoader contactLoader = new ContactLoader();
			Cursor cursor = contactLoader.openCursor(mContext, false, contactLookupKeys);
			
			for(int i=0;i<cursor.getCount();i++) {
				Contact contact = contactLoader.loadFromCursor(cursor, i);
				if(contact!=null) mItems.add(contact);
			}	
		}
		
		notifyDataSetChanged();
	}

	public int insert(Contact contact, SourcesAdapter mSourcesAdapter) {

		boolean succ = mLoader.insertFilters(mContext, contact, mSourcesAdapter.getItems());
		if (succ) {
			mItems.add(contact);
			Collections.sort(mItems, new Contact.Comparator());

			notifyDataSetChanged();

			for (int i = 0; i < mItems.size(); i++) {
				if (mItems.get(i).equals(contact))
					return i + 1;
			}
		}
		return -1;
	}

	public void delete(int actFilterModePosition) {
		
		mLoader.deleteFilters(mContext,mItems.get(actFilterModePosition-1));
		mItems.remove(actFilterModePosition-1);
		notifyDataSetChanged();
	}

}
