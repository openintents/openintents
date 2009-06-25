package org.openintents.intents;

/**
 * Intents for Locale bridge.
 * 
 * @author Peli
 * @version 1.0.0
 */
public class LocaleBridgeIntents {

	/**
	 * String extra for a broadcast intent to be performed.
	 * 
	 * IMPORTANT: Encode the Intent to a String using toURI()
	 * before using putExtra().
	 * Convert the String extra back to an Intent using Intent.getIntent().
	 * 
	 * <p>Constant Value: "org.openintents.extra.BROADCAST_INTENT"</p>
	 */
	public static final String EXTRA_LOCALE_BRIDGE_INTENT = "org.openintents.localebridge.extra.LOCALE_BRIDGE_INTENT";


	/**
	 * String extra for component name of the setting activity.
	 * 
	 * <p>Constant Value: "org.openintents.extra.BROADCAST_INTENT"</p>
	 */
	public static final String EXTRA_LOCALE_BRIDGE_COMPONENT = "org.openintents.localebridge.extra.LOCALE_BRIDGE_COMPONENT";
	
}
