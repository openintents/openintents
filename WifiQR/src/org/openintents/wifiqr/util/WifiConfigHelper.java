package org.openintents.wifiqr.util;

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


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class WifiConfigHelper {

	
	public static String writeToString(WifiConfiguration wc) {
		StringBuffer sb= new StringBuffer();
		JSONObject jo= new JSONObject();
		try {
			jo.put("hiddenSSID", wc.hiddenSSID);
			try {
				Field field=wc.getClass().getDeclaredField("enterpriseFields");
				Object entArray=field.get(wc);
				for (int i=0;i<Array.getLength(entArray);i++){
					Object enterpriseField=Array.get(entArray,i);
					Field varName=enterpriseField.getClass().getDeclaredField("varName");
					Field value=enterpriseField.getClass().getDeclaredField("value");
					Log.v(varName.get(varName).toString(),value.get(value).toString());
				}
				Log.v("",field.toGenericString());
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			wc.BSSID;
//			wc.networkId;
//			
//			
//			wc.preSharedKey;
//			wc.SSID;
//			wc.priority;
//			wc.wepKeys;
			
		
			
		} catch (JSONException e) {
			return null;
		}
		
		
		
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
