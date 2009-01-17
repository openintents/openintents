/* $Id$
 * 
 * Copyright 2009 Randy McEoin
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
package org.openintents.bcinfo;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.SecretKeyFactory;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class bcinfo extends Activity {
	
	private static String TAG = "bcinfo";
    // Menu Item order
    public static final int MENU_COMPARE_INDEX = Menu.FIRST;

	protected static SecretKeyFactory keyFac;
    protected static String algorithm = "PBEWithMD5And128BitAES-CBC-OpenSSL";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
	    HashSet <String> serviceTypes=new HashSet<String>(); 
	    HashSet <String> cipherAlgorithms=new HashSet<String>();
	    HashSet <String> keyFactoryAlgorithms=new HashSet<String>();
	    HashSet <String> keyAgreementAlgorithms=new HashSet<String>();
	    HashSet <String> keyStoreAlgorithms=new HashSet<String>();
	    HashSet <String> messageDigestAlgorithms=new HashSet<String>();

		try {
		    keyFac = SecretKeyFactory
		    .getInstance(algorithm,"BC");

		    Provider p=keyFac.getProvider();
			TextView infoView = (TextView)findViewById(R.id.info);
	    	infoView.setText(p.getInfo());
			TextView nameView = (TextView)findViewById(R.id.name);
	    	nameView.setText(p.getName());
			TextView versionView = (TextView)findViewById(R.id.version);
	    	versionView.setText(Double.toString(p.getVersion()));

	    	Set <Provider.Service> services=p.getServices();
		    Iterator<Service> iter = services.iterator();
		    while (iter.hasNext()) {
		      Service service=(Service) iter.next();
		    	serviceTypes.add(service.getType());
		    	if (service.getType().compareTo("Cipher")==0) {
		    		cipherAlgorithms.add(service.getAlgorithm());
		    	}
		    	if (service.getType().compareTo("KeyAgreement")==0) {
		    		keyAgreementAlgorithms.add(service.getAlgorithm());
		    	}
		    	if (service.getType().compareTo("KeyFactory")==0) {
		    		keyFactoryAlgorithms.add(service.getAlgorithm());
		    	}
		    	if (service.getType().compareTo("KeyStore")==0) {
		    		keyStoreAlgorithms.add(service.getAlgorithm());
		    	}
		    	if (service.getType().compareTo("MessageDigest")==0) {
		    		messageDigestAlgorithms.add(service.getAlgorithm());
		    	}
		    }
		    Log.d(TAG,"types: "+serviceTypes);
		    Log.d(TAG,"ciphers: "+cipherAlgorithms);
		} catch (NoSuchAlgorithmException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());
		} catch (NoSuchProviderException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());		
		}

    	String service_types=listSortHash(serviceTypes);
		TextView serviceTypesView = (TextView)findViewById(R.id.service_types);
    	serviceTypesView.setText(service_types);

    	String algorithms=listSortHash(cipherAlgorithms);
		TextView ciphersView = (TextView)findViewById(R.id.cipher);
    	ciphersView.setText(algorithms);

    	algorithms=listSortHash(keyAgreementAlgorithms);
		TextView keyAgreementView = (TextView)findViewById(R.id.keyagreement);
    	keyAgreementView.setText(algorithms);

    	algorithms=listSortHash(keyFactoryAlgorithms);
		TextView keyFactoryView = (TextView)findViewById(R.id.keyfactory);
    	keyFactoryView.setText(algorithms);

    	algorithms=listSortHash(keyStoreAlgorithms);
		TextView keyStoreView = (TextView)findViewById(R.id.keystore);
    	keyStoreView.setText(algorithms);

    	algorithms=listSortHash(messageDigestAlgorithms);
		TextView messageDigestView = (TextView)findViewById(R.id.messagedigest);
    	messageDigestView.setText(algorithms);

        String algorithmMedium = "PBEWithMD5And128BitAES-CBC-OpenSSL";

    	algorithm=algorithmMedium;
    	try {
		    keyFac = SecretKeyFactory
		    .getInstance(algorithm,"BC");
		    Log.d(TAG,"instanciated "+algorithm);
		} catch (NoSuchAlgorithmException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());
		} catch (NoSuchProviderException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());		
		}

    }
    
    String listSortHash(HashSet<String> hs) {
    	String list="";
		String[] sa=hs.toArray(new String[hs.size()]);
		Arrays.sort(sa, String.CASE_INSENSITIVE_ORDER);
		for (int i=0; i<sa.length; i++) {
		      list += sa[i] + "\n";
		}
		return list;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	
		menu.add(0,MENU_COMPARE_INDEX, 0, R.string.menu_compare)
			.setIcon(android.R.drawable.ic_menu_info_details);

		return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
    	
		switch(item.getItemId()) {
		case MENU_COMPARE_INDEX:
			Intent encryptCompare = new Intent(this, EncryptCompare.class);
			startActivity(encryptCompare);
			break;
		default:
			Log.e(TAG,"Unknown itemId");
			break;
		}
		return super.onOptionsItemSelected(item);
    }

    
}
