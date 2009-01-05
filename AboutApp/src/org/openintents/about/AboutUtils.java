package org.openintents.about;

import org.openintents.metadata.AboutMetaData;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class AboutUtils {
	private static final String TAG = "AboutUtils";

	public static String getTextFromArray(final String[] array) {
		String text = "";
		for (String person : array) {
			text += person + "\n";
		}
		text = text.substring(0, text.length() - 1); // delete last "\n"
		return text;
	}

	/**
	 * Get String array from Extra or from Meta-data through resources.
	 * 
	 * @param packagename
	 * @param intent
	 * @param extra
	 * @param metadata
	 */
	public static String getExtraOrMetadataArray(final Context context, final String packagename,
			final Intent intent, final String extra, final String metadata) {
		if (intent.hasExtra(extra)
				&& intent.getStringArrayExtra(extra) 
					!= null) {
			String text = getTextFromArray(intent
					.getStringArrayExtra(extra));
			Log.d(TAG, "Extra: " + extra + ", text: " + text);
			return text;
		} else {
	        //Try meta data of package
	        Bundle md = null;
	        try {
	                md = context.getPackageManager().getApplicationInfo(
						packagename, PackageManager.GET_META_DATA).metaData;
	        } catch (NameNotFoundException e) {
	            Log.e(TAG, "Package name not found", e);
	        }
	
	        if (md != null) {
	        	String[] array = null;
            	try {
            		int id = md.getInt(metadata);
            		Resources resources = context.getPackageManager()
						.getResourcesForApplication(packagename);
            		array = resources.getStringArray(id);
	        	} catch (NameNotFoundException e) {
            		Log.e(TAG, "Package name not found ", e);
	        	} catch (NumberFormatException e) {
            		Log.e(TAG, "Metadata not valid id.", e);
	        	} catch (Resources.NotFoundException e) {
            		Log.e(TAG, "Resource not found.", e);
	        	}
	        	
	        	if (array != null) {

        			String text = getTextFromArray(array);
        			Log.d(TAG, "Metadata: " + metadata + ", text: " + text);
        			return text;
            	} else {
            		return "";
            	}
	        } else {
	        	return "";
	        }
		}
	}

	/**
	 * Get string from extra or from metadata.
	 * 
	 * @param context
	 * @param packagename
	 * @param intent
	 * @param extra
	 * @param metadata
	 * @return
	 */
	public static String getExtraOrMetadataString(final Context context, final String packagename,
			final Intent intent, final String extra, final String metadata) {
		if (intent.hasExtra(extra)
				&& intent.getStringExtra(extra) != null) {
			return intent.getStringExtra(extra);
		} else {
	        //Try meta data of package
	        Bundle md = null;
	        try {
	                md = context.getPackageManager().getApplicationInfo(
						packagename, PackageManager.GET_META_DATA).metaData;
	        } catch (NameNotFoundException e) {
	            Log.e(TAG, "Package name not found", e);
	        }
	
	        if (md != null
					&& !TextUtils.isEmpty(md
							.getString(metadata))) {
	        	return md
				.getString(metadata);
	        } else {
	    		return "";
	        }
	
		}
	}

}
