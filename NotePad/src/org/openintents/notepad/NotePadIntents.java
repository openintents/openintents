package org.openintents.notepad;

public class NotePadIntents {

	/* 
	 * Content Provider ID.
	 * Private extra that is passed along with an ENCRYPT or DECRYPT intent.
	 */
	public static final String EXTRA_ID = "org.openintents.notepad.extra.id";

	/* 
	 * Content URI passed as String.
	 * Private extra that is passed along with an ENCRYPT or DECRYPT intent.
	 */
	public static final String EXTRA_URI = "org.openintents.notepad.extra.uri";
	
	/* 
	 * Original encrypted text.
	 * Private extra that is passed along with an ENCRYPT or DECRYPT intent.
	 */
	public static final String EXTRA_ENCRYPTED_TEXT = "org.openintents.notepad.extra.encrypted_text";

	/* 
	 * The action to perform (either encrypt or decrypt).
	 * Private extra that is passed along with an ENCRYPT or DECRYPT intent.
	 */
	public static final String EXTRA_ACTION = "org.openintents.notepad.extra.action";

}
