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
	/*
	 * XTEA.java Copyright (c) 2005-2007 Thomas Dixon
	 * 
	 * Permission is hereby granted, free of charge, to any person obtaining a
	 * copy of this software and associated documentation files (the
	 * "Software"), to deal in the Software without restriction, including
	 * without limitation the rights to use, copy, modify, merge, publish,
	 * distribute, sublicense, and/or sell copies of the Software, and to permit
	 * persons to whom the Software is furnished to do so, subject to the
	 * following conditions:
	 * 
	 * The above copyright notice and this permission notice shall be included
	 * in all copies or substantial portions of the Software.
	 * 
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
	 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
	 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
	 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
	 * USE OR OTHER DEALINGS IN THE SOFTWARE.
	 */

	public static class XTEA {

		public final int rounds = 32, keySize = 16, blockSize = 8;

		private final int delta = 0x9e3779b9, decryptSum = 0xc6ef3720;

		private int[] subKeys = new int[4];

		public void init(byte[] key) throws InvalidKeyException {
			if (key == null)
				throw new InvalidKeyException("Null key");

			if (key.length != keySize)
				throw new InvalidKeyException(
						"Invalid key length (req. 16 bytes got " + key.length
								+ ")");

			subKeys[0] = bytesToIntBig(key, 0);
			subKeys[1] = bytesToIntBig(key, 4);
			subKeys[2] = bytesToIntBig(key, 8);
			subKeys[3] = bytesToIntBig(key, 12);
		}

		// Expects valid parameters only
		public void encrypt(byte[] in, int inOff, byte[] out, int outOff) {
			int v0 = bytesToIntBig(in, inOff), v1 = bytesToIntBig(in, inOff + 4);

			int n = rounds, sum = 0;

			while (n-- > 0) {
				v0 += ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + subKeys[sum & 3]);
				sum += delta;
				v1 += ((v0 << 4 ^ v0 >>> 5) + v0)
						^ (sum + subKeys[sum >>> 11 & 3]);
			}

			intToBytesBig(v0, out, outOff);
			intToBytesBig(v1, out, outOff + 4);
		}

		public void decrypt(byte[] in, int inOff, byte[] out, int outOff) {
			int v0 = bytesToIntBig(in, inOff), v1 = bytesToIntBig(in, inOff + 4);

			int n = rounds, sum = decryptSum;

			while (n-- > 0) {
				v1 -= ((v0 << 4 ^ v0 >>> 5) + v0)
						^ (sum + subKeys[sum >>> 11 & 3]);
				sum -= delta;
				v0 -= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + subKeys[sum & 3]);
			}

			intToBytesBig(v0, out, outOff);
			intToBytesBig(v1, out, outOff + 4);
		}

		public byte[] encrypt(byte[] in) {
			int length=in.length;
			if ((length % 8)!=0) {
				// add padding to the next 8
				length += (8-(length%8));
			}
			byte[] out = new byte[in.length];
			int offset=0;
			while (offset < in.length) {
				encrypt(in, offset, out, offset);
				offset+=8;
			}
			return out;
		}

		public byte[] decrypt(byte[] in) {
			byte[] out = new byte[blockSize];
			decrypt(in, 0, out, 0);
			return out;
		}

		// Helpers
		private int bytesToIntBig(byte[] in, int off) {
			return ((in[off++]) << 24) | ((in[off++] & 0xff) << 16)
					| ((in[off++] & 0xff) << 8) | ((in[off] & 0xff));
		}

		private void intToBytesBig(int x, byte[] out, int off) {
			out[off++] = (byte) (x >>> 24);
			out[off++] = (byte) (x >>> 16);
			out[off++] = (byte) (x >>> 8);
			out[off] = (byte) (x);
		}
	}

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
    		"XTEA",
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
			compareThread.interrupt();
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
                dialog.setCancelable(true);
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
    	
		File file=new File(filename);
    	if (!file.exists()) {
    		results="File does not exist.";
    		sendResults(results);
        	return;
    	}
    	int maxLength=10000000;
    	int fileSize=(int)file.length();
    	if (fileSize > maxLength) {
    		results="File larger than "+maxLength+" bytes.";
    		sendResults(results);
        	return;
    	}
    	int chunkSize=262144;
    	if (debug) Log.d(TAG,"PerformTests: fileSize="+fileSize+", chunkSize="+chunkSize);
    	results+="fileSize="+fileSize+", chunkSize="+chunkSize+"\n";
    	char[] buf=new char[chunkSize];
        for (int i=0; i<cipherAlgorithms.length; i++) {
        	try {
    			FileReader reader=new FileReader(filename);
    			int offset=0;
    			int numRead=0;
    			boolean result=true;
            	results += "Testing "+cipherAlgorithms[i]+"\n";
            	long startTime=System.currentTimeMillis();
            	
    			while ((numRead=reader.read(buf, 0, chunkSize))!=-1) {
    		    	if (debug) Log.d(TAG,"PerformTests: offset="+offset);
                	result = encryptTest(cipherAlgorithms[i],buf,"1234567890123456");
                	offset+=numRead;
                	if (result==false) {
                		results += "encryption failed";
                		break;
                	}
        	    	if (compareThread.isInterrupted()) {
        	    		return;
        	    	}
    			}

    			long endTime=System.currentTimeMillis();
    	    	long runTime=endTime-startTime;
    	    	results += "\nRun time: "+runTime+"ms\n";

            	results += "\n";
    			reader.close();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    			results=e.getLocalizedMessage();
        		sendResults(results);
            	return;
    		} catch (IOException e) {
    			e.printStackTrace();
    			results=e.getLocalizedMessage();
        		sendResults(results);
            	return;
    		}

    		sendResults(results);
        	
	    	if (compareThread.isInterrupted()) {
	    		return;
	    	}
        }
    }
    
    private void sendResults(String results) {
		Message m = new Message();
		m.what = EncryptCompare.MSG_COMPARE;
		Bundle b = new Bundle();
		b.putString("results", results);
		m.setData(b);
		EncryptCompare.this.myViewUpdateHandler.sendMessage(m); 
    }
    

    private boolean encryptTest(String algorithm, char[] buf, String password) {
    	if (debug) Log.d(TAG,"EncryptTest("+algorithm+")");
    	
	    int repetitions=1;
	    boolean result=false;;

	    if (algorithm.compareTo("XTEA")==0) {
			XTEA x = new XTEA();
			try {
				x.init(password.getBytes());
				byte[] plaintext = charsToBytes(buf);
				result=true;  // assume success
			    for (int i=0; i<repetitions; i++) {
			    	byte[] out = x.encrypt(plaintext);
			    	if (out.length != plaintext.length) {
			    		result=false;
			    	}
			    }
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}

    	} else {
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
	
			    for (int i=0; i<repetitions; i++) {
			        result=encryptBuffer(pbeCipher, pbeKey,pbeParamSpec, plaintext);
			    	if (compareThread.isInterrupted()) {
			    		return false;
			    	}
			    }
			} catch (NoSuchAlgorithmException e) {
				result=false;
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				result=false;
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				result=false;
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				result=false;
				e.printStackTrace();
			}
    	}

		return result;
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
