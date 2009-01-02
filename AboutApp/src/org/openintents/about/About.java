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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openintents.intents.AboutIntents;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
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

/**
 * Main About dialog activity.
 * 
 * @author pjv
 *
 */
public class About extends TabActivity {
	//TODO packaging
	//TODO BUG rotating screen broken due to TabHost?
	//TODO BUG OI Updater does not find OI About.
	
	private static final String TAG = "About";
	
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
	 * Menu item id's.
	 */
	public static final int MENU_ITEM_ABOUT = Menu.FIRST;

    /**
	 * Change the logo image using the resource in the string argument.
	 * 
	 * @param logoString
	 *            String of a content uri to an image resource (actually, can
	 *            also be a string of a resId, but that won't help much across
	 *            packages).
	 */
	protected void changeLogoImage(final String logoString) {
		try {
			int imageDescriptionResId = Integer
					.parseInt(logoString);
			mLogoImage.setImageResource(imageDescriptionResId);
		} catch (NumberFormatException nfe) { // Not a resource id but a uri
												// perhaps?
			Uri imageDescriptionUri = Uri.parse(logoString);
			if (imageDescriptionUri != null) {
				mLogoImage.setImageURI(imageDescriptionUri);
			} else { // Not even a uri, so invalid.
				throw new IllegalArgumentException("Not a valid image.");
			}
		}
	}

	/**
	 * Change the logo image using the resource name and package.
	 * 
	 * @param resourceFileName
	 *            String of the name of the image resource (as you would append
	 *            it after "R.drawable.").
	 * @param resourcePackageName
	 *            String of the name of the source package of the image resource
	 *            (the package name of the calling app).
	 */
	protected void changeLogoImage(final String resourceFileName,
			final String resourcePackageName) {
		try {
			Resources resources = getPackageManager()
					.getResourcesForApplication(resourcePackageName);
			final int id = resources
					.getIdentifier(resourceFileName, null, null);
			mLogoImage.setImageDrawable(resources.getDrawable(id));
		} catch (NumberFormatException e) { // Not a resource id
			throw new IllegalArgumentException("Not a valid image.");
		} catch (NotFoundException e) { // Resource not found
			throw new IllegalArgumentException("Not a valid image.");
		} catch (NameNotFoundException e) { //Not a package name
			throw new IllegalArgumentException(
					"Not a valid (image resource) package name.");
		}
		/*The idea for this came from:
			android.content.Intent.ShortcutIconResource and related contstants and intents, in android.content.Intent: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/content/Intent.java;h=39888c1bc0f62effa788815e5b9376969d255766;hb=master
			what's done with this in com.android.launcher.Launcher: http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git;a=blob;f=src/com/android/launcher/Launcher.java;h=928f4caecde593d0fb430718de28d5e52df989ad;hb=HEAD
				and in android.webkit.gears.DesktopAndroid: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/webkit/gears/DesktopAndroid.java
		*/
	}

