/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of OI About.
 *
 *   OI About is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OI About is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OI About.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   
 *   
 *   The idea, window layout and elements, and some of the comments below are based on GtkAboutDialog. See http://library.gnome.org/devel/gtk/stable/GtkAboutDialog.html and http://www.gtk.org.
 */

package org.openintents.about;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.TabHost;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.tomgibara.android.veecheck.Veecheck;
import com.tomgibara.android.veecheck.util.PrefSettings;

public class About extends TabActivity {
	//TODO packaging
	//TODO change version file url
	//TODO BUG rotating screen broken due to TabHost?? Cannot test without sensor.
	//TODO Link doesn't work!!!!!
	//TODO Intent extras namespace aren't correct yet. Suggestions made in the openintents.org intents db. Need to agree on this with OI and need to know how they do it (constants and such) in their other apps.

	private static final String TAG = "About";
	
	/**
	 * The intent action to use. This and some of the intent extras with the keys below is everything you need.
	 */
	public static final String INTENT_ACTION="org.openintents.action.SHOW_ABOUT_DIALOG";
	
    /**
     * Intent extra key for:
     * A logo for the about box.
     * There are 3 ways to supply an image via the intent:
     * 		1. Put the resource id (an integer) as a String in "logo". This won't help you much since it can only be a resource of OI About itself (does not work across packages).
     * 		2. Put the content uri of the image as a String in "logo". For instance: "content://images/1". As content provider you can use: a) your own small content provider just for the image, b) the System-wide MediaProvider (but your image will become public and might be duplicated each time showing the About dialog).
     * 		3. Put the name of the image resource as a String in "logo". This is the part you would append after "R.drawable." but with type and package as a prefix also. Actually it's good to use the result from "getResources().getResourceName(R.drawable.icon)". If you do this, you also need to put the package of your application (and thus the package containing the image resource) in "logo package" (see KEY_LOGO_PACKAGE below) as a String.
     */
    public static final String KEY_LOGO = "logo";
    /**
     * Intent extra key for:
     * The name of the package containing the image resource. Has no use but to support KEY_LOGO alternative 3, see above. You can put the result of "getResources().getResourcePackageName(R.drawable.icon)". Should not be added to the intent otherwise.
     */
    public static final String KEY_LOGO_PACKAGE = "logo package";
    /**
     * Intent extra key for:
     * The name of the program.
     */
    public static final String KEY_PROGRAM_NAME = "program name";
    /**
     * Intent extra key for:
     * The version of the program.
     */
    public static final String KEY_PROGRAM_VERSION = "program version";
    
    /**
     * Intent extra key for:
     * Comments about the program. This string is displayed in a label in the main dialog, thus it should be a short explanation of the main purpose of the program, not a detailed list of features.
     */
    public static final String KEY_COMMENTS_TEXT = "comments text";
    /**
     * Intent extra key for:
     * Copyright information for the program.
     */
    public static final String KEY_COPYRIGHT_TEXT = "copyright text";
    /**
     * Intent extra key for:
     * The URL for the link to the website of the program. This should be a string starting with "http://".
     */
    public static final String KEY_WEBSITE_URL = "website url";
    /**
     * Intent extra key for:
     * The label for the link to the website of the program. If this is not set, it defaults to the URL specified in the website url property.
     */
    public static final String KEY_WEBSITE_LABEL = "website label";
    /**
     * Intent extra key for:
     * The authors of the program, as an array of strings. Each string may contain email addresses and URLs, which will be displayed as links.
     */
    public static final String KEY_AUTHORS_TEXT_ARRAY = "authors text array";
    /**
     * Intent extra key for:
     * The people documenting the program, as an array of strings. Each string may contain email addresses and URLs, which will be displayed as links.
     */
    public static final String KEY_DOCUMENTERS_TEXT_ARRAY = "documenters text array";
    /**
     * Intent extra key for:
     * The people who made the translation for the current localization, as an array of strings. Each string may contain email addresses and URLs, which will be displayed as links.
     */
    public static final String KEY_TRANSLATORS_TEXT_ARRAY_FOR_THIS_L10N = "translators text array for this L10n";

