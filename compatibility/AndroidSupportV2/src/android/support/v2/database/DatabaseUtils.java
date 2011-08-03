/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Since the source code of API level 11 and higher is
// not yet available, the following methods have been implemented
// from the JavaDoc description only.
package android.support.v2.database;

import android.text.TextUtils;

public class DatabaseUtils {
	/**
	 * Appends one set of selection args to another. This is useful when adding a selection argument to a user provided set.
	 * 
	 * @since API Level 11
	 * 
	 * @param originalValues
	 * @param newValues
	 * @return
	 */
	public static String[] appendSelectionArgs (String[] originalValues, String[] newValues) {
		if (newValues == null) {
			return originalValues;
		} else if (originalValues == null) {
			return newValues;
		} else {
			int oLen = originalValues.length;
			int nLen = newValues.length;
			String[] values = new String[oLen + nLen];
			for (int i = 0; i < oLen; i++) {
				values[i] = originalValues[i];
			}
			for (int i = 0; i < nLen; i++) {
				values[oLen + i] = newValues[i];
			}
			return values;
		}
	}

	
	/**
	 * Concatenates two SQL WHERE clauses, handling empty or null values.
	 * 
	 * @since API Level 11
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String concatenateWhere (String a, String b) {
		if (TextUtils.isEmpty(b)) {
			return a;
		} else if (TextUtils.isEmpty(a)) {
			return b;
		} else {
			StringBuilder sb = new StringBuilder(a.length() + b.length() + 9);
			sb.append("(");
			sb.append(a);
			sb.append(") AND (");
			sb.append(b);
			sb.append(")");
			return sb.toString();
		}
	}

	
	
	
}
