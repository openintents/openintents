package org.openintens.samples.lendme;

import org.openintens.samples.lendme.data.Item;
import org.openintens.samples.lendme.data.Item.Owner;
import org.openintens.samples.lendme.data.persistence.ItemsLoader;
import org.openintents.historify.uri.Actions;

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