    /**
     * Intent extra key for:
     * The people who contributed artwork to the program, as an array of strings. Each string may contain email addresses and URLs, which will be displayed as links.
     */
    public static final String KEY_ARTISTS_TEXT_ARRAY = "artists text array";
    /**
     * Intent extra key for:
     * The license of the program. This string is displayed in a text view in a secondary dialog, therefore it is fine to use a long multi-paragraph text. Still, not too long as it's sent through an intent and may cause delay. Note that the text is only wrapped in the text view if the "wrap license" property (see KEY_WRAP_LICENSE below) is set to TRUE; otherwise the text itself must contain the intended linebreaks.
     */
    public static final String KEY_LICENSE_TEXT = "license text";
    /**
     * Intent extra key for:
     * Whether to wrap the text in the license dialog.
     */
    public static final String KEY_WRAP_LICENSE = "wrap license";
    
	/**
	 * The views.
	 */
	protected ImageSwitcher mLogoImage;
	protected TextSwitcher mProgramNameAndVersionText;
	protected TextSwitcher mCommentsText;
	protected TextSwitcher mCopyrightText;
	protected TextSwitcher mWebsiteText;
	protected EditText mAuthorsText;
	protected EditText mDocumentersText;
	protected EditText mTranslatorsText;
	protected EditText mArtistsText;
	protected EditText mLicenseText;

	protected TabHost tabHost;

	/**
	 * Menu item id's
	 */
	public static final int MENU_ITEM_PREFS = Menu.FIRST;
	public static final int MENU_ITEM_ABOUT = Menu.FIRST+1;
	