	/**
	 * Fetch and display artists information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayArtists(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_ARTISTS)
				&& intent.getStringArrayExtra(AboutIntents.EXTRA_ARTISTS) 
					!= null) {
			String text = "";
			for (String person : intent
					.getStringArrayExtra(AboutIntents.EXTRA_ARTISTS)) {
				text += person + "\n";
    		}
    		mArtistsText.setText(text);
    	} else {
    		mArtistsText.setText("");
    	}
	}

	/**
	 * Fetch and display authors information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayAuthors(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_AUTHORS)
				&& intent.getStringArrayExtra(AboutIntents.EXTRA_AUTHORS) 
					!= null) {
			String text = "";
			for (String person : intent
					.getStringArrayExtra(AboutIntents.EXTRA_AUTHORS)) {
				text += person + "\n";
    		}
    		mAuthorsText.setText(text);
    	} else {
    		mAuthorsText.setText("");
    	}
	}

	/**
	 * Fetch and display comments information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayComments(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_COMMENTS)
				&& intent.getStringExtra(AboutIntents.EXTRA_COMMENTS) != null) {
			mCommentsText.setText(intent
					.getStringExtra(AboutIntents.EXTRA_COMMENTS));
    	} else {
    		mCommentsText.setText("");
    	}
	}

	/**
	 * Fetch and display copyright information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayCopyright(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_COPYRIGHT)
				&& intent.getStringExtra(AboutIntents.EXTRA_COPYRIGHT) 
					!= null) {
			mCopyrightText.setText(intent
					.getStringExtra(AboutIntents.EXTRA_COPYRIGHT));
    	} else {
    		mCopyrightText.setText("");
    	}
	}

	/**
	 * Fetch and display documenters information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayDocumenters(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_DOCUMENTERS)
				&& intent.getStringArrayExtra(AboutIntents.EXTRA_DOCUMENTERS) 
					!= null) {
    		String text = "";
    		for (String person : intent
					.getStringArrayExtra(AboutIntents.EXTRA_DOCUMENTERS)) {
    			text += person + "\n";
    		}
    		mDocumentersText.setText(text);
    	} else {
    		mDocumentersText.setText("");
    	}
	}

	/**
	 * Fetch and display license information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayLicense(final Intent intent) {
		mLicenseText.setHorizontallyScrolling(!intent.getBooleanExtra(
				AboutIntents.EXTRA_WRAP_LICENSE, false));
		mLicenseText.setHorizontalScrollBarEnabled(!intent.getBooleanExtra(
				AboutIntents.EXTRA_WRAP_LICENSE, false));
		if (intent.hasExtra(AboutIntents.EXTRA_LICENSE)
				&& intent.getStringExtra(AboutIntents.EXTRA_LICENSE) != null) {
			mLicenseText.setText(intent
					.getStringExtra(AboutIntents.EXTRA_LICENSE));
		} else {
    		mLicenseText.setText("");
    	}
	}

	/**
	 * Fetch and display logo information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayLogo(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_LOGO)
				&& intent.getStringExtra(AboutIntents.EXTRA_LOGO) != null
				&& intent.hasExtra(AboutIntents.EXTRA_LOGO_PACKAGE)
				&& intent.getStringExtra(AboutIntents.EXTRA_LOGO_PACKAGE) 
					!= null) {
    		try {
    			changeLogoImage(intent.getStringExtra(AboutIntents.EXTRA_LOGO),
						intent.getStringExtra(AboutIntents.EXTRA_LOGO_PACKAGE));
    		} catch (IllegalArgumentException e) {
    			mLogoImage.setImageURI(Uri.EMPTY);
    		}
    	} else if (intent.hasExtra(AboutIntents.EXTRA_LOGO)
				&& intent.getStringExtra(AboutIntents.EXTRA_LOGO) != null) {
    		try {
    			changeLogoImage(intent.getStringExtra(AboutIntents.EXTRA_LOGO));
    		} catch (IllegalArgumentException e) {
    			mLogoImage.setImageURI(Uri.EMPTY);
    		}
    	} else {
    		try {
                PackageInfo pi = getPackageManager().getPackageInfo(
						getCallingPackage(), 0);
                changeLogoImage("" + pi.applicationInfo.icon);
    		} catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package name not found", e);
        		mLogoImage.setImageURI(Uri.EMPTY);
    		} catch (IllegalArgumentException e) {
    			mLogoImage.setImageURI(Uri.EMPTY);
    		}
    	}
	}

	/**
	 * Fetch and display program name and version information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayProgramNameAndVersion(final Intent intent) {
		String programText;
		if (intent.hasExtra(AboutIntents.EXTRA_PROGRAM_NAME)
				&& intent.getStringExtra(AboutIntents.EXTRA_PROGRAM_NAME) 
					!= null) {
			programText = intent
					.getStringExtra(AboutIntents.EXTRA_PROGRAM_NAME);
			setTitle(getString(R.string.about_activity_title) + " "
					+ intent.getStringExtra(AboutIntents.EXTRA_PROGRAM_NAME));
        } else {
            try {
                    PackageInfo pi = getPackageManager().getPackageInfo(
						getCallingPackage(), 0);
                    programText = getString(pi.applicationInfo.labelRes);
                    if (TextUtils.isEmpty(programText)) {
                    	refuseToShow();
                    	return;
                    }
            } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Package name not found", e);
                	refuseToShow();
                	return;
            }
        }
        programText = displayProgramVersion(intent, programText);
        mProgramNameAndVersionText.setText(programText);
	}

	/**
	 * Fetch and display program version information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected String displayProgramVersion(final Intent intent, String programText) {
		if (intent.hasExtra(AboutIntents.EXTRA_PROGRAM_VERSION)
				&& intent.getStringExtra(AboutIntents.EXTRA_PROGRAM_VERSION) 
					!= null) {
			programText += " "
				+ intent.getStringExtra(AboutIntents.EXTRA_PROGRAM_VERSION);
		} else {
			String version = "";
            try {
                    PackageInfo pi = getPackageManager().getPackageInfo(
                    		getCallingPackage(), 0);
                    version = pi.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Package name not found", e);
            }
            if (!TextUtils.isEmpty(version)) {
                programText += " " + version;
            }
		}
		return programText;
	}

	/**
	 * Fetch and display translators information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayTranslators(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_TRANSLATORS)
				&& intent.getStringArrayExtra(AboutIntents.EXTRA_TRANSLATORS) 
					!= null) {
			String text = "";
			for (String person : intent
					.getStringArrayExtra(AboutIntents.EXTRA_TRANSLATORS)) {
				text += person + "\n";
    		}
    		mTranslatorsText.setText(text);
    	} else {
    		mTranslatorsText.setText("");
    	}
	}

	/**
	 * Fetch and display website link information.
	 * 
	 * @param intent The intent from which to tru to fetch the information.
	 */
	protected void displayWebsiteLink(final Intent intent) {
		if (intent.hasExtra(AboutIntents.EXTRA_WEBSITE_URL)
				&& intent.getStringExtra(AboutIntents.EXTRA_WEBSITE_URL) != null
				&& intent.hasExtra(AboutIntents.EXTRA_WEBSITE_LABEL)
				&& intent.getStringExtra(AboutIntents.EXTRA_WEBSITE_LABEL) 
					!= null) {
			mWebsiteText.setText(intent
					.getStringExtra(AboutIntents.EXTRA_WEBSITE_LABEL));
    		
    		//Create TransformFilter
    		TransformFilter tf = new TransformFilter() {

				public String transformUrl(final Matcher matcher,
						final String url) {
					return intent
							.getStringExtra(AboutIntents.EXTRA_WEBSITE_URL);
				}
    			
    		};
    		
    		//Allow a label and url through Linkify
			Linkify.addLinks((TextView) mWebsiteText.getChildAt(0), Pattern
					.compile(".*"), "", null, tf);
			Linkify.addLinks((TextView) mWebsiteText.getChildAt(1), Pattern
					.compile(".*"), "", null, tf);
		} else if (intent.hasExtra(AboutIntents.EXTRA_WEBSITE_URL)
				&& intent.getStringExtra(AboutIntents.EXTRA_WEBSITE_URL) 
					!= null) {
			mWebsiteText.setText(intent
					.getStringExtra(AboutIntents.EXTRA_WEBSITE_URL));
    	} else {
    		mWebsiteText.setText("");
    	}
	}

