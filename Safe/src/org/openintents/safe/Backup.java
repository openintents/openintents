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

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class Backup {

	private static boolean debug = false;
	private static final String TAG = "Backup";
	
	public static int CURRENT_VERSION = 1;
	
	private String result="";
	
	Context myCtx=null;

	public Backup(Context ctx) {
		myCtx=ctx;
	}

    public boolean write(String filename) {
    	if (debug) Log.d(TAG,"write("+filename+",)");
    	
		try {
            FileOutputStream str = new FileOutputStream(filename);
            org.xmlpull.v1.XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(str, "utf-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "OISafe");
            
            serializer.attribute(null, "version", Integer.toString(CURRENT_VERSION));
            
            Date today;
            String dateOut;
            DateFormat dateFormatter;

            dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
            					   DateFormat.FULL);
            today = new Date();
            dateOut = dateFormatter.format(today);

            serializer.attribute(null, "date", dateOut);
            
			DBHelper dbHelper=new DBHelper(myCtx);
			
			String masterKeyEncrypted = dbHelper.fetchMasterKey();
			serializer.startTag(null, "MasterKey");
			serializer.text(masterKeyEncrypted);
			serializer.endTag(null, "MasterKey");

			String salt = dbHelper.fetchSalt();
			serializer.startTag(null, "Salt");
			serializer.text(salt);
			serializer.endTag(null, "Salt");

			List<CategoryEntry> crows;
			crows = dbHelper.fetchAllCategoryRows();
			HashMap<Long, ArrayList<String>> packageAccess=dbHelper.fetchPackageAccessAll();
			
			int totalPasswords=0;

			for (CategoryEntry crow : crows) {

				serializer.startTag(null, "Category");
				serializer.attribute(null, "name", crow.name);

				List<PassEntry> rows;
				rows = dbHelper.fetchAllRows(crow.id);
	
				for (PassEntry row : rows) {
					totalPasswords++;
					
					serializer.startTag(null, "Entry");

					serializer.startTag(null, "RowID");
					serializer.text(Long.toString(row.id));
					serializer.endTag(null, "RowID");

					serializer.startTag(null, "Description");
					serializer.text(row.description);
					serializer.endTag(null, "Description");
					
					serializer.startTag(null, "Website");
					serializer.text(row.website);
					serializer.endTag(null, "Website");
					
					serializer.startTag(null, "Username");
					serializer.text(row.username);
					serializer.endTag(null, "Username");
					
					serializer.startTag(null, "Password");
					serializer.text(row.password);
					serializer.endTag(null, "Password");

					serializer.startTag(null, "Note");
					serializer.text(row.note);
					serializer.endTag(null, "Note");

					if (row.uniqueName!=null) {
						serializer.startTag(null, "UniqueName");
						serializer.text(row.uniqueName);
						serializer.endTag(null, "UniqueName");
					}
					
					if(packageAccess.containsKey(row.id)) {
						serializer.startTag(null, "PackageAccess");
						serializer.text(packageAccess.get(row.id).toString());
						serializer.endTag(null, "PackageAccess");
					}

					serializer.endTag(null, "Entry");
				}
				serializer.endTag(null, "Category");
			}

			serializer.endTag(null, "OISafe");
			serializer.endDocument();

			dbHelper.close();

			result=myCtx.getString(R.string.backup_complete)+" "+
				Integer.toString(totalPasswords);
		} catch (IOException e) {
			e.printStackTrace();
			result=myCtx.getString(R.string.backup_failed)+" "+
				e.getLocalizedMessage();
			return false;
		}
		return true;
    }
    public String getResult() {
    	return result;
    }
}
