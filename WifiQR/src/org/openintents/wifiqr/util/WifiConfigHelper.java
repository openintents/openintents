package org.openintents.wifiqr.util;

/**
****************************************************************************
* Copyright (C) 2010 OpenIntents.org                                       **                                                                          *
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
 
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class WifiConfigHelper {

	//PART OF MOBILE APP!

	
	public static String writeToString(WifiConfiguration wc) {
		StringBuffer sb= new StringBuffer();
		JSONObject jo= new JSONObject();
		//IMPLEMENTATION OF THIS IN WIFIQRDESKTOP
		
		return jo.toString();
	}
	
	public static WifiConfiguration readFromString(String str){
		WifiConfiguration wc= new WifiConfiguration();
		try {
			JSONObject jo= new JSONObject(str);
			String password= jo.getString("preSharedKey");
			String SSID= jo.getString("SSID");
			String BSSID=jo.optString("BSSID");
			String hiddenSSSID=jo.optString("hiddenSSID");

			Vector<String>wepKeys=new Vector<String>();
			int wepKeysTotal=0;
			for (int i=0;i<4;i++)
			{
				final String key="WEPKey"+i;
				final String value=jo.optString(key);
				if(value!=null && ! value.equals(""))
				{					
					wepKeys.add("\""+value+"\"");
					wepKeysTotal++;
				}
			}
			
			String secType=		jo.optString("KeyMgmt");
			if(secType!=null && secType.equals("NONE"))
			{				
				BitSet bs=new BitSet(wc.allowedKeyManagement.size());
				bs.clear();//this should reset to 0x00000000 ,which means none/WEP
				wc.allowedKeyManagement=bs;
			}
			
			String[] strWepKeys=new String[wepKeysTotal];
			wepKeys.toArray(strWepKeys);
			
			wc.preSharedKey="\""+password+"\"";
			wc.SSID="\""+SSID+"\"";
			wc.BSSID=BSSID;
			wc.wepKeys=strWepKeys;
			wc.wepTxKeyIndex=wepKeysTotal;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return wc;
	}
	
}
