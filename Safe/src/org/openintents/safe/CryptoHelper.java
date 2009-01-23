/* $Id$
 * 
 * Copyright 2007-2008 Steven Osborn
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
package org.openintents.safe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.util.Log;

/**
 * Crypto helper class.
 * 
 * Basic crypto class that uses Bouncy Castle Provider to
 * encrypt/decrypt data using PBE (Password Based Encryption) via
 * 128Bit AES. I'm fairly new to both Crypto and Java so if you 
 * notice I've done something terribly wrong here please let me
 * know.
 *
 * @author Steven Osborn - http://steven.bitsetters.com
 */
public class CryptoHelper {

	private static final boolean debug = true;
    private static String TAG = "CryptoHelper";
    protected static PBEKeySpec pbeKeySpec;
    protected static PBEParameterSpec pbeParamSpec;
    protected static SecretKeyFactory keyFac;

    public final static int EncryptionMedium=1;
    public final static int EncryptionStrong=2;

    protected static String algorithmMedium = "PBEWithMD5And128BitAES-CBC-OpenSSL";
//  protected static String algorithm = "PBEWithSHA1And128BitAES-CBC-BC";  // slower
	protected static String algorithmStrong = "PBEWithSHA1And256BitAES-CBC-BC";
	private String algorithm = "";
    protected static String desAlgorithm = "DES";
    protected static String password = null;
    protected static SecretKey pbeKey;
    protected static Cipher pbeCipher;
    private boolean status=false;	// status of the last encrypt/decrypt

    private static byte[] salt = null; 

    private static final int count = 20;

