package org.openintents.intents;

/**
 * 
 * @author Peli
 * @version 1.1.0
 */
public class ShoppingListIntents {

	/**
	 * String extra containing the action to be performed.
	 * 
	 * <p>Constant Value: "org.openintents.extra.ACTION"</p>
	 */
	public static final String EXTRA_ACTION = "org.openintents.extra.ACTION";

	/**
	 * String extra containing the data on which to perform the action.
	 * 
	 * <p>Constant Value: "org.openintents.extra.DATA"</p>
	 */
	public static final String EXTRA_DATA = "org.openintents.extra.DATA";

	/**
	 * Task to be used in EXTRA_ACTION.
	 * 
	 * <p>Constant Value: "CLEAN_UP_LIST"</p>
	 */
	public static final String TASK_CLEAN_UP_LIST = "org.openintents.shopping.task.clean_up_list";
	
	/**
	 * Inserts shopping list items from a string array in intent extras.
	 * 
	 */
	public static final String TYPE_STRING_ARRAYLIST_SHOPPING = "org.openintents.type/string.arraylist.shopping";
	public static final String EXTRA_STRING_ARRAYLIST_SHOPPING = "org.openintents.extra.STRING_ARRAYLIST_SHOPPING";

	
}
