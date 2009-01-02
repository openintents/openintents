/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.intents;

// Version Dec 28, 2008

public class CryptoIntents {

	/**
	 * Activity Action: Encrypt the string given in the extra TEXT.
	 * Returns the encrypted string in the extra TEXT.
	 * 
	 * Additional extras may be defined in a private namespace to define
	 * extra values that are returned unmodified.
	 * 
	 * <p>Constant Value: "org.openintents.action.ENCRYPT"</p>
	 */
	public static final String ACTION_ENCRYPT = "org.openintents.action.ENCRYPT";

	/**
	 * Activity Action: Decrypt the string given in the extra TEXT.
	 * Returns the decrypted string in the extra TEXT.
	 *
	 * Additional extras may be defined in a private namespace to define
	 * extra values that are returned unmodified.
	 * 
	 * <p>Constant Value: "org.openintents.action.DECRYPT"</p>
	 */
	public static final String ACTION_DECRYPT = "org.openintents.action.DECRYPT";

	/**
	 * The text to encrypt or decrypt.
	 * 
	 * <p>Constant Value: "org.openintents.extra.TEXT"</p>
	 */
	public static final String EXTRA_TEXT = "org.openintents.extra.TEXT";

}
