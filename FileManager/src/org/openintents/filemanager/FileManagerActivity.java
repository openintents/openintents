package org.openintents.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileManagerActivity extends ListActivity { 
	private static final String TAG = "FileManagerActivity";

     private List<IconifiedText> directoryEntries = new ArrayList<IconifiedText>(); 
     private File currentDirectory = new File("/sdcard"); 
     
     private MimeTypes mMimeTypes;

     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle icicle) { 
          super.onCreate(icicle); 
          
          // Create map of extensions:
          getMimeTypes();
			
          browseToRoot(); 
     }

	/**
	 * 
	 */
     private void getMimeTypes() {
    	 MimeTypeParser mtp = new MimeTypeParser();

    	 XmlResourceParser in = this.getResources().getXml(R.xml.mimetypes);

    	 try {
    		 mMimeTypes = mtp.fromXmlResource(in);
    	 } catch (XmlPullParserException e) {
    		 Log
    		 .e(
    				 TAG,
    				 "PreselectedChannelsActivity: XmlPullParserException",
    				 e);
    		 throw new RuntimeException(
    		 "PreselectedChannelsActivity: XmlPullParserException");
    	 } catch (IOException e) {
    		 Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
    		 throw new RuntimeException(
    		 "PreselectedChannelsActivity: IOException");
    	 }
     } 
      
     /** 
      * This function browses to the 
      * root-directory of the file-system. 
      */ 
     private void browseToRoot() { 
          browseTo(new File("/")); 
    } 
      
     /** 
      * This function browses up one level 
      * according to the field: currentDirectory 
      */ 
     private void upOneLevel(){ 
          if(this.currentDirectory.getParent() != null) 
               this.browseTo(this.currentDirectory.getParentFile()); 
     } 
      
     private void browseTo(final File aDirectory){ 
          this.setTitle(aDirectory.getAbsolutePath()); 
          
          if (aDirectory.isDirectory()){ 
               this.currentDirectory = aDirectory; 
               fill(aDirectory.listFiles()); 
          }else{ 

              // Lets start an intent to View the file, that was clicked... 
        	  FileManagerActivity.this.openFile(aDirectory); 
        	 
          } 
     } 
      
     private void openFile(File aFile) { 
          Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

          Uri data = Uri.parse("file://" + aFile.getAbsolutePath());
          String type = mMimeTypes.getMimeType(aFile.getName());
          intent.setDataAndType(data, type);
          
          try {
        	  startActivity(intent); 
          } catch (ActivityNotFoundException e) {
        	  Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
          };
     } 

     private void fill(File[] files) { 
          this.directoryEntries.clear(); 
           
          // Add the "." == "current directory" 
          /*this.directoryEntries.add(new IconifiedText( 
                    getString(R.string.current_dir), 
                    getResources().getDrawable(R.drawable.ic_launcher_folder)));        */
          // and the ".." == 'Up one level' 
          if(this.currentDirectory.getParent() != null) 
               this.directoryEntries.add(new IconifiedText( 
                         getString(R.string.up_one_level), 
                         getResources().getDrawable(R.drawable.ic_launcher_folder_open))); 
           
          Drawable currentIcon = null; 
          for (File currentFile : files){ 
               if (currentFile.isDirectory()) { 
                    currentIcon = getResources().getDrawable(R.drawable.ic_launcher_folder); 
               }else{ 
                    String fileName = currentFile.getName(); 
                    
                    String mimetype = mMimeTypes.getMimeType(fileName);
                    
                    currentIcon = getDrawableForMimetype(mimetype);
                    if (currentIcon == null) {
                    	currentIcon = getResources().getDrawable(R.drawable.icon_file);
                    }
               } 
               this.directoryEntries.add(new IconifiedText( 
              		 currentFile.getName(), currentIcon)); 
          } 
          Collections.sort(this.directoryEntries); 
           
          IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this); 
          itla.setListItems(this.directoryEntries);          
          this.setListAdapter(itla); 
     } 

     @Override 
     protected void onListItemClick(ListView l, View v, int position, long id) { 
          super.onListItemClick(l, v, position, id); 

          String selectedFileString = this.directoryEntries.get(position) 
                    .getText(); 
          if (selectedFileString.equals(getString(R.string.up_one_level))) { 
               this.upOneLevel(); 
          } else { 
        	  String curdir = this.currentDirectory 
              .getAbsolutePath() ;
        	  String file = this.directoryEntries.get(position) 
              .getText();
        	  String separator = "/";
        	  if (curdir.endsWith("/")) {
        		  separator = "";
        	  }
               File clickedFile = new File(curdir + separator
                                   + file);
               if (clickedFile != null) 
                    this.browseTo(clickedFile); 
          } 
     } 

     
     /**
      * Return the Drawable that is associated with a specific mime type
      * for the VIEW action.
      * 
      * @param mimetype
      * @return
      */
     Drawable getDrawableForMimetype(String mimetype) {
    	 PackageManager pm = getPackageManager();
    	 
    	 Intent intent = new Intent(Intent.ACTION_VIEW);
    	 intent.setType(mimetype);
    	 
    	 final List<ResolveInfo> lri = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    	 
    	 if (lri != null && lri.size() > 0) {
    		 // return first element
    		 final ResolveInfo ri = lri.get(0);
    		 return ri.loadIcon(pm);
    	 }
    	 
    	 return null;
     }
}