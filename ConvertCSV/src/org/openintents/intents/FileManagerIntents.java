package org.openintents.intents;

// Version Dec 7, 2008


/**
 * Provides OpenIntents actions, extras, and categories used by providers. 
 * <p>These specifiers extend the standard Android specifiers.</p>
 */
public final class FileManagerIntents {

	/**
	 * Activity Action: Pick a file through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current file name or file name suggestion.
	 * Returns a new file name in data.
	 * 
	 * <p>Constant Value: "org.openintents.action.PICK_FILE"</p>
	 */
	public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";
	
	/**
	 * The title to display.
	 * 
	 * <p>This is shown in the title bar of the file manager.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.TITLE"</p>
	 */
	public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

	/**
	 * Whether the file manager should allow the user to enter a new file name.
	 * 
	 * <p>Boolean value: true: Allow user to enter a file name. 
	 * false: Allow user only to pick from existing files.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.ALLOW_NEW_FILENAME"</p>
	 */
	public static final String EXTRA_ALLOW_NEW_FILENAME = "org.openintents.extra.ALLOW_NEW_FILENAME";

}
