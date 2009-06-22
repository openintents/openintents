package org.openintents.intents;

/**
 * Intents for automation.
 * 
 * @author Peli
 * @version 1.0.0
 */
public class AutomationIntents {

	/**
	 * Activity Action.
	 * 
	 * <p>Constant Value: "org.openintents.action.EDIT_AUTOMATION_SETTINGS"</p>
	 */
	public static final String ACTION_EDIT_AUTOMATION_SETTINGS = "org.openintents.action.EDIT_AUTOMATION_SETTINGS";

	/**
	 * String extra for an activity intent to be performed.
	 * 
	 * IMPORTANT: Encode the Intent to a String using toURI()
	 * before using putExtra().
	 * Convert the String extra back to an Intent using Intent.getIntent().
	 * 
	 * <p>Constant Value: "org.openintents.extra.ACTIVITY_INTENT"</p>
	 */
	public static final String EXTRA_ACTIVITY_INTENT = "org.openintents.extra.ACTIVITY_INTENT";
	
	/**
	 * String extra for a broadcast intent to be performed.
	 * 
	 * IMPORTANT: Encode the Intent to a String using toURI()
	 * before using putExtra().
	 * Convert the String extra back to an Intent using Intent.getIntent().
	 * 
	 * <p>Constant Value: "org.openintents.extra.BROADCAST_INTENT"</p>
	 */
	public static final String EXTRA_BROADCAST_INTENT = "org.openintents.extra.BROADCAST_INTENT";
	
	/**
	 * String extra containing a human readable description of the action to be performed.
	 * 
	 * <p>Constant Value: "org.openintents.extra.DESCRIPTION"</p>
	 */
	public static final String EXTRA_DESCRIPTION = "org.openintents.extra.DESCRIPTION";

	/**
	 * Category for various kinds of settings for automation.
	 * 
	 * <p>Constant Value: "org.openintents.category.AUTOMATION_SETTING"</p>
	 */
	public static final String CATEGORY_AUTOMATION_SETTING = "org.openintents.category.AUTOMATION_SETTING";
}
