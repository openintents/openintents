package org.openintents.testing.peli.contentprovider01a;

// import org.openintents.main.R;
import org.openintents.testing.peli.contentprovider01a.R;

import android.app.Activity;
import android.database.Cursor;
import android.net.ContentURI;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContentProvider01a extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        // Get the base URI for contacts. 
	    ContentURI myPerson = new ContentURI(android.provider.Contacts.People.CONTENT_URI.toURI());
	
	    // Add the ID of the record I'm looking for (I'd have to know this somehow).
	    //myPerson.addId(1);
	
	    // Query for this record.
	    Cursor cur = managedQuery(myPerson, null, null, null);
	    
	    TextView text = (TextView) findViewById(R.id.mytext);
	    text.setText("Cursor: ");
	    text.append(cur.toString());
	    text.append("\n");
	    text.append("" + cur.count() + " rows");
	    
	    cur.first();
	    String[] cols = cur.getColumnNames();
	    for (int i=0; i<cols.length; i++)
	    {
	    	text.append("\n" + i);
	    	text.append("\n" + cols[i]);
	    	text.append("\n" + cur.getColumnName(i));
	    	text.append("\n" + cur.getString(i));
	    	//text.append("\n" + cur.getColumnName(i) + ": " + cur.getString(i));
	    };
	    
	    //text.append("\n" + cur.getString(1));
	    //text.append("\n" + cur.getInt(1));
	
	
	     // An array specifying which columns to return. 
	     // The provider exposes a list of column names it returns for a specific
	     // query, or you can get all columns and iterate through them. 
	     String[] projection = new String[] {
	         android.provider.BaseColumns._ID,
	         android.provider.Contacts.PeopleColumns.NAME,
	         android.provider.Contacts.PhonesColumns.NUMBER,
	         android.provider.Contacts.PeopleColumns.PHOTO//,
	         //android.provider.Contacts.PeopleColumns.NOTES
	     };

	     // Best way to retrieve a query; returns a managed query. 
	     Cursor managedCursor = managedQuery( android.provider.Contacts.Phones.CONTENT_URI,
	                             projection, //Which columns to return. 
	                             null,       // WHERE clause--we won't specify.
	                             android.provider.Contacts.PeopleColumns.NAME + " ASC"); // Order-by clause.

	     
	     
	    text.append("\n-----------");
	     cols = managedCursor.getColumnNames();
	     
	     managedCursor.first();
	    
	    text.append("\n" + managedCursor.count() + " rows");
	    for (int i=0; i<cols.length; i++)
	    {
	    	text.append("\n " + i);
	    	text.append("\n" + managedCursor.getColumnName(i));
	    	//text.append("\n" + managedCursor.getString(i));
	    	//text.append("\n" + managedCursor.getInt(i));
	    	//text.append("\n" + cur.getColumnName(i) + ": " + cur.getString(i));
	    };
	    //managedCursor.next();
	     
	     
	     
	     
	    managedCursor.first();
	    while (!managedCursor.isLast() && ! managedCursor.isAfterLast())
		{
		    for (int i=0; i<managedCursor.count(); i++)
		    {
		    	text.append("\n>> " + i);
		    	text.append("\n" + managedCursor.getColumnName(i));
		    	text.append("\n" + managedCursor.getString(i));
		    	text.append("\n" + managedCursor.getInt(i));
		    	//text.append("\n" + cur.getColumnName(i) + ": " + cur.getString(i));
		    };
		    managedCursor.next();
		}; 
		

    }
}