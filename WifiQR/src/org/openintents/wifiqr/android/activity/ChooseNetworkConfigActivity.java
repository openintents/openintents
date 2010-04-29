package org.openintents.wifiqr.android.activity;

/**
****************************************************************************
* Copyright (C) 2010 OpenIntents.org                                       *
*                                                                          *
* Licensed under the Apache License, Version 2.0 (the "License");          *
* you may not use this file except in compliance with the License.         *
* You may obtain a copy of the License at                                  *
*                                                                          *
*      http://www.apache.org/licenses/LICENSE-2.0                          *
*                                                                          *
* Unless required by applicable law or agreed to in writing, software      *
* distributed under the License is distributed on an "AS IS" BASIS,        *
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
* See the License for the specific language governing permissions and      *
* limitations under the License.                                           *
****************************************************************************
*/


import java.util.ArrayList;
import java.util.List;

import org.openintents.wifiqr.WifiQR;
import org.openintents.wifiqr.util.WifiConfigHelper;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseNetworkConfigActivity extends ListActivity {

	
	public void onCreate(Bundle savedstate){
		super.onCreate(savedstate);
		
		WifiManager wifimanager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		List<WifiConfiguration> wifiList=wifimanager.getConfiguredNetworks();
		
		setListAdapter(new ArrayAdapter<WifiConfiguration>(this, android.R.layout.simple_list_item_1,android.R.id.text1, wifiList));
		
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		WifiConfiguration wifiConf=(WifiConfiguration) getListAdapter().getItem(position);
		
		Intent intent=new Intent();
		intent.putExtra(WifiQR.EXTRA_WIFI_CONFIG_STRING,WifiConfigHelper.writeToString(wifiConf));
		setResult(RESULT_OK, intent);
		finish();
	}
	
	
}
