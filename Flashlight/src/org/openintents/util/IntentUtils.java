

/**
 * Original method retrieved from:
 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
 */
package org.openintents.util;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author romainguy
 * @author Peli
 * @author Karl Ostmo
 *
 */
public class IntentUtils {
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to the specified intent. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param intent The Intent to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(final Context context, final Intent intent) {
	    final PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	
	/**
	 * URI prefix to a package name to bring up the download page on the Android Market
	 */
    public static final String MARKET_PACKAGE_DETAILS_PREFIX = "market://details?id=";
    
    /**
     * This wrapper function first checks whether an intent is available. If it is not,
     * then the Android Market is launched (if available) to download the appropriate
     * package.  On the other hand, if the intent is available, and if a non-negative
     * request code is passed, the Intent is launched with startActivity().
     * Otherwise, the Intent is launched with startActivityForResult()
     * @param context
     * @param intent
     * @param request_code
     * @param package_name
     */
    public static void intentLaunchWithMarketFallback(Activity context, Intent intent, int request_code, String package_name) {
        if (isIntentAvailable(context, intent)) {
            if (request_code < 0)
                context.startActivity(intent);
            else
                context.startActivityForResult(intent, request_code);
        } else {
            // Launch market intent
            Uri market_uri = Uri.parse(MARKET_PACKAGE_DETAILS_PREFIX + package_name);
            Intent i = new Intent(Intent.ACTION_VIEW, market_uri);
            if (isIntentAvailable(context, i)) {
                context.startActivity(i);
            } else {
                Toast.makeText(context, "Android Market not available.", Toast.LENGTH_LONG).show();
            }
        }
    }
	
}
