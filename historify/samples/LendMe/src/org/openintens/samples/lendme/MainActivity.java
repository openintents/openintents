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

package org.openintens.samples.lendme;

import org.openintens.samples.lendme.data.Item.Owner;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	
	public static final String ACTION_SHOW_ITEM = "org.openintents.samples.lendme.SHOW_ITEM";
	
	private Intent lentIntent, borrowedIntent;
	private TabHost tabHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);
        
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
        
        TabSpec lentTabSpec = tabHost.newTabSpec("tid1");
        TabSpec borrowedTabSpec = tabHost.newTabSpec("tid2");
        
        lentIntent = new Intent(this, ItemsActivity.class);
        lentIntent.putExtra(ItemsActivity.EXTRA_OWNER, Owner.Me.toString());
        lentTabSpec.setIndicator(getString(R.string.main_tab_lent)).setContent(lentIntent);
        
        borrowedIntent = new Intent(this, ItemsActivity.class);
        borrowedIntent.putExtra(ItemsActivity.EXTRA_OWNER, Owner.Contact.toString());
        borrowedTabSpec.setIndicator(getString(R.string.main_tab_borrowed)).setContent(borrowedIntent);

        tabHost.addTab(lentTabSpec);
        tabHost.addTab(borrowedTabSpec);

        int tabToShow = 0;
        
        if(ACTION_SHOW_ITEM.equals(getIntent().getAction())) {
        	//activity is launched by historify to show a particular item
        	if(EventIntentHandler.onEventIntentReceived(this, getIntent())) {
        		tabToShow = EventIntentHandler.getTabToShow()==Owner.Me ? 0 : 1;
        	}
        }

		tabHost.setCurrentTab(tabToShow);
		
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {

    	if(ACTION_SHOW_ITEM.equals(intent.getAction())) {
        	//activity is relaunched by historify to show a particular item
        	if(EventIntentHandler.onEventIntentReceived(this, intent)) {
        		tabHost.setCurrentTab(EventIntentHandler.getTabToShow()==Owner.Me ? 0 : 1);
        	}
        }
    }
    
}