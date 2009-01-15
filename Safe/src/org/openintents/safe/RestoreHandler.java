/* $Id: RestoreHandler.java 71 2008-12-28 19:42:48Z peli0101 $
 * 
 * Copyright 2008 Randy McEoin
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import android.util.Log;

public class RestoreHandler extends DefaultHandler {

	private static boolean debug = false;
	private static final String TAG = "Restore";

    // ===========================================================
    // Fields
    // ===========================================================
    
    private boolean in_apws = false;
    private boolean in_masterkey = false;
    private boolean in_category = false;
    private boolean in_entry = false;
    private boolean in_description = false;
    private boolean in_website = false;
    private boolean in_username = false;
    private boolean in_password = false;
    private boolean in_note = false;
    
    private RestoreDataSet myRestoreDataSet = new RestoreDataSet();

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public RestoreDataSet getParsedData() {
         return this.myRestoreDataSet;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
         this.myRestoreDataSet = new RestoreDataSet();
    }

    @Override
    public void endDocument() throws SAXException {
         // Nothing to do
    }

	/** Called on opening tags like:
	* &lt;tag>
	* 
	* Can provide attribute(s) from xml like:
	* &lt;tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
		String qName, Attributes atts) throws SAXException {
		
		if (localName.equals("AndroidPasswordSafe")) {
			in_apws = true;
			String attrValue = atts.getValue("version");
			int version = Integer.parseInt(attrValue);

			String date = atts.getValue("date");

			myRestoreDataSet.setVersion(version);
			myRestoreDataSet.setDate(date);

			if (debug) Log.d(TAG,"found APWS "+version+" date "+date);
			
		}else if (in_apws && localName.equals("MasterKey")) {
			in_masterkey = true;

			if (debug) Log.d(TAG,"found MasterKey");

		}else if (in_apws && localName.equals("Category")) {
			in_category = true;

			String name = atts.getValue("name");
			myRestoreDataSet.newCategory(name);

			if (debug) Log.d(TAG,"found Category "+name);

		}else if (in_category && localName.equals("Entry")) {
			this.in_entry = true;

			myRestoreDataSet.newEntry();

			if (debug) Log.d(TAG,"found Entry");

		}else if (in_entry && localName.equals("Description")) {
			in_description = true;
		}else if (in_entry && localName.equals("Website")) {
			in_website = true;
		}else if (in_entry && localName.equals("Username")) {
			in_username = true;
		}else if (in_entry && localName.equals("Password")) {
			in_password = true;
		}else if (in_entry && localName.equals("Note")) {
			in_note = true;
		}
	}
    
	/** Called on closing tags like:
	 * &lt;/tag> 
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
	
		if (localName.equals("AndroidPasswordSafe")) {
			in_apws = false;
		}else if (in_apws && localName.equals("MasterKey")) {
			in_masterkey = false;
		}else if (in_apws && localName.equals("Category")) {
			in_category = false;
			
			myRestoreDataSet.storyCategory();
			
		}else if (in_category && localName.equals("Entry")) {
			in_entry = false;

			myRestoreDataSet.storeEntry();
			
		}else if (in_entry && localName.equals("Description")) {
			in_description = false;
		}else if (in_entry && localName.equals("Website")) {
			in_website = false;
		}else if (in_entry && localName.equals("Username")) {
			in_username = false;
		}else if (in_entry && localName.equals("Password")) {
			in_password = false;
		}else if (in_entry && localName.equals("Note")) {
			in_note = false;
		}
	}

	/** Called on the following structure:
	 * &lt;tag>characters&lt;/tag> */
	@Override
	public void characters(char ch[], int start, int length) {
		if (in_masterkey){
			myRestoreDataSet.setMasterKeyEncrypted(new String(ch, start, length));
		}
		if (in_description){
			myRestoreDataSet.setDescription(new String(ch, start, length));
		}
		if (in_website){
			myRestoreDataSet.setWebsite(new String(ch, start, length));
		}
		if (in_username){
			myRestoreDataSet.setUsername(new String(ch, start, length));
		}
		if (in_password){
			myRestoreDataSet.setPassword(new String(ch, start, length));
		}
		if (in_note){
			myRestoreDataSet.setNote(new String(ch, start, length));
		}
	} 
}
