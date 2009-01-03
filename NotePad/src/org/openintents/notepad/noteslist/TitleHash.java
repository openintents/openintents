package org.openintents.notepad.noteslist;

import java.util.HashMap;

import org.openintents.notepad.R;

import android.content.Context;

public class TitleHash {

	Context mContext;
	
	/**
	 * Map encrypted titles to decrypted ones.
	 */
	static HashMap<String,String> mTitleHashMap = new HashMap<String,String>();
	
	public TitleHash(Context context) {
		mContext = context;
	}
	
	/**
	 * Return the decrypted title, or null.
	 * @param encryptedTitle
	 * @return
	 */
    public String getDecryptedTitle(String encryptedTitle) {
    	return mTitleHashMap.get(encryptedTitle);
    	/*if (mTitleHashMap.containsKey(encryptedTitle)) {
    		return mTitleHashMap.get(encryptedTitle);
    	} else {
    		return mContext.getString(R.string.encrypted);
    	}*/
    }
    
    public void put(String encryptedTitle, String decryptedTitle) {
    	mTitleHashMap.put(encryptedTitle, decryptedTitle);
    }
    
    public void flush() {
    	mTitleHashMap = new HashMap<String,String>();
    }
}
