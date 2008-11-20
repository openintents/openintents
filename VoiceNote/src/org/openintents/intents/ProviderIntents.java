package org.openintents.intents;

// Version Nov 20, 2008


/**
 * Provides OpenIntents actions, extras, and categories used by providers. 
 * <p/> These specifiers extend the standard Android specifiers.
 */
public abstract class ProviderIntents {

	/**
	 * Broadcast Action: Sent after a new entry has been inserted.
	 * Constant Value: "org.openintents.action.INSERTED"
	 */
	public static final String ACTION_INSERTED = "org.openintents.action.INSERTED";
	
	/**
	 * Broadcast Action: Sent after an entry has been modified.
	 * Constant Value: "org.openintents.action.MODIFIED"
	 */
	public static final String ACTION_MODIFIED = "org.openintents.action.MODIFIED";
	
	/**
	 * Broadcast Action: Sent after an entry has been deleted.
	 * Constant Value: "org.openintents.action.DELETED"
	 */
	public static final String ACTION_DELETED = "org.openintents.action.DELETED";
	
}
