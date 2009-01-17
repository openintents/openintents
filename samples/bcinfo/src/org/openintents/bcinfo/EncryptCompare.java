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

import org.openintents.intents.FileManagerIntents;

import java.lang.System;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EncryptCompare extends Activity {
	
	private static String TAG = "EncryptCompare";
	private static boolean debug = true;

	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;

	private static final int COMPARE_PROGRESS_KEY = 0;
    protected static final int MSG_COMPARE = 0x101; 

	protected static SecretKeyFactory keyFac;

	private static final byte[] salt = {
		(byte)0xfc, (byte)0x76, (byte)0x80, (byte)0xae,
		(byte)0xfd, (byte)0x82, (byte)0xbe, (byte)0xee,
    };

    private static final int count = 20;

    private String cipherAlgorithms[] = {
//    		"AES",	// no such implementation
//    		"AESCBC",	// implemention not found
    		"PBEWithMD5And128BitAES-CBC-OpenSSL",
    		"PBEWithMD5And192BitAES-CBC-OpenSSL",
    		"PBEWithMD5And256BitAES-CBC-OpenSSL",
    		"PBEWithMD5AndDES",
//    		"PBEWithMD5AndRC2",	// no such implementation
    		"PBEWithSHA1AndDES",
    		"PBEWithSHA256And128BitAES-CBC-BC",
    		"PBEWithSHA256And192BitAES-CBC-BC",
    		"PBEWithSHA256And256BitAES-CBC-BC",
    		"PBEWithSHA1And128BitAES-CBC-BC",
    		"PBEWithSHA1And192BitAES-CBC-BC",
    		"PBEWithSHA1And256BitAES-CBC-BC",
    		"PBEWithSHAAnd2-KEYTRIPLEDES-CBC",
    		"PBEWithSHAAnd256BitAES-CBC-BC",
    		"PBEWithSHAAnd3-KEYTRIPLEDES-CBC",
    };
    EditText mFileName;
	private Thread compareThread=null;

    public Handler myViewUpdateHandler = new Handler(){
    	
    	// @Override
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    			case MSG_COMPARE:
    				Bundle b=msg.getData();
    				String results=b.getString("results");
    				TextView resultsView = (TextView)findViewById(R.id.results);
    				resultsView.setText(results);
    				break;
             }
             super.handleMessage(msg);
        }
    }; 

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encryptcompare);

		Button browseButton = (Button) findViewById(R.id.browse_button);
		browseButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View arg0) {
		    	openFile();
		    }
		});
	    final boolean browseAvailable = isIntentAvailable(this,
	    		FileManagerIntents.ACTION_PICK_FILE);
	    browseButton.setEnabled(browseAvailable);

        mFileName = (EditText) findViewById(R.id.file_path);
        File sdpath = Environment.getExternalStorageDirectory(); 
        String filename= sdpath.toString() + "/" + "passwordsafe.csv";
        mFileName.setText(filename);
        
		Button compareButton = (Button) findViewById(R.id.compare_button);
		compareButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View arg0) {
		        mFileName = (EditText) findViewById(R.id.file_path);

				String fileName = mFileName.getText().toString();
				compareThreadStart(fileName);
		    }
		});

    }
    @Override
    protected void onPause() {
		super.onPause();
		
		if (debug) Log.d(TAG,"onPause()");
		
		if ((compareThread != null) && (compareThread.isAlive())) {
			if (debug) Log.d(TAG,"wait for thread");
			int maxWaitToDie=500000;
			try { compareThread.join(maxWaitToDie); } 
			catch(InterruptedException e){} //  ignore 
		}
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case COMPARE_PROGRESS_KEY: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.compare_progress));
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                return dialog;
            }
        }
        return null;
    }

	private void compareThreadStart(final String filename){
		showDialog(COMPARE_PROGRESS_KEY);
		compareThread = new Thread(new Runnable() {
			public void run() {
				PerformTests(filename);
				dismissDialog(COMPARE_PROGRESS_KEY);

				
				if (debug) Log.d(TAG,"thread end");
				}
			});
		compareThread.start();
	}


    private void PerformTests(String filename) {
        String results="";
    	
		TextView resultsView = (TextView)findViewById(R.id.results);

		File file=new File(filename);
    	if (!file.exists()) {
    		results="File does not exist.";
        	resultsView.setText(results);
        	return;
    	}
    	char[] buf=new char[1024];
    	try {
			FileReader reader=new FileReader(filename);
			reader.read(buf, 0, 1024);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			results=e.getLocalizedMessage();
        	resultsView.setText(results);
        	return;
		} catch (IOException e) {
			e.printStackTrace();
			results=e.getLocalizedMessage();
        	resultsView.setText(results);
        	return;
		}
        for (int i=0; i<cipherAlgorithms.length; i++) {
        	results += "Testing "+cipherAlgorithms[i]+"\n";
//        	resultsView.setText(results);
        	results += encryptTest(cipherAlgorithms[i],buf,"1234567890");
        	results += "\n";
//        	resultsView.setText(results);
        	
			Message m = new Message();
			m.what = EncryptCompare.MSG_COMPARE;
			Bundle b = new Bundle();
			b.putString("results", results);
			m.setData(b);
			EncryptCompare.this.myViewUpdateHandler.sendMessage(m); 

        }
    }

    private String encryptTest(String algorithm, char[] buf, String password) {
    	String results="";
    	Log.d(TAG,"EncryptTest("+algorithm+")");
    	
    	long startTime=System.currentTimeMillis();
    	
        PBEKeySpec pbeKeySpec;
        PBEParameterSpec pbeParamSpec;
        SecretKey pbeKey;
        Cipher pbeCipher;

        pbeParamSpec = new PBEParameterSpec(salt,count);
		pbeKeySpec = new PBEKeySpec(password.toCharArray());
		
		byte[] plaintext = charsToBytes(buf);
    	try {
		    keyFac = SecretKeyFactory
		    .getInstance(algorithm,"BC");
		    pbeKey = keyFac.generateSecret(pbeKeySpec);
		    pbeCipher = Cipher
		    .getInstance(algorithm,"BC");

		    int repetitions=100;
		    boolean result=false;;
		    for (int i=0; i<repetitions; i++) {
		        result=encryptBuffer(pbeCipher, pbeKey,pbeParamSpec, plaintext);
		    }
		    if (result==true) {
		    	results="OK";
		    }else{
		    	results="Fail";
		    }
		} catch (NoSuchAlgorithmException e) {
			results=e.getLocalizedMessage();
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			results=e.getLocalizedMessage();
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			results=e.getLocalizedMessage();
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			results=e.getLocalizedMessage();
			e.printStackTrace();
		}

    	long endTime=System.currentTimeMillis();
    	long runTime=endTime-startTime;
    	results += "\nRun time: "+runTime+"ms\n";

		return results;
    }

    private boolean encryptBuffer(Cipher pbeCipher, SecretKey pbeKey,
    		PBEParameterSpec pbeParamSpec, byte[] plaintext) {
    	boolean result=false;
    	
		@SuppressWarnings("unused")
		byte[] ciphertext = {};

	    try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		    ciphertext = pbeCipher.doFinal(plaintext);
			result=true;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

    	return result;
    }
    
    private void openFile() {
        mFileName = (EditText) findViewById(R.id.file_path);

		String fileName = mFileName.getText().toString();
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.open_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.open_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == RESULT_OK && data != null) {
				// obtain the filename
				String filename = data.getDataString();
				if (filename != null) {
					// Get rid of URI prefix:
					if (filename.startsWith("file://")) {
						filename = filename.substring(7);
					}
					
					mFileName.setText(filename);
				}				
				
			}
			break;
		}
	}
    public static byte[] charsToBytes(char[] chars) {
    	
    	byte [] bytes = new byte [chars.length * 2];
		int j = 0;
		for (int i = 0; i < chars.length; i += 2)
		{
			bytes[i]  = (byte)(chars[j] & 0xff);
			bytes[i+1]= (byte)(chars[j] >> 8 & 0xff);
			j++;
		}
    	return bytes;
    }
    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}