    /**
     * Constructor which defaults to a medium encryption level.
     */
    public CryptoHelper() {
//    	initialize(EncryptionMedium);
    }
    /**
     * Constructor which allows the specification of the encryption level.
     * 
     * @param strength encryption strength
     * @param salt salt to be used
     */
    public void init(int strength, String salt) throws CryptoHelperException {
    	try {
			setSalt(salt);
	        initialize(strength);
		} catch (CryptoHelperException e) {
			e.printStackTrace();
			throw e;
		}
    }
    /**
     * Initialize the class.  Sets the encryption level for the instance
     * and generates the secret key factory.
     * 
     * @param Strength
     */
    private void initialize(int Strength) {
    	switch (Strength) {
    	case EncryptionMedium:
    		algorithm=algorithmMedium;
    		break;
    	case EncryptionStrong:
    		algorithm=algorithmStrong;
    		break;
    	}
		pbeParamSpec = new PBEParameterSpec(salt,count);
		try {
		    keyFac = SecretKeyFactory
		    .getInstance(algorithm,"BC");
		} catch (NoSuchAlgorithmException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());
		} catch (NoSuchProviderException e) {
		    Log.e(TAG,"CryptoHelper(): "+e.toString());		
		}
    }

    /**
     * Generate a random salt for use with the cipher.
     * 
     * @author Randy McEoin
     * @return String version of the 8 byte salt
     */
    public static String generateSalt() throws NoSuchAlgorithmException {
    	byte[] salt = new byte[8];
    	SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
			sr.nextBytes(salt);
			if (debug) Log.d(TAG,"generateSalt: salt="+salt.toString());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw e;
		}
		return toHexString(salt);
    }
    /**
     * @author Isaac Potoczny-Jones
     * 
     * @return null if failure, otherwise hex string version of key
     */
	public static String generateMasterKey () throws NoSuchAlgorithmException {
		try {
			KeyGenerator keygen;
			keygen = KeyGenerator.getInstance("AES");
			keygen.init(256);
			SecretKey genDesKey = keygen.generateKey();
			return toHexString(genDesKey.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"generateMasterKey(): "+e.toString());
			throw e;
		}
	}

    /**
     * 
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] md5String(String message) {
	
		byte[] input = message.getBytes();
	
		MessageDigest hash;
		ByteArrayInputStream	bIn = null;
		DigestInputStream	dIn = null;
	
		try {
		    hash = MessageDigest.getInstance("MD5");
	
		    bIn = new ByteArrayInputStream(input);
		    dIn = new DigestInputStream(bIn, hash);
	
		    for(int i=0;i<input.length;i++) {
		    	dIn.read();
		    }
	
		} catch (NoSuchAlgorithmException e) {
		    Log.e(TAG,"md5String(): "+e.toString());
		} catch (IOException e) {
		    Log.e(TAG,"md5String(): "+e.toString());
		}
	
		return dIn.getMessageDigest().digest();
    }

    /**
     * 
     * @param bytes
     * @return
     */
    public static String toHexString(byte bytes[]) {
	
		StringBuffer retString = new StringBuffer();
		for (int i = 0; i < bytes.length; ++i) {
		    retString.append(Integer
			    .toHexString(0x0100 + (bytes[i] & 0x00FF))
			    .substring(1));
		}
		return retString.toString();
    }
    
    public static byte[] hexStringToBytes(String hex) {
    	
    	byte [] bytes = new byte [hex.length() / 2];
		int j = 0;
		for (int i = 0; i < hex.length(); i += 2)
		{
			try {
				String hexByte=hex.substring(i, i+2);

				Integer I = new Integer (0);
				I = Integer.decode("0x"+hexByte);
				int k = I.intValue ();
				bytes[j++] = new Integer(k).byteValue();
			} catch (NumberFormatException e)
			{
				Log.i(TAG,e.getLocalizedMessage());
				return bytes;
			} catch (StringIndexOutOfBoundsException e)
			{
				Log.i(TAG,"StringIndexOutOfBoundsException");
				return bytes;
			}
		}
    	return bytes;
    }

    /**
     * Set the password to be used as an encryption key
     * 
     * @param pass - might be a user-entered key, or one generated by generateMasterKey.
     * @throws Exception
     */
    public void setPassword(String pass) {
    	if (debug) Log.d(TAG,"setPassword("+pass+")");
		password = pass;
		pbeKeySpec = new PBEKeySpec(password.toCharArray());
		try {
		    pbeKey = keyFac.generateSecret(pbeKeySpec);
		    pbeCipher = Cipher
		    .getInstance(algorithm,"BC");
		} catch (InvalidKeySpecException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		} catch (NoSuchAlgorithmException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		} catch (NoSuchProviderException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		} catch (NoSuchPaddingException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		}
    }

    private void setSalt(String saltIn) throws CryptoHelperException {
    	if (saltIn==null) {
			String msg = "Salt must not be null.";
		    throw new CryptoHelperException(msg);
    	}
		byte[] byteSaltIn=hexStringToBytes(saltIn);

		if (byteSaltIn.length != 8) {
			String msg = "Salt must be 8 bytes in length.";
		    throw new CryptoHelperException(msg);
		}
		salt=byteSaltIn;
		if (debug) Log.d(TAG,"setSalt: salt="+toHexString(salt));
    }
    /**
     * encrypt a string
     * 
     * @param plaintext
     * @return encrypted String
     * @throws Exception
     */
    public String encrypt(String plaintext) throws CryptoHelperException {
		status=false; // assume failure
		if(password == null) {
		    String msg = "Must call setPassword before running encrypt.";
		    throw new CryptoHelperException(msg);
		}
		if (salt==null) {
		    String msg = "Must call setSalt before running encrypt.";
		    throw new CryptoHelperException(msg);
		}
		byte[] ciphertext = {};
	
		try {
		    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		    ciphertext = pbeCipher.doFinal(plaintext.getBytes());
		    status=true;
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"encrypt(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"encrypt(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"encrypt(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"encrypt(): "+e.toString());
		}
	
		String stringCiphertext=toHexString(ciphertext);
		return stringCiphertext;
    }

    /**
     * unencrypt previously encrypted string
     * 
     * @param ciphertext
     * @return decrypted String
     * @throws Exception
     */
    public String decrypt(String ciphertext) throws CryptoHelperException {
		status=false; // assume failure
		if(password == null) {
		    String msg = "Must call setPassword before running decrypt.";
		    throw new CryptoHelperException(msg);
		}
		if (salt==null) {
		    String msg = "Must call setSalt before running decrypt.";
		    throw new CryptoHelperException(msg);
		}
	
		if ((ciphertext==null) || (ciphertext=="")) {
			return "";
		}
		byte[] byteCiphertext=hexStringToBytes(ciphertext);
		byte[] plaintext = {};
		
		try {
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		    plaintext = pbeCipher.doFinal(byteCiphertext);
		    status=true;
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		}
	
		return new String(plaintext);
    }

    /**
     * Status of the last encrypt or decrypt.
     * 
     * @return true if last operation was successful
     */
    public boolean getStatus() {
    	return status;
    }
    

    /**
     * encrypt a string using a random session key
     * 
     * @author Peli
     * 
     * @param plaintext
     * @return encrypted String
     * @throws Exception
     */
    public String encryptWithSessionKey(String plaintext) throws CryptoHelperException {
    	Log.i(TAG, "Encrypt with session key");
		status=false; // assume failure
		if(password == null) {
		    String msg = "Must call setPassword before runing encrypt.";
		    throw new CryptoHelperException(msg);
		}
		byte[] cipherSessionKey = {};
		byte[] ciphertext = {};
		
		// First create a session key
		SecretKey sessionKey = null;
		byte[] sessionKeyEncoded = null;
		String sessionKeyString = null;
		try {
			KeyGenerator keygen;
			keygen = KeyGenerator.getInstance("AES");
			keygen.init(256); // needs 96 bytes
			//keygen.init(128); // needs 64 bytes
			sessionKey = keygen.generateKey();
			sessionKeyEncoded = sessionKey.getEncoded();
			sessionKeyString = new String(sessionKeyEncoded);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG,"generateMasterKey(): "+e.toString());
		}

		// Convert this to a Pbe key
		PBEKeySpec sessionPbeKeySpec = new PBEKeySpec(sessionKeyString.toCharArray());
		SecretKey sessionPbeKey = null;
		try {
		    sessionPbeKey = keyFac.generateSecret(sessionPbeKeySpec);
		} catch (InvalidKeySpecException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		}
		    
		// Encrypt the session key using the master key
	    try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		    cipherSessionKey = pbeCipher.doFinal(sessionKeyEncoded);
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"encryptWithSessionKey(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"encryptWithSessionKey(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"encryptWithSessionKey(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"encryptWithSessionKey(): "+e.toString());
		}
		
		// Now encrypt the text using the session key
	    try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, sessionPbeKey, pbeParamSpec);
		    ciphertext = pbeCipher.doFinal(plaintext.getBytes());
		    status=true;
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"encryptWithSessionKey2(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"encryptWithSessionKey2(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"encryptWithSessionKey2(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"encryptWithSessionKey2(): "+e.toString());
		}
	
		String stringCipherVersion = "A";
		String stringCipherSessionKey = toHexString(cipherSessionKey);
		String stringCiphertext=toHexString(ciphertext);
		Log.i(TAG, "Length: " + stringCipherSessionKey.length() + ", " + stringCipherSessionKey);
		
		StringBuilder sb = new StringBuilder(stringCipherVersion.length() 
				+ stringCipherSessionKey.length() 
				+ stringCiphertext.length());
		sb.append(stringCipherVersion);
		sb.append(stringCipherSessionKey);
		sb.append(stringCiphertext);
		return sb.toString();
    }

    /**
     * unencrypt encrypted string previously encrypted with
     * encryptWithSessionKey()
     * 
     * @author Peli
     * 
     * @param ciphertext
     * @return decrypted String
     * @throws Exception
     */
    public String decryptWithSessionKey(String ciphertext) throws CryptoHelperException {
		status=false; // assume failure
		if(password == null) {
		    String msg = "Must call setPassword before running decrypt.";
		    throw new CryptoHelperException(msg);
		}
	
		if ((ciphertext==null) || (ciphertext=="")) {
			return "";
		}
		String cipherVersion = null;
		String cipherSessionKey = null;
		
		// Split cipher into session key and text
		try {
			cipherVersion = ciphertext.substring(0,1);
			if (cipherVersion.equals("A")) {
				cipherSessionKey = ciphertext.substring(1,97); // 64 if init(128) had been chosen
				ciphertext = ciphertext.substring(97);
			} else {
				Log.e(TAG, "Unknown cipher version" + cipherVersion);
				return "";
			}
		} catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "Invalid ciphertext (with session key)");
			return "";
		}
		
		// Decrypt the session key
		byte[] byteCipherSessionKey=hexStringToBytes(cipherSessionKey);
		byte[] byteSessionKey = {};
		
		try {
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		    byteSessionKey = pbeCipher.doFinal(byteCipherSessionKey);
		    status=true;
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		}

		// Convert the session key into a Pbe key
		String stringSessionKey = new String(byteSessionKey);
		PBEKeySpec sessionPbeKeySpec = new PBEKeySpec(stringSessionKey.toCharArray());
		SecretKey sessionPbeKey = null;
		try {
		    sessionPbeKey = keyFac.generateSecret(sessionPbeKeySpec);
		} catch (InvalidKeySpecException e) {
		    Log.e(TAG,"setPassword(): "+e.toString());
		}

		// Use the session key to decrypt the text
		byte[] byteCiphertext=hexStringToBytes(ciphertext);
		byte[] plaintext = {};
		
		try {
		    pbeCipher.init(Cipher.DECRYPT_MODE, sessionPbeKey, pbeParamSpec);
		    plaintext = pbeCipher.doFinal(byteCiphertext);
		    status=true;
		} catch (IllegalBlockSizeException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (BadPaddingException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidKeyException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		} catch (InvalidAlgorithmParameterException e) {
		    Log.e(TAG,"decrypt(): "+e.toString());
		}
		
		return new String(plaintext);
    }
}