	/* (non-Javadoc)
     * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	//Set up the layout with the TabHost
    	tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.main,
				tabHost.getTabContentView(), true);
        
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
		
        mProgramNameAndVersionText = (TextSwitcher) 
        		findViewById(R.id.t_program_name_and_version);
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
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//About action
		menu.add(ContextMenu.NONE, MENU_ITEM_ABOUT, ContextMenu.NONE,
				R.string.menu_about).setIcon(R.drawable.about);

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
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_ABOUT:
				// Show the about dialog for this app.
				showAboutDialog();
				return true;
			
			default:
				// Whoops, unknown menu item.
				Log.e(TAG, "Unknown menu item");
		}
		return super.onOptionsItemSelected(item);
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
        if (intent == null) {
        	refuseToShow();
        	return;
        }
    	setResult(RESULT_OK);
    	
    	displayLogo(intent);
        displayProgramNameAndVersion(intent);
    	displayComments(intent);
    	displayCopyright(intent);
    	displayWebsiteLink(intent);
    	displayAuthors(intent);
    	displayDocumenters(intent);
    	displayTranslators(intent);
    	displayArtists(intent);
    	displayLicense(intent);
	}
	
	/**
	 * There wasn't enough data, so notify we canceled and finish the Activity.
	 */
	protected void refuseToShow() {
		Log.e(TAG, "No program name specified.");
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Show an about dialog for this application.
	 */
	protected void showAboutDialog() {
		Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);
		
		//Supply the image.
		/*//alternative 2b: Put the image resId into the provider.
		Bitmap image = BitmapFactory.decodeResource(getResources(), 
				R.drawable.icon);//lossy
		String uri = Images.Media.insertImage(getContentResolver(), image,
				getString(R.string.about_logo_title), 
				getString(R.string.about_logo_description));
		intent.putExtra(AboutIntents.EXTRA_LOGO, uri);*/
		
		//alternative 3: Supply the image name and package.
		intent.putExtra(AboutIntents.EXTRA_LOGO, getResources()
				.getResourceName(R.drawable.icon));
		intent.putExtra(AboutIntents.EXTRA_LOGO_PACKAGE, getResources()
				.getResourcePackageName(R.drawable.icon));

		intent.putExtra(AboutIntents.EXTRA_PROGRAM_NAME,
				getString(R.string.app_name));
		
		//Get the app version
		String version = "?";
		try {
		        PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
		        version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
		        Log.e(TAG, "Package name not found", e);
		}
		intent.putExtra(AboutIntents.EXTRA_PROGRAM_VERSION, version);
		
		intent.putExtra(AboutIntents.EXTRA_COMMENTS,
				getString(R.string.about_comments));
		intent.putExtra(AboutIntents.EXTRA_COPYRIGHT,
				getString(R.string.about_copyright));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_LABEL,
				getString(R.string.about_website_label));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_URL,
				getString(R.string.about_website_url));
		intent.putExtra(AboutIntents.EXTRA_AUTHORS, getResources()
				.getStringArray(R.array.about_authors));
		intent.putExtra(AboutIntents.EXTRA_DOCUMENTERS, getResources()
				.getStringArray(R.array.about_documenters));
		intent.putExtra(AboutIntents.EXTRA_TRANSLATORS, getResources()
				.getStringArray(R.array.about_translators));
		intent.putExtra(AboutIntents.EXTRA_ARTISTS, getResources()
				.getStringArray(R.array.about_artists));
		
		//Read in the license file as a big String
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(
				getResources().openRawResource(R.raw.license_short)));
		String license = "";
		String line;
		try {
			while ((line = in.readLine()) != null) { // Read line per line.
				license += line + "\n";
			}
		} catch (IOException e) {
			//Should not happen.
			e.printStackTrace();
		}
		intent.putExtra(AboutIntents.EXTRA_LICENSE, license);
		intent.putExtra(AboutIntents.EXTRA_WRAP_LICENSE, false);
		
		// Start about activity. Needs to be "forResult" with requestCode>=0
		// because the About dialog may call elements from your Manifest by your
		// package name.
		startActivityForResult(Intent.createChooser(intent,
				getString(R.string.about_chooser_title)), 0);
	}
}
