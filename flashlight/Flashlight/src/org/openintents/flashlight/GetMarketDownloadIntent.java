package org.openintents.flashlight;

import org.openintents.util.IntentUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * @author Peli
 * @author Karl Ostmo
 */
public class GetMarketDownloadIntent {

	/**
	 * Color Picker package name for download from the Android Market
	 */
	public final static String PACKAGE_NAME_COLOR_PICKER = "org.openintents.colorpicker";

	//public final static String APK_DOWNLOAD_URL_PREFIX = "http://code.google.com/p/openintents/downloads/detail?name=";
	//public final static String APK_DOWNLOAD_FILENAME_COLOR_PICKER = "ColorPicker-1.0.0.apk";
	public final static String COLOR_PICKER_WEBSITE = "http://www.openintents.org/en/colorpicker";
	// FIXME
//	public final static String APK_DOWNLOAD_FILENAME_COLOR_PICKER = "ShoppingList-1.2.3.apk";
	//public final static Uri APK_DOWNLOAD_URI_COLOR_PICKER = Uri.parse(APK_DOWNLOAD_URL_PREFIX + APK_DOWNLOAD_FILENAME_COLOR_PICKER);
	public final static Uri APK_DOWNLOAD_URI_COLOR_PICKER = Uri.parse(COLOR_PICKER_WEBSITE);

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
        if (IntentUtils.isIntentAvailable(context, intent)) {
            if (request_code < 0)
                context.startActivity(intent);
            else
                context.startActivityForResult(intent, request_code);
        } else {
            // Launch market intent
        	Intent i = getMarketDownloadIntent(package_name);
            if (IntentUtils.isIntentAvailable(context, i)) {
                context.startActivity(i);
            } else {
                Toast.makeText(context, "Android Market not available.", Toast.LENGTH_LONG).show();
            }
        }
    }
	
    public static Intent getMarketDownloadIntent(String package_name) {
        Uri market_uri = Uri.parse(MARKET_PACKAGE_DETAILS_PREFIX + package_name);
        return new Intent(Intent.ACTION_VIEW, market_uri);
    }
}
