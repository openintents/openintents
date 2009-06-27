package com.twofortyfouram;

public class Intent {

	/**
	 * Intent action String broadcast by Locale to create or edit a plug-in setting. 
	 * When Locale broadcasts this Intent, it will contain the extra EXTRA_STRING_BREADCRUMB.
	 */
	public static final String ACTION_EDIT_SETTING = "com.twofortyfouram.locale.intent.action.EDIT_SETTING";
	
	/**
	 * Default action for firing a setting.
	 */
	public static final String ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING";
	
	/**
	 * Type: String
	 * <br/>
	 * Maps to a String that represents the action String when a setting is applied.
	 */
	public static final String EXTRA_STRING_ACTION_FIRE = "com.twofortyfouram.locale.intent.extra.ACTION_FIRE";
	
	/**
	 * Type: String
	 * <br/>
	 * Maps to a String that represents the action String when a setting is applied.
	 */
	public static final String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";
	
	/**
	 * Type: String
	 * 
	 * Maps to a String that represents the Activity breadcrumb path.
	 */
	public static final String EXTRA_STRING_BREADCRUMB = "com.twofortyfouram.locale.intent.extra.BREADCRUMB";

	/**
	 * Activity result code indicating that a setting or condition 
	 * should be deleted when the Locale situation editor resumes. 
	 * If this return code is set, then the setting or condition does 
	 * not need to provide any of the other required Intent extras.
	 */
	public static final int RESULT_REMOVE = 15;
}
