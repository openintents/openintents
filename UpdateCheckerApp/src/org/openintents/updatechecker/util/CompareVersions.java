package org.openintents.updatechecker.util;

import android.util.Log;

public class CompareVersions {

	private static int COMPARISON_GREATER = 1;
	private static int COMPARISON_EQUAL = 0;
	private static int COMPARISON_LESS = -1;
	private static int COMPARISON_NOT_POSSIBLE = 2;
	
	
	private static final String TAG = "CompareVersions";
	
	/**
	 * 
	 * @param currentVersionCode
	 * @param latestVersionCode
	 * @param currentVersionName
	 * @param latestVersionName
	 * @return
	 */
	public static boolean isUpToDate(int currentVersionCode, int latestVersionCode,
			String currentVersionName, String latestVersionName) {
		
		int result = compareVersions(currentVersionCode, latestVersionCode,
				currentVersionName, latestVersionName);
		
		if (result == COMPARISON_GREATER || result == COMPARISON_EQUAL) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param currentVersionCode
	 * @param latestVersionCode
	 * @param currentVersionName
	 * @param latestVersionName
	 * @return
	 */
	public static boolean isNewerVersionAvailable(int currentVersionCode, int latestVersionCode,
			String currentVersionName, String latestVersionName) {

		return compareVersions(currentVersionCode, latestVersionCode,
			currentVersionName, latestVersionName) == COMPARISON_LESS;
	}

	/**
	 * If version is ignored.
	 * @param currentVersionCode
	 * @param latestVersionCode
	 * @param currentVersionName
	 * @param latestVersionName
	 * @return
	 */
	public static boolean isIgnoredVersion(int currentVersionCode, int ignoreVersionCode,
			String currentVersionName, String ignoreVersionName) {

		int result = compareVersions(currentVersionCode, ignoreVersionCode,
				currentVersionName, ignoreVersionName);
		
		if (result == COMPARISON_LESS || result == COMPARISON_EQUAL) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Compare versions.
	 * 
	 * @param versionCodeA
	 * @param versionCodeB
	 * @param versionNameA
	 * @param versionNameB
	 * @return COMPARISON_GREATER, if version A is greater than version B.
	 *         COMPARISON_LESS if version A is less than version B.
	 *         COMPARISON_EQUAL if version A equals version B.
	 *         COMPARISON_NOT_POSSIBLE if comparison is not possible.
	 */
	private static int compareVersions(int versionCodeA, int versionCodeB,
			String versionNameA, String versionNameB) {
		
		//testCases();
		
		if (versionCodeA > 0 && versionCodeB > 0) {
			// Compare version codes
			return (versionCodeA > versionCodeB) ? COMPARISON_GREATER :
				(versionCodeA < versionCodeB) ? COMPARISON_LESS :
				COMPARISON_EQUAL;
		} else if (versionNameA != null
				&& versionNameB != null) {
			// Compare version names
			return compareVersionNames(versionNameA, versionNameB);
		} else {
			// No comparison possible
			return COMPARISON_NOT_POSSIBLE;
		}
	}
	
	
	/**
	 * Compare version names.
	 * 
	 * Names are of the form
	 * a.b.c
	 * a.b.c-d
	 * a.b.c_d
	 * 
	 * e.g. 1.0.11 > 1.0.2
	 *      1.0.2 > 1.0.1-rc1
	 *      1.0.11a > 1.0.2b
	 *      
	 * Only case not covered yet:
	 *      1.0.1-rc11 > 1.0.1-rc2
	 * @param versionNameA
	 * @param versionNameB
	 * @return +1 if A is larger than B, -1 the other way round, and 0 if they are equal.
	 */
	private static int compareVersionNames(String versionNameA, String versionNameB) {
		// Split up
		String[] partsA = versionNameA.split("[.]");
		String[] partsB = versionNameB.split("[.]");
		
		int len = Math.min(partsA.length, partsB.length);
		
		for (int i = 0; i < len; i++) {
			String[] a = splitNumericPart(partsA[i]);
			String[] b = splitNumericPart(partsB[i]);
			
			int na = parsePositiveInt(a[0]);
			int nb = parsePositiveInt(b[0]);
			
			if (na > nb) {
				// is definitely larger
				return +1;
			} else if (na < nb) {
				// is definitely smaller
				return -1;
			} else {
				// Is equal. Is there a difference in the part
				// behind the integer?
				if (a[1].length() == 0 && b[1].length() > 0) {
					// 1.0.0 > 1.0.0b (for beta)
					return +1;
				} else if (a[1].length() > 0 && b[1].length() == 0) {
					return -1;
				}
				int differInRest = a[1].compareTo(b[1]);
				if (differInRest != 0) {
					return differInRest;
				}
			}
			
		}
		
		// If so far all parts are equal, then compare further elements
		if (partsA.length > partsB.length) {
			return +1;
		} else if (partsA.length < partsB.length) {
			return -1;
		}
		
		// If no comparison brought a difference, they should be equal.
		return 0;
	}
	
	/**
	 * Returns the leading numeric part.
	 * @param s
	 * @return
	 */
	private static String[] splitNumericPart(String s) {
		int splitpos = 0;
		for (int i = 0; i < s.length(); i++) {
			if (("" + s.charAt(i)).matches("[0-9]")) {
				splitpos++;
			} else {
				break;
			}
		}
		return new String[] {s.substring(0, splitpos), s.substring(splitpos)};
	}
	
	/**
	 * Parses a positive integer.
	 * Returns -1 if it is no integer.
	 * @param s
	 * @return
	 */
	private static int parsePositiveInt(String s) {
		int n = -1;
		try {
			n = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// do nothing.
		};
		return n;
	}
	
	/////////////////////////////////////
	
	
	private static boolean tested = false;
	
	private static void testCases() {
		if (tested) return;
		tested = true;
		Log.d(TAG, "Test 1: " + ( 1 == compareVersionNames("1.1", "1.0")));
		Log.d(TAG, "Test 2: " + ( 0 == compareVersionNames("1.0", "1.0")));
		Log.d(TAG, "Test 3: " + (-1 == compareVersionNames("0.9", "1.0")));
		Log.d(TAG, "Test 4: " + ( 1 == compareVersionNames("1.0.1", "1.0")));
		Log.d(TAG, "Test 5: " + ( 0 == compareVersionNames("1.0.1", "1.0.1")));
		Log.d(TAG, "Test 6: " + ( 1 == compareVersionNames("1.11.1", "1.9.99")));
		Log.d(TAG, "Test 7: " + ( 1 == compareVersionNames("1.0.1", "1.0.1b")));
		Log.d(TAG, "Test 8: " + (-1 == compareVersionNames("2.0.5beta", "2.0.5")));
		Log.d(TAG, "Test 9: " + (-1 == compareVersionNames("1.0.1-rc1", "1.0.1-rc2")));
		Log.d(TAG, "Test 10: " + (0 == compareVersionNames("1.0.1-rc1", "1.0.1-rc1")));
		Log.d(TAG, "Test 11: " + ( 1 == compareVersionNames("1.0.1-rc11", "1.0.1-rc2"))); // not covered yet!
		
		Log.d(TAG, "Test i1: " + (1 == parsePositiveInt("1")));
		Log.d(TAG, "Test i2: " + (-1 == parsePositiveInt("")));
		Log.d(TAG, "Test i3: " + (-1 == parsePositiveInt(null)));
	}
}
