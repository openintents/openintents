/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.samples.lendme.data;

import org.openintents.historify.data.model.EventData;
import org.openintents.historify.data.providers.Events.Originator;
import org.openintents.historify.services.bridge.HistorifyBridge;
import org.openintents.historify.services.bridge.HistorifyBridge.QuickPostContext;
import org.openintents.samples.lendme.MainActivity;
import org.openintents.samples.lendme.R;
import org.openintents.samples.lendme.Toaster;
import org.openintents.samples.lendme.data.Item.Owner;
import org.openintents.samples.lendme.data.persistence.ItemsProviderHelper.ItemsTable;

import android.content.Context;
import android.os.Bundle;

public class HistorifyPostHelper {

	private static HistorifyPostHelper sInstance;
	private static final int SOURCE_VERSION = 14;
	public static final String PREF_NAME = "posting";
	
	private static final String EVENTKEY_START = "start";
	private static final String EVENTKEY_END = "end";
	
	public static HistorifyPostHelper getInstance(Context context) {
		
		if(sInstance == null) 
			sInstance = new HistorifyPostHelper(context);
		
		return sInstance;
	}
	
	private HistorifyBridge mBridge;
	private boolean mUserPrefersPosting;
	
	private HistorifyPostHelper(Context context) {
		
		mBridge = new HistorifyBridge(R.drawable.icon);
		
		//init QuickPost Context
		String sourceName = context.getString(R.string.app_name);
		String sourceDescription = context.getString(R.string.app_description);
		
		QuickPostContext quickPostContext = new QuickPostContext(sourceName,sourceDescription,null,MainActivity.ACTION_SHOW_ITEM,SOURCE_VERSION);
		mBridge.setQuickPostContext(quickPostContext);
		
		//check is auto posting is set by the user
		mUserPrefersPosting = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE).getBoolean(PREF_NAME, true);
		
	}
	
	public boolean userPrefersPosting() {
		return mUserPrefersPosting;
	}
	
	public void setUserPrefersPosting(Context context, boolean value) {
		mUserPrefersPosting = value;
		context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE).edit().putBoolean(PREF_NAME, mUserPrefersPosting).commit();
	}

	public void postLendingStartEvent(Context context, Bundle parameterSet, long itemId) {

		String contactKey = parameterSet.getString(ItemsTable.CONTACT_KEY);
		long lendingStart = System.currentTimeMillis();
		String itemName = parameterSet.getString(ItemsTable.ITEM_NAME);
		Owner owner = Owner.parseString(parameterSet.getString(ItemsTable.OWNER));
		
		String postText = String.format(context.getString(R.string.post_lent),itemName);
		
		EventData eventData = new EventData( generateEventKey(itemId, EVENTKEY_START), contactKey, lendingStart, postText, owner == Owner.Me ? Originator.user : Originator.contact);
		mBridge.quickPost(context, eventData);
		
		Toaster.toast(context, R.string.post_successful);
	}

	public void postReturedEvent(Context context, Item item) {
		
		String contactKey = item.getContactKey();
		long time = System.currentTimeMillis();
		String itemName = item.getName();
		Owner owner = item.getOwner();
		
		String postText = String.format(context.getString(R.string.post_returned),itemName);
		
		EventData eventData = new EventData( generateEventKey(item.getId(), EVENTKEY_END), contactKey, time, postText, owner == Owner.Me ? Originator.contact : Originator.user);
		mBridge.quickPost(context, eventData);
		
		Toaster.toast(context, R.string.post_successful);
	}
	
	private String generateEventKey(long itemId, String action) {
		
		return String.valueOf(itemId)+"_"+action;
	}

	public long getItemId(String eventKey) {
		return Long.valueOf(eventKey.substring(0,eventKey.indexOf("_")));
	}
}
