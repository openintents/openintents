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
 */

package org.openintents.intents;

/**
 * Intents definition belonging to OI About.
 * 
 * @author pjv
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
	 * The only intent action for OI About: Show an about dialog to display
	 * information about your application. Send along extras with information to
	 * display. Only the PROGRAM_NAME extra is obligatory.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.action.SHOW_ABOUT_DIALOG"
	 * </p>
	 */
	public static final String ACTION_SHOW_ABOUT_DIALOG = 
		"org.openintents.action.SHOW_ABOUT_DIALOG";
	/**
	 * Intent extra key for: 
	 * A logo for the about box. There are 3 ways to
	 * supply an image via the intent: 
	 * 	1. Put the resource id (an integer) as a
	 * String in "logo". This won't help you much since it can only be a
	 * resource of OI About itself (does not work across packages).
	 * 
	 * 	2. Put the content uri of the image as a String in "logo". For instance:
	 * "content://images/1". As content provider you can use: a) your own small
	 * content provider just for the image, b) the System-wide MediaProvider
	 * (but your image will become public and might be duplicated each time
	 * showing the About dialog).
	 * 
	 * 	3. Put the name of the image resource as a String in "logo". This is the
	 * part you would append after "R.drawable." but with type and package as a
	 * prefix also. Actually it's good to use the result from
	 * "getResources().getResourceName(R.drawable.icon)". If you do this, you
	 * also need to put the package of your application (and thus the package
	 * containing the image resource) in "org.openintents.extra.LOGO_PACKAGE"
	 * (see EXTRA_LOGO_PACKAGE below) as a String.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.LOGO"
	 * </p>
	 */
	public static final String EXTRA_LOGO = "org.openintents.extra.LOGO";
	/**
	 * Intent extra key for: 
	 * The name of the package containing the image
	 * resource. Has no use but to support EXTRA_LOGO alternative 3, see above.
	 * You can put the result of
	 * "getResources().getResourcePackageName(R.drawable.icon)". Should not be
	 * added to the intent otherwise.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.LOGO_PACKAGE"
	 * </p>
	 */
	public static final String EXTRA_LOGO_PACKAGE = 
		"org.openintents.extra.LOGO_PACKAGE";
	/**
	 * Intent extra key for:
	 * The name of the program.
	 * 
	 * <p>Constant Value: "org.openintents.extra.PROGRAM_NAME"</p>
	 */
	public static final String EXTRA_PROGRAM_NAME = 
		"org.openintents.extra.PROGRAM_NAME";
	/**
	 * Intent extra key for:
	 * The version of the program.
	 * 
	 * <p>Constant Value: "org.openintents.extra.PROGRAM_VERSION"</p>
	 */
	public static final String EXTRA_PROGRAM_VERSION = 
		"org.openintents.extra.PROGRAM_VERSION";
	/**
	 * Intent extra key for: 
	 * Comments about the program. This string is
	 * displayed in a label in the main dialog, thus it should be a short
	 * explanation of the main purpose of the program, not a detailed list of
	 * features.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.COMMENTS"
	 * </p>
	 */
	public static final String EXTRA_COMMENTS = 
		"org.openintents.extra.COMMENTS";
	/**
	 * Intent extra key for:
	 * Copyright information for the program.
	 * 
	 * <p>Constant Value: "org.openintents.extra.COPYRIGHT"</p>
	 */
	public static final String EXTRA_COPYRIGHT = 
		"org.openintents.extra.COPYRIGHT";
	/**
	 * Intent extra key for: 
	 * The URL for the link to the website of the program.
	 * This should be a string starting with "http://".
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.WEBSITE_URL"
	 * </p>
	 */
	public static final String EXTRA_WEBSITE_URL = 
		"org.openintents.extra.WEBSITE_URL";
	/**
	 * Intent extra key for: 
	 * The label for the link to the website of the
	 * program. If this is not set, it defaults to the URL specified in the
	 * "org.openintents.extra.WEBSITE_URL" property.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.WEBSITE_LABEL"
	 * </p>
	 */
	public static final String EXTRA_WEBSITE_LABEL = 
		"org.openintents.extra.WEBSITE_LABEL";
	/**
	 * Intent extra key for: 
	 * The authors of the program, as an array of strings.
	 * Each string may contain email addresses and URLs, which will be displayed
	 * as links.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.AUTHORS"
	 * </p>
	 */
	public static final String EXTRA_AUTHORS = "org.openintents.extra.AUTHORS";
	/**
	 * Intent extra key for: 
	 * The people documenting the program, as an array of
	 * strings. Each string may contain email addresses and URLs, which will be
	 * displayed as links.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.DOCUMENTERS"
	 * </p>
	 */
	public static final String EXTRA_DOCUMENTERS = 
		"org.openintents.extra.DOCUMENTERS";
	/**
	 * Intent extra key for: 
	 * The people who made the translation for the current
	 * localization, as an array of strings. Each string may contain email
	 * addresses and URLs, which will be displayed as links. Only list those for
	 * the currently used/shown L10n.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.TRANSLATORS"
	 * </p>
	 */
	public static final String EXTRA_TRANSLATORS = 
		"org.openintents.extra.TRANSLATORS";
	/**
	 * Intent extra key for: 
	 * The people who contributed artwork to the program,
	 * as an array of strings. Each string may contain email addresses and URLs,
	 * which will be displayed as links.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.ARTISTS"
	 * </p>
	 */
	public static final String EXTRA_ARTISTS = "org.openintents.extra.ARTISTS";
	/**
	 * Intent extra key for: 
	 * The license of the program. This string is
	 * displayed in a text view in a secondary dialog, therefore it is fine to
	 * use a long multi-paragraph text. Still, not too long as it's sent through
	 * an intent and may cause delay. Note that the text is only wrapped in the
	 * text view if the "org.openintents.action.WRAP_LICENSE" property (see
	 * EXTRA_WRAP_LICENSE below) is set to TRUE; otherwise the text itself must
	 * contain the intended linebreaks.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.LICENSE"
	 * </p>
	 */
	public static final String EXTRA_LICENSE = "org.openintents.extra.LICENSE";
	/**
	 * Intent extra key for:
	 * Whether to wrap the text in the license dialog.
	 * 
	 * <p>Constant Value: "org.openintents.extra.WRAP_LICENSE"</p>
	 */
	public static final String EXTRA_WRAP_LICENSE = 
		"org.openintents.extra.WRAP_LICENSE";

}
