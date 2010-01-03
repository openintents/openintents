package org.openintents.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * Helper functions for retrieving remote themes,
 * that are themes in external packages.
 * 
 * @author Peli
 *
 */
public class ThemeUtils {
	public static final String[] OpenIntentsThemeAttributes = new String[] {
			"upperCaseFont", // 0 - index see below
			"labelTextSize", // 1
			"textSizeSmall", // 2
			"textSizeMedium", // 3
			"textSizeLarge", // 4
			"textColor", // 5
			"priceTextColor", // 6
			"markTextColor", // 7
			"markType", // 8
			"background", // 9
			"backgroundPadding", // 10
			"divider", // 11
			"clickMeansEdit", // 12
			"typeface" // 13
	};
	
	public static int ID_upperCaseFont = 0;
	public static int ID_labelTextSize = 1;
	public static int ID_textSizeSmall = 2;
	public static int ID_textSizeMedium = 3;
	public static int ID_textSizeLarge = 4;
	public static int ID_textColor = 5;
	public static int ID_priceTextColor = 6;
	public static int ID_markTextColor = 7;
	public static int ID_markType = 8;
	public static int ID_background = 9;
	public static int ID_backgroundPadding = 10;
	public static int ID_divider = 11;
	public static int ID_clickMeansEdit = 12;
	public static int ID_typeface = 13;
	
	
	public static int[] getAttributeIds(Context context, String[] attrNames, String packageName) {
		int len = attrNames.length;
		Resources res = context.getResources();
		
		int[] attrIds = new int[len];
		for (int i = 0; i < len; i++) {
			attrIds[i] = res.getIdentifier(attrNames[i], "attr", packageName);
			Log.d("ee", " retrieve: " + attrIds[i]);
		}
		return attrIds;
	}
}
