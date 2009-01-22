/* $Id$
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

import java.util.ArrayList;

import android.util.Log;

public class RestoreDataSet {

	private static boolean debug = false;
	private static final String TAG = "RestoreDataSet";

	private int version = 0;
	private String date = null;
	private String salt = null;
	private String masterKeyEncrypted = null;
	private Long currentCategoryId = new Long(0);
	private CategoryEntry currentCategory = null;
	private ArrayList<CategoryEntry> categoryEntries = new ArrayList<CategoryEntry>();
	private PassEntry currentEntry = null;
	private ArrayList<PassEntry> passEntries = new ArrayList<PassEntry>();
	private int totalEntries = 0;
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int extractedVersion) {
		version = extractedVersion;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String extractedDate) {
		date = extractedDate;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String extractedSalt) {
		salt = extractedSalt;
	}
	public String getMasterKeyEncrypted() {
		return masterKeyEncrypted;
	}
	public void setMasterKeyEncrypted(String extractedKey) {
		masterKeyEncrypted = extractedKey;
	}
	public ArrayList<CategoryEntry> getCategories() {
		return categoryEntries;
	}
	public void newCategory(String extractedCategory) {
		currentCategory = new CategoryEntry();
		currentCategoryId++;
		currentCategory.id = currentCategoryId;
		currentCategory.name = extractedCategory;
	}
	public void storyCategory() {
		if (currentCategory != null) {
			categoryEntries.add(currentCategory);
			currentCategory=null;
		}
	}
	public ArrayList<PassEntry> getPass() {
		return passEntries;
	}
	public void newEntry() {
		currentEntry = new PassEntry();
		currentEntry.category = currentCategoryId;
		currentEntry.description="";
		currentEntry.website="";
		currentEntry.username="";
		currentEntry.password="";
		currentEntry.note="";
	}
	public void storeEntry() {
		// only add an entry if we had all the fields
		if (debug) Log.d(TAG,currentEntry.description+" "+currentEntry.website+" "+
				currentEntry.username+" "+currentEntry.password+" "+
				currentEntry.note);
		if ((currentEntry != null) &&
			(currentEntry.description!="") &&
			(currentEntry.website!="") &&
			(currentEntry.username!="") &&
			(currentEntry.password!="") &&
			(currentEntry.note!="")) {
			passEntries.add(currentEntry);
			totalEntries++;
		}
		currentEntry = null;
	}
	public int getTotalEntries() {
		return totalEntries;
	}
	public void setDescription(String extractedDescription) {
		if (debug) Log.d(TAG,"setDescription("+extractedDescription+")");
		if (currentEntry != null) {
			currentEntry.description += extractedDescription;
		}
	}
	public void setWebsite(String extractedWebsite) {
		if (debug) Log.d(TAG,"setWebsite("+extractedWebsite+")");
		if (currentEntry != null) {
			currentEntry.website += extractedWebsite;
		}
	}
	public void setUsername(String extractedUsername) {
		if (currentEntry != null) {
			currentEntry.username += extractedUsername;
		}
	}
	public void setPassword(String extractedPassword) {
		if (currentEntry != null) {
			currentEntry.password += extractedPassword;
		}
	}
	public void setNote(String extractedNote) {
		if (currentEntry != null) {
			currentEntry.note += extractedNote;
		}
	}
}
