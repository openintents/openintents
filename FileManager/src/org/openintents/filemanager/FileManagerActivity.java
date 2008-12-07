package org.openintents.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.intents.FileManagerIntents;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class FileManagerActivity extends ListActivity { 
	private static final String TAG = "FileManagerActivity";

	private int mState;
	
	private static final int STATE_BROWSE = 1;
	private static final int STATE_PICK = 2;
	
	
	/** Contains directries and files together */
     private List<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();

     /** Dir separate for sorting */
     List<IconifiedText> mListDir = new ArrayList<IconifiedText>();
     
     /** Files separate for sorting */
     List<IconifiedText> mListFile = new ArrayList<IconifiedText>();
     
     /** SD card separate for sorting */
     List<IconifiedText> mListSdCard = new ArrayList<IconifiedText>();
     
     private File currentDirectory = new File(""); 
     
     private String mSdCardPath = "";
     
     private MimeTypes mMimeTypes;
     
     private EditText mEditFilename;
     private Button mButtonPick;
     private LinearLayout mDirectoryButtons;

     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle icicle) { 
          super.onCreate(icicle); 
          
          setContentView(R.layout.filelist);
          
          mDirectoryButtons = (LinearLayout) findViewById(R.id.directory_buttons);
          mEditFilename = (EditText) findViewById(R.id.filename);
          

          mButtonPick = (Button) findViewById(R.id.button_pick);
          
          mButtonPick.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					pickFile();
				}
          });
          
          // Create map of extensions:
          getMimeTypes();
          
          getSdCardPath();
          
          mState = STATE_BROWSE;
          
          Intent intent = getIntent();
          String action = intent.getAction();
          
          File browseto = new File("/");
          
          if (action != null && action.equals(FileManagerIntents.ACTION_PICK_FILE)) {
        	  mState = STATE_PICK;
        	  
        	  File file = FileUtils.getFile(intent.getData());
        	  if (file != null) {
        		  
        		  browseto = FileUtils.getPathWithoutFilename(file);
        		  
        		  mEditFilename.setText(file.getName());
        	  } else {
        		  
        	  }
        	  
        	  String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
        	  if (title != null) {
        		  setTitle(title);
        	  }
          } else {
        	  mState = STATE_BROWSE;
         	 
        	  mEditFilename.setVisibility(View.GONE);
        	  mButtonPick.setVisibility(View.GONE);
          }
          
          browseTo(browseto);
     }

     private void pickFile() {
    	 String filename = mEditFilename.getText().toString();
    	 File file = FileUtils.getFile(currentDirectory.getAbsolutePath(), filename);
    	 
    	 Intent intent = getIntent();
    	 intent.setData(FileUtils.getUri(file));
    	 setResult(RESULT_OK, intent);
    	 finish();
     }
     
	/**
	 * 
	 */
     private void getMimeTypes() {
    	 MimeTypeParser mtp = new MimeTypeParser();

    	 XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

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
      * This function browses up one level 
      * according to the field: currentDirectory 
      */ 
     private void upOneLevel(){ 
          if(currentDirectory.getParent() != null) 
               browseTo(currentDirectory.getParentFile()); 
     } 
      
     private void browseTo(final File aDirectory){ 
          // setTitle(aDirectory.getAbsolutePath());
          
          Log.i(TAG, "browse to: " + aDirectory.getAbsoluteFile());
          
          if (aDirectory.isDirectory()){
               currentDirectory = aDirectory;
               fill(aDirectory.listFiles());
               setDirectoryButtons();
          }else{ 

        	  if (mState == STATE_BROWSE) {
	              // Lets start an intent to View the file, that was clicked... 
	        	  openFile(aDirectory); 
        	  } else if (mState == STATE_PICK) {
        		  // Pick the file
        		  mEditFilename.setText(aDirectory.getName());
        	  }
          } 
     } 
      
     private void openFile(File aFile) { 
          Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

          Uri data = FileUtils.getUri(aFile);
          String type = mMimeTypes.getMimeType(aFile.getName());
          intent.setDataAndType(data, type);
          
          try {
        	  startActivity(intent); 
          } catch (ActivityNotFoundException e) {
        	  Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
          };
     } 

     
     
     private void fill(File[] files) { 
          directoryEntries.clear(); 
          mListDir.clear();
          mListFile.clear();
          mListSdCard.clear();
          
           
          // Add the "." == "current directory" 
          /*directoryEntries.add(new IconifiedText( 
                    getString(R.string.current_dir), 
                    getResources().getDrawable(R.drawable.ic_launcher_folder)));        */
          // and the ".." == 'Up one level' 
          /*
          if(currentDirectory.getParent() != null) 
               directoryEntries.add(new IconifiedText( 
                         getString(R.string.up_one_level), 
                         getResources().getDrawable(R.drawable.ic_launcher_folder_open))); 
          */
           
          Drawable currentIcon = null; 
          for (File currentFile : files){ 
               if (currentFile.isDirectory()) { 
            	   if (currentFile.getAbsolutePath().equals(mSdCardPath)) {
            		   currentIcon = getResources().getDrawable(R.drawable.icon_sdcard);
            		   
                       mListSdCard.add(new IconifiedText( 
                       		 currentFile.getName(), currentIcon)); 
            	   } else {
            		   currentIcon = getResources().getDrawable(R.drawable.ic_launcher_folder);

                       mListDir.add(new IconifiedText( 
                         		 currentFile.getName(), currentIcon)); 
            	   }
               }else{ 
                    String fileName = currentFile.getName(); 
                    
                    String mimetype = mMimeTypes.getMimeType(fileName);
                    
                    currentIcon = getDrawableForMimetype(mimetype);
                    if (currentIcon == null) {
                    	currentIcon = getResources().getDrawable(R.drawable.icon_file);
                    }
                    mListFile.add(new IconifiedText( 
                     		 currentFile.getName(), currentIcon)); 
               } 
               
          } 
          //Collections.sort(mListSdCard); 
          Collections.sort(mListDir); 
          Collections.sort(mListFile); 

          addAllElements(directoryEntries, mListSdCard);
          addAllElements(directoryEntries, mListDir);
          addAllElements(directoryEntries, mListFile);
           
          IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this); 
          itla.setListItems(directoryEntries);          
          setListAdapter(itla); 
     } 
     
     private void addAllElements(List<IconifiedText> addTo, List<IconifiedText> addFrom) {
    	 int size = addFrom.size();
    	 for (int i = 0; i < size; i++) {
    		 addTo.add(addFrom.get(i));
    	 }
     }
     
     private void setDirectoryButtons() {
    	 String[] parts = currentDirectory.getAbsolutePath().split("/");
    	 
    	 mDirectoryButtons.removeAllViews();
    	 
    	 int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
    	 
    	 
    	 // Add home button separately
    	 ImageButton ib = new ImageButton(this);
    	 ib.setImageResource(R.drawable.ic_launcher_home_small);
		 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
		 ib.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					browseTo(new File("/"));
				}
		 });
		 mDirectoryButtons.addView(ib);
		 
    	 // Add other buttons
    	 
    	 String dir = "";
    	 
    	 for (int i = 1; i < parts.length; i++) {
    		 dir += "/" + parts[i];
    		 if (dir.equals(mSdCardPath)) {
    			 // Add SD card button
    			 ib = new ImageButton(this);
    	    	 ib.setImageResource(R.drawable.icon_sdcard_small);
    			 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    			 ib.setOnClickListener(new View.OnClickListener() {
    					public void onClick(View view) {
    						browseTo(new File(mSdCardPath));
    					}
    			 });
    			 mDirectoryButtons.addView(ib);
    		 } else {
	    		 Button b = new Button(this);
	    		 b.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
	    		 b.setText(parts[i]);
	    		 b.setTag(dir);
	    		 b.setOnClickListener(new View.OnClickListener() {
	 				public void onClick(View view) {
	 					String dir = (String) view.getTag();
	 					browseTo(new File(dir));
	 				}
	    		 });
	    		 mDirectoryButtons.addView(b);
    		 }
    	 }
     }

     @Override 
     protected void onListItemClick(ListView l, View v, int position, long id) { 
          super.onListItemClick(l, v, position, id); 

          String selectedFileString = directoryEntries.get(position) 
                    .getText(); 
          if (selectedFileString.equals(getString(R.string.up_one_level))) { 
               upOneLevel(); 
          } else { 
        	  String curdir = currentDirectory 
              .getAbsolutePath() ;
        	  String file = directoryEntries.get(position) 
              .getText();
        	  File clickedFile = FileUtils.getFile(curdir, file);
               if (clickedFile != null) 
                    browseTo(clickedFile); 
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
     
     private void getSdCardPath() {
    	 mSdCardPath = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath();
     }
}