/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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

package org.openintents.intents;

/**
 * Intents definition belonging to OI About.
 * 
 * @version 2009-Jan-08
 * 
 * @author pjv
 * @author Peli
 *
 */
public final class AboutIntents {
	
	/**
	 * Empty, preventing instantiation.
	 */
	private AboutIntents() {
		//Empty, preventing instantiation.
	}

	/**
	 * Activity Action: Show an about dialog to display
	 * information about the application.
	 * 
	 * The application information is retrieved from the
	 * application's manifest. In order to send the package
	 * you have to launch this activity through
	 * startActivityForResult().
	 * 
	 * Alternatively, you can specify the package name 
	 * manually through the extra EXTRA_PACKAGE.
	 * 
	 * All data can be replaced using optional intent extras.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.action.SHOW_ABOUT_DIALOG"
	 * </p>
	 */
	public static final String ACTION_SHOW_ABOUT_DIALOG = 
		"org.openintents.action.SHOW_ABOUT_DIALOG";

	/**
	 * Optional intent extra: Specify your application package name.
	 * 
	 * If you start the About dialog through startActivityForResult()
	 * then the application package is sent automatically and does
	 * not need to be supplied here.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.PACKAGE_NAME"
	 * </p>
	 */
	public static final String EXTRA_PACKAGE_NAME = 
		"org.openintents.extra.PACKAGE_NAME";
	
	/**
	 * Optional intent extra: A logo for the about box from an image URI.
	 * 
	 * By default, this is retrieved from the Manifest tag
	 * "application android:icon".
	 * 
	 * Alternatively you can set the EXTRA_ICON_RESOURCE.
	 * 
	 * ICON_URI specifies the content uri of the image as a String. For instance:
	 * "content://images/1". As content provider you can use: a) your own small
	 * content provider just for the image, b) the System-wide MediaProvider
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.ICON_URI"
	 * </p>
	 */
	public static final String EXTRA_ICON_URI = "org.openintents.extra.ICON_URI";
	
	/**
	 * Optional intent extra: A logo for the about box from a resource.
	 * 
	 * By default, this is retrieved from the Manifest tag
	 * "application android:icon".
	 * 
	 * Alternatively you can set the EXTRA_ICON_URI.
	 * 
	 * Specify the name of the image resource as a String.
	 * Use the result from "getResources().getResourceName(R.drawable.icon)".
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.ICON_RESOURCE"
	 * </p>
	 */
	public static final String EXTRA_ICON_RESOURCE = "org.openintents.extra.ICON_RESOURCE";
	
	/**
	 * Optional intent extra: The name of the program.
	 * 
	 * By default, this is retrieved from the Manifest tag
	 * "application android:label".
	 * 
	 * <p>Constant Value: "org.openintents.extra.APPLICATION_LABEL"</p>
	 */
	public static final String EXTRA_APPLICATION_LABEL = 
		"org.openintents.extra.APPLICATION_LABEL";
	
	/**
	 * Optional intent extra: The version of the program.
	 * 
	 * By default, this is retrieved from the Manifest tag
	 * "manifest android:versionName".
	 * 
	 * <p>Constant Value: "org.openintents.extra.VERSION_NAME"</p>
	 */
	public static final String EXTRA_VERSION_NAME = 
		"org.openintents.extra.VERSION_NAME";
	
	/**
	 * Optional intent extra: Comments about the program.
	 * 
	 * This string is displayed in a label in the main dialog, 
	 * thus it should be a short explanation of the main purpose 
	 * of the program, not a detailed list of features.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_COMMENTS name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.COMMENTS"
	 * </p>
	 */
	public static final String EXTRA_COMMENTS = 
		"org.openintents.extra.COMMENTS";
	
	/**
	 * Optional intent extra: Copyright information for the program.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_COPYRIGHT name.
	 * 
	 * <p>Constant Value: "org.openintents.extra.COPYRIGHT"</p>
	 */
	public static final String EXTRA_COPYRIGHT = 
		"org.openintents.extra.COPYRIGHT";
	
	/**
	 * Optional intent extra: The URL for the link to the website of the program.
	 * 
	 * This should be a string starting with "http://".
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_WEBSITE_URL name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.WEBSITE_URL"
	 * </p>
	 */
	public static final String EXTRA_WEBSITE_URL = 
		"org.openintents.extra.WEBSITE_URL";
	
	/**
	 * Optional intent extra: The label for the link to the website of the
	 * program. 
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_WEBSITE_LABEL name.
	 * 
	 * If this is not set, it defaults to the URL specified in the
	 * "org.openintents.extra.WEBSITE_URL" property.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.WEBSITE_LABEL"
	 * </p>
	 */
	public static final String EXTRA_WEBSITE_LABEL = 
		"org.openintents.extra.WEBSITE_LABEL";
	
	/**
	 * Optional intent extra:
	 * The authors of the program, as an array of strings.
	 * 
	 * Each string may contain email addresses and URLs, which will be displayed
	 * as links.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_AUTHORS name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.AUTHORS"
	 * </p>
	 */
	public static final String EXTRA_AUTHORS = "org.openintents.extra.AUTHORS";

	/**
	 * Optional intent extra:
	 * The people documenting the program, as an array of
	 * strings.
	 * 
	 * Each string may contain email addresses and URLs, which will be
	 * displayed as links.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_DOCUMENTERS name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.DOCUMENTERS"
	 * </p>
	 */
	public static final String EXTRA_DOCUMENTERS = 
		"org.openintents.extra.DOCUMENTERS";
	
	/**
	 * Optional intent extra:
	 * The people who made the translation for the current
	 * localization, as an array of strings.
	 * 
	 * Each string may contain email
	 * addresses and URLs, which will be displayed as links. Only list those for
	 * the currently used/shown L10n.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_TRANSLATORS name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.TRANSLATORS"
	 * </p>
	 */
	public static final String EXTRA_TRANSLATORS = 
		"org.openintents.extra.TRANSLATORS";
	
	/**
	 * Optional intent extra:
	 * The people who contributed artwork to the program,
	 * as an array of strings.
	 * 
	 * Each string may contain email addresses and URLs,
	 * which will be displayed as links.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_ARTISTS name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.ARTISTS"
	 * </p>
	 */
	public static final String EXTRA_ARTISTS = "org.openintents.extra.ARTISTS";
	
	/**
	 * Optional intent extra:
	 * The name of the raw resource containing the license of the program.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_LICENSE name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.LICENSE_RESOURCE"
	 * </p>
	 */
	public static final String EXTRA_LICENSE_RESOURCE = "org.openintents.extra.LICENSE_RESOURCE";

	/**
	 * Optional intent extra:
	 * The primary email address for this application.
	 * 
	 * By default, this is retrieved from the Manifest meta tag
	 * with the AboutMetaData.METADATA_EMAIL name.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.EMAIL"
	 * </p>
	 */
	public static final String EXTRA_EMAIL = "org.openintents.extra.EMAIL";
	

}
