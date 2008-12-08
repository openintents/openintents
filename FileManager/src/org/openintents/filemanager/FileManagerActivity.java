package org.openintents.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.UpdateMenu;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FileManagerActivity extends ListActivity { 
	private static final String TAG = "FileManagerActivity";

	private int mState;
	
	private static final int STATE_BROWSE = 1;
	private static final int STATE_PICK = 2;
	

	private static final int MENU_ABOUT = Menu.FIRST + 1;
	private static final int MENU_UPDATE = Menu.FIRST + 2;
	private static final int MENU_PREFERENCES = Menu.FIRST + 3;
	private static final int MENU_NEW_FOLDER = Menu.FIRST + 4;
	private static final int MENU_DELETE = Menu.FIRST + 5;
	private static final int MENU_RENAME = Menu.FIRST + 6;
	
	private static final int DIALOG_NEW_FOLDER = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_RENAME = 3;
	
	private static final String BUNDLE_CURRENT_DIRECTORY = "current_directory";
	private static final String BUNDLE_CONTEXT_FILE = "context_file";
	private static final String BUNDLE_CONTEXT_TEXT = "context_text";
	
	/** Contains directories and files together */
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

     private String mContextText;
     private File mContextFile = new File("");
     private Drawable mContextIcon;
     
     private EditText mEditFilename;
     private Button mButtonPick;
     private LinearLayout mDirectoryButtons;

     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle icicle) { 
          super.onCreate(icicle); 
          
          setContentView(R.layout.filelist);

		  getListView().setOnCreateContextMenuListener(this);
		  getListView().setEmptyView(findViewById(R.id.empty));
  		
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
          
          if (!TextUtils.isEmpty(mSdCardPath)) {
        	  browseto = new File(mSdCardPath);
          }
          
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
          
          if (icicle != null) {
        	  browseto = new File(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
        	  mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
        	  mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);
          }
          
          browseTo(browseto);
     }

     
     /*
     @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
*/
     

 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		
 		// remember file name
 		outState.putString(BUNDLE_CURRENT_DIRECTORY, currentDirectory.getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_FILE, mContextFile.getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_TEXT, mContextText);
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
               refreshList();
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

     
     
     private void refreshList() {
    	 
    	  File[] files = currentDirectory.listFiles();
          
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
    	 
    	 checkButtonLayout();
     }

     private void checkButtonLayout() {
    	 
    	 // Let's measure how much space we need:
    	 //int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    	 int spec = View.MeasureSpec.UNSPECIFIED;
    	 mDirectoryButtons.measure(spec, spec);
    	 int count = mDirectoryButtons.getChildCount();
    	 
    	 int requiredwidth = mDirectoryButtons.getMeasuredWidth();
    	 int width = getWindowManager().getDefaultDisplay().getWidth();
    	 //Log.i(TAG, "more: " + );
    	 //Log.i(TAG, "more: " + mDirectoryButtons.getChildAt(count - 1).getMeasuredWidth());
    	 //Log.i(TAG, "lastpos: " + lastpos + ", width: " + width);
    	 
    	 if (requiredwidth > width) {
        	 int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        	 
        	 // Create a new button that shows that there is more to the left:
        	 ImageButton ib = new ImageButton(this);
        	 ib.setImageResource(R.drawable.ic_menu_back_small);
    		 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    		 // 
    		 ib.setOnClickListener(new View.OnClickListener() {
    				public void onClick(View view) {
    					// Up one directory.
    					browseTo(currentDirectory.getParentFile());
    				}
    		 });
    		 mDirectoryButtons.addView(ib, 0);
    		 
    		 // New button needs even more space
    		 ib.measure(spec, spec);
    		 requiredwidth += ib.getMeasuredWidth();

    		 // Need to take away some buttons
    		 // but leave at least "back" button and one directory button.
    		 while (requiredwidth > width && mDirectoryButtons.getChildCount() > 2) {
    			 View view = mDirectoryButtons.getChildAt(1);
    			 requiredwidth -= view.getMeasuredWidth();
    			 
	    		 mDirectoryButtons.removeViewAt(1);
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
     

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);

 		menu.add(0, MENU_NEW_FOLDER, 0, R.string.menu_new_folder).setIcon(
 				android.R.drawable.ic_menu_add).setShortcut('0', 'f');
 		
 		UpdateMenu
 				.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);
 		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(
 				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');

 		return true;
 	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Generate any additional actions that can be performed on the
		// overall list. This allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);

		// Workaround to add icons:
		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
				menu);
		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, FileManagerActivity.class), null, intent,
				0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Intent intent;
		switch (item.getItemId()) {
		case MENU_NEW_FOLDER:
			showDialog(DIALOG_NEW_FOLDER);
			return true;
			
		case MENU_UPDATE:
			UpdateMenu.showUpdateBox(this);
			return true;

		case MENU_ABOUT:
			showAboutBox();
			return true;
/*
		case MENU_PREFERENCES:
			intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
			return true;
			*/
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
/*
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
*/
		IconifiedText it = directoryEntries.get(info.position);
		menu.setHeaderTitle(it.getText());
		menu.setHeaderIcon(it.getIcon());
		// Setup the menu header

		menu.add(0, MENU_RENAME, 0, R.string.menu_rename);
		menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		// Remember current selection
		IconifiedText ic = directoryEntries.get(menuInfo.position);
		mContextText = ic.getText();
		mContextIcon = ic.getIcon();
		mContextFile = FileUtils.getFile(currentDirectory, ic.getText());
		
		switch (item.getItemId()) {

		case MENU_DELETE:
			showDialog(DIALOG_DELETE);
			return true;

		case MENU_RENAME:
			showDialog(DIALOG_RENAME);
			return true;
		}

		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_NEW_FOLDER:
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.dialog_new_folder, null);
			final EditText et = (EditText) view
					.findViewById(R.id.foldername);
			et.setText("");
			return new AlertDialog.Builder(this)
            	.setIcon(android.R.drawable.ic_dialog_alert)
            	.setTitle(R.string.create_new_folder).setView(view).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							createNewFolder(et.getText().toString());
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();
		

		case DIALOG_DELETE:
			return new AlertDialog.Builder(this).setTitle(getString(R.string.really_delete, mContextText))
            	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							deleteFileOrFolder(mContextFile);
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();

		case DIALOG_RENAME:
			inflater = LayoutInflater.from(this);
			view = inflater.inflate(R.layout.dialog_new_folder, null);
			final EditText et2 = (EditText) view
				.findViewById(R.id.foldername);
			return new AlertDialog.Builder(this)
            	.setTitle(R.string.menu_rename).setView(view).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							
							renameFileOrFolder(mContextFile, et2.getText().toString());
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();
			
		}
		return null;
		
	}


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		switch (id) {
		case DIALOG_NEW_FOLDER:
			EditText et = (EditText) dialog.findViewById(R.id.foldername);
			et.setText("");
			break;

		case DIALOG_DELETE:
			((AlertDialog) dialog).setTitle(getString(R.string.really_delete, mContextText));
			break;
			
		case DIALOG_RENAME:
			et = (EditText) dialog.findViewById(R.id.foldername);
			et.setText(mContextText);
			TextView tv = (TextView) dialog.findViewById(R.id.foldernametext);
			if (mContextFile.isDirectory()) {
				tv.setText(R.string.file_name);
			} else {
				tv.setText(R.string.file_name);
			}
			((AlertDialog) dialog).setIcon(mContextIcon);
			break;
		}
	}

	private void showAboutBox() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	private void createNewFolder(String foldername) {
		if (!TextUtils.isEmpty(foldername)) {
			File file = FileUtils.getFile(currentDirectory, foldername);
			if (file.mkdirs()) {
				
				// Change into new directory:
				browseTo(file);
			} else {
				Toast.makeText(this, R.string.error_creating_new_folder, Toast.LENGTH_SHORT).show();
			}
		}
	}


	private void deleteFileOrFolder(File file) {
		
		if (file.delete()) {
			// Delete was successful.
			refreshList();
			if (file.isDirectory()) {
				Toast.makeText(this, R.string.folder_deleted, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			}
		} else {
			if (file.isDirectory() && file.list().length > 0) {
				Toast.makeText(this, R.string.error_folder_not_empty, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.error_deleting_file, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void renameFileOrFolder(File file, String newFileName) {
		
		File newFile = FileUtils.getFile(currentDirectory, newFileName);
		
		if (file.renameTo(newFile)) {
			// Rename was successful.
			refreshList();
			if (file.isDirectory()) {
				Toast.makeText(this, R.string.folder_renamed, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.file_renamed, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, R.string.error_renaming_file, Toast.LENGTH_SHORT).show();
		}
	}
	
}