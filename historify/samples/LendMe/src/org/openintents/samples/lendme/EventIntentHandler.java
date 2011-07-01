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

package org.openintents.samples.lendme;

import org.openintents.historify.uri.Actions;
import org.openintents.samples.lendme.data.Item;
import org.openintents.samples.lendme.data.Item.Owner;
import org.openintents.samples.lendme.data.persistence.ItemsLoader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

public class EventIntentHandler {

	private static Owner tabToShow;
	private static Item itemToShow = null;
	
	public static synchronized boolean onEventIntentReceived(Context context, Intent intent) {
		
		String eventKey = intent.getStringExtra(Actions.EXTRA_EVENT_KEY);
		if(eventKey!=null) {
			
			Item item = new ItemsLoader().query(context, eventKey);
			if(item==null) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.app_name)
					.setPositiveButton("OK", null)
					.setMessage(R.string.main_msg_no_item)
					.show();
			} else {
				
				itemToShow = item;
	    		tabToShow  = item.getOwner();
	    		return true;
			}
		}
		
		return false;
	}
	
	public static synchronized Owner getTabToShow() {
		Owner retval = tabToShow;
		tabToShow = null;
		return retval;
	}
	
	public static synchronized Item getItemToShow() {
		Item retval =  itemToShow;
		itemToShow = null;
		return retval;
	}
}