	/**
	 * Preference default values constants.
	 */
	public static final long DEFAULT_CHECK_INTERVAL = 24 * 60 * 60 * 1000L;
	public static final long DEFAULT_PERIOD = 1 * 60 * 60 * 1000L;

    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	//Set up the layout with the TabHost
    	tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_info))
                .setIndicator(getString(R.string.l_info))
                .setContent(R.id.sv_info));
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_credits))
                .setIndicator(getString(R.string.l_credits))
                .setContent(R.id.sv_credits));
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_license))
                .setIndicator(getString(R.string.l_license))
                .setContent(R.id.sv_license));
        
        //Set the animations for the switchers
        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        
        //Find the views
        mLogoImage = (ImageSwitcher) findViewById(R.id.i_logo);
        mLogoImage.setInAnimation(in);
        mLogoImage.setOutAnimation(out);
		
		mProgramNameAndVersionText = (TextSwitcher) findViewById(R.id.t_program_name_and_version);
		mProgramNameAndVersionText.setInAnimation(in);
		mProgramNameAndVersionText.setOutAnimation(out);
        
		mCommentsText = (TextSwitcher) findViewById(R.id.t_comments);
		mCommentsText.setInAnimation(in);
		mCommentsText.setOutAnimation(out);
		
		mCopyrightText = (TextSwitcher) findViewById(R.id.t_copyright);
		mCopyrightText.setInAnimation(in);
		mCopyrightText.setOutAnimation(out);
		
		mWebsiteText = (TextSwitcher) findViewById(R.id.t_website);
		mWebsiteText.setInAnimation(in);
		mWebsiteText.setOutAnimation(out);
		
		mAuthorsText = (EditText) findViewById(R.id.et_authors);
		
		mDocumentersText = (EditText) findViewById(R.id.et_documenters);
		
		mTranslatorsText = (EditText) findViewById(R.id.et_translators);
		
		mArtistsText = (EditText) findViewById(R.id.et_artists);
		
		mLicenseText = (EditText) findViewById(R.id.et_license);
		
		//Set up the version checking preferences
		SharedPreferences prefs = PrefSettings.getSharedPrefs(this);
		//Assign some default settings if necessary
		if (prefs.getString(PrefSettings.KEY_CHECK_URI, null) == null) {
			Editor editor = prefs.edit();
			editor.putBoolean(PrefSettings.KEY_ENABLED, true);
			editor.putLong(PrefSettings.KEY_PERIOD, DEFAULT_PERIOD);
			editor.putLong(PrefSettings.KEY_CHECK_INTERVAL, DEFAULT_CHECK_INTERVAL);
			editor.putString(PrefSettings.KEY_CHECK_URI, getString(R.string.version_file_url));
			editor.commit();
		}

		//Reschedule the version checks - we need to do this if the settings have changed (as above)
		//it may also necessary in the case where an application has been updated
		//here for simplicity, we do it every time the application is launched
		Intent intent = new Intent(Veecheck.getRescheduleAction(this));
		sendBroadcast(intent);
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
        tabHost.setCurrentTabByTag(getString(R.string.l_info));
		
		//Decode the intent, if any
		final Intent intent = getIntent();
        if(intent==null){
        	refuseToShow();
        	return;
        }
    	setResult(RESULT_OK);
    	
    	if(intent.hasExtra(KEY_LOGO) && intent.getStringExtra(KEY_LOGO)!=null
    			&& intent.hasExtra(KEY_LOGO_PACKAGE) && intent.getStringExtra(KEY_LOGO_PACKAGE)!=null){
    		try{
    			changeLogoImage(intent.getStringExtra(KEY_LOGO), intent.getStringExtra(KEY_LOGO_PACKAGE));
    		}catch(IllegalArgumentException e){
    			mLogoImage.setImageURI(Uri.EMPTY);
    		}
    	}else if(intent.hasExtra(KEY_LOGO) && intent.getStringExtra(KEY_LOGO)!=null){
    		try{
    			changeLogoImage(intent.getStringExtra(KEY_LOGO));
    		}catch(IllegalArgumentException e){
    			mLogoImage.setImageURI(Uri.EMPTY);
    		}
    	}else{
    		mLogoImage.setImageURI(Uri.EMPTY);
    	}
        if(intent.hasExtra(KEY_PROGRAM_NAME) && intent.getStringExtra(KEY_PROGRAM_NAME)!=null){
        	String programText=intent.getStringExtra(KEY_PROGRAM_NAME);
        	setTitle(getString(R.string.about_activity_title)+" "+intent.getStringExtra(KEY_PROGRAM_NAME));
        	if(intent.hasExtra(KEY_PROGRAM_VERSION) && intent.getStringExtra(KEY_PROGRAM_VERSION)!=null){
        		programText+=" "+intent.getStringExtra(KEY_PROGRAM_VERSION);
        	}
    		mProgramNameAndVersionText.setText(programText);
        }else{
        	refuseToShow();
        	return;
        }
    	if(intent.hasExtra(KEY_COMMENTS_TEXT) && intent.getStringExtra(KEY_COMMENTS_TEXT)!=null){
    		mCommentsText.setText(intent.getStringExtra(KEY_COMMENTS_TEXT));
    	}else{
    		mCommentsText.setText("");
    	}
    	if(intent.hasExtra(KEY_COPYRIGHT_TEXT) && intent.getStringExtra(KEY_COPYRIGHT_TEXT)!=null){
    		mCopyrightText.setText(intent.getStringExtra(KEY_COPYRIGHT_TEXT));
    	}else{
    		mCopyrightText.setText("");
    	}
    	if(intent.hasExtra(KEY_WEBSITE_URL) && intent.getStringExtra(KEY_WEBSITE_URL)!=null
    			&& intent.hasExtra(KEY_WEBSITE_LABEL) && intent.getStringExtra(KEY_WEBSITE_LABEL)!=null){
    		mWebsiteText.setText(intent.getStringExtra(KEY_WEBSITE_LABEL));
    		
    		//Allow a label and url through Linkify
    		Linkify.addLinks((TextView)mWebsiteText.getChildAt(0), Pattern.compile(".*"), intent.getStringExtra(KEY_WEBSITE_URL));
    		Linkify.addLinks((TextView)mWebsiteText.getChildAt(1), Pattern.compile(".*"), intent.getStringExtra(KEY_WEBSITE_URL));
    	}else if(intent.hasExtra(KEY_WEBSITE_URL) && intent.getStringExtra(KEY_WEBSITE_URL)!=null){
    		mWebsiteText.setText(intent.getStringExtra(KEY_WEBSITE_URL));
    	}else{
    		mWebsiteText.setText("");
    	}
    	if(intent.hasExtra(KEY_AUTHORS_TEXT_ARRAY) && intent.getStringArrayExtra(KEY_AUTHORS_TEXT_ARRAY)!=null){
    		String text="";
    		for(String person: intent.getStringArrayExtra(KEY_AUTHORS_TEXT_ARRAY)){
    			text+=person+"\n";
    		}
    		mAuthorsText.setText(text);
    	}else{
    		mAuthorsText.setText("");
    	}
    	if(intent.hasExtra(KEY_DOCUMENTERS_TEXT_ARRAY) && intent.getStringArrayExtra(KEY_DOCUMENTERS_TEXT_ARRAY)!=null){
    		String text="";
    		for(String person: intent.getStringArrayExtra(KEY_DOCUMENTERS_TEXT_ARRAY)){
    			text+=person+"\n";
    		}
    		mDocumentersText.setText(text);
    	}else{
    		mDocumentersText.setText("");
    	}
    	if(intent.hasExtra(KEY_TRANSLATORS_TEXT_ARRAY_FOR_THIS_L10N) && intent.getStringArrayExtra(KEY_TRANSLATORS_TEXT_ARRAY_FOR_THIS_L10N)!=null){
    		String text="";
    		for(String person: intent.getStringArrayExtra(KEY_TRANSLATORS_TEXT_ARRAY_FOR_THIS_L10N)){
    			text+=person+"\n";
    		}
    		mTranslatorsText.setText(text);
    	}else{
    		mTranslatorsText.setText("");
    	}
    	if(intent.hasExtra(KEY_ARTISTS_TEXT_ARRAY) && intent.getStringArrayExtra(KEY_ARTISTS_TEXT_ARRAY)!=null){
    		String text="";
    		for(String person: intent.getStringArrayExtra(KEY_ARTISTS_TEXT_ARRAY)){
    			text+=person+"\n";
    		}
    		mArtistsText.setText(text);
    	}else{
    		mArtistsText.setText("");
    	}
    	mLicenseText.setHorizontallyScrolling(!intent.getBooleanExtra(KEY_WRAP_LICENSE, false));
    	if(intent.hasExtra(KEY_LICENSE_TEXT) && intent.getStringExtra(KEY_LICENSE_TEXT)!=null){
    		mLicenseText.setText(intent.getStringExtra(KEY_LICENSE_TEXT));
    	}else{
    		mLicenseText.setText("");
    	}
	}

	/**
	 * There wasn't enough data, so notify we canceled and finish the Activity.
	 */
	protected void refuseToShow() {
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Change the logo image using the resource in the string argument.
	 * @param logoString String of a content uri to an image resource (actually, can also be a string of a resId, but that won't help much across packages).
	 */
	protected void changeLogoImage(String logoString) {
		try {
			int imageDescriptionResId = Integer
					.parseInt(logoString);
			mLogoImage.setImageResource(imageDescriptionResId);
		} catch (NumberFormatException nfe) {// Not a resource id but a uri
												// perhaps?
			Uri imageDescriptionUri = Uri.parse(logoString);
			if (imageDescriptionUri != null) {
				mLogoImage.setImageURI(imageDescriptionUri);
			} else {// Not even a uri, so invalid.
				throw new IllegalArgumentException("Not a valid image.");
			}
		}
	}
	
	/**
	 * Change the logo image using the resource name and package.
	 * @param resourceFileName String of the name of the image resource (as you would append it after "R.drawable.").
	 * @param resourcePackageName String of the name of the source package of the image resource (the package name of the calling app).
	 */
	protected void changeLogoImage(String resourceFileName, String resourcePackageName) {
		try {
			Resources resources = getPackageManager().getResourcesForApplication(resourcePackageName);
			final int id = resources.getIdentifier(resourceFileName, null, null);
			mLogoImage.setImageDrawable(resources.getDrawable(id));
		} catch (NumberFormatException e) {// Not a resource id
			throw new IllegalArgumentException("Not a valid image.");
		} catch (NotFoundException e) {// Resource not found
			throw new IllegalArgumentException("Not a valid image.");
		} catch (NameNotFoundException e) {//Not a package name
			throw new IllegalArgumentException("Not a valid (image resource) package name.");
		}
		/*The idea for this came from:
			android.content.Intent.ShortcutIconResource and related contstants and intents, in android.content.Intent: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/content/Intent.java;h=39888c1bc0f62effa788815e5b9376969d255766;hb=master
			what's done with this in com.android.launcher.Launcher: http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git;a=blob;f=src/com/android/launcher/Launcher.java;h=928f4caecde593d0fb430718de28d5e52df989ad;hb=HEAD
				and in android.webkit.gears.DesktopAndroid: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/webkit/gears/DesktopAndroid.java
		*/
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//Show preferences action
		menu.add(ContextMenu.NONE, MENU_ITEM_PREFS, ContextMenu.NONE, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);

		//About action
		menu.add(ContextMenu.NONE, MENU_ITEM_ABOUT, ContextMenu.NONE, R.string.menu_about).setIcon(R.drawable.about);

		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, About.class), null,
				intent, 0, null);

		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_ABOUT: {
				// Show the about dialog for this app.
				showAboutDialog();
				return true;
			}
			case MENU_ITEM_PREFS: {
				// Show the preferences.
				startActivity(new Intent().setClass(this, Preferences.class));
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showAboutDialog() {
		Intent intent=new Intent(About.INTENT_ACTION);
		
		//Supply the image.
		/*//alternative 2b: Put the image resId into the provider.
		Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.icon);//lossy
		String uri = Images.Media.insertImage(getContentResolver(), image,
				getString(R.string.about_logo_title), getString(R.string.about_logo_description));
		intent.putExtra(About.KEY_LOGO, uri);*/
		
		//alternative 3: Supply the image name and package.
		intent.putExtra(About.KEY_LOGO, getResources().getResourceName(R.drawable.icon));
		intent.putExtra(About.KEY_LOGO_PACKAGE, getResources().getResourcePackageName(R.drawable.icon));
		
		intent.putExtra(About.KEY_PROGRAM_NAME, getString(R.string.app_name));
		
		//Get the app version
		String version = "?";
		try {
		        PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
		        version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
		        Log.e(TAG, "Package name not found", e);
		};
		intent.putExtra(About.KEY_PROGRAM_VERSION, version);
		intent.putExtra(About.KEY_COMMENTS_TEXT, getString(R.string.about_comments));
		intent.putExtra(About.KEY_COPYRIGHT_TEXT, getString(R.string.about_copyright));
		intent.putExtra(About.KEY_WEBSITE_LABEL, getString(R.string.about_website_label));
		intent.putExtra(About.KEY_WEBSITE_URL, getString(R.string.about_website_url));
		intent.putExtra(About.KEY_AUTHORS_TEXT_ARRAY, getResources().getStringArray(R.array.about_authors));
		intent.putExtra(About.KEY_DOCUMENTERS_TEXT_ARRAY, getResources().getStringArray(R.array.about_documenters));
		intent.putExtra(About.KEY_TRANSLATORS_TEXT_ARRAY_FOR_THIS_L10N, getResources().getStringArray(R.array.about_translators));
		intent.putExtra(About.KEY_ARTISTS_TEXT_ARRAY, getResources().getStringArray(R.array.about_artists));
		
		//Read in the license file as a big String
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.license_short)));
		String license="";
		String line;
		try {
			while((line=in.readLine())!=null){//Read line per line.
				 license+=line+"\n";
			}
		} catch (IOException e) {
			//Should not happen.
			e.printStackTrace();
		}
		intent.putExtra(About.KEY_LICENSE_TEXT, license);
		intent.putExtra(About.KEY_WRAP_LICENSE, false);
		
		startActivity(Intent.createChooser(intent, getString(R.string.about_chooser_title)));
	}
}