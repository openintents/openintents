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

package org.openintents.historify.data.providers.internal;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;
import org.openintents.historify.utils.UriUtils;

import android.net.Uri;

/**
 * 
 * Helper class for constants in {@link FactoryTestProvider}.
 * 
 * @author berke.andras
 */
public class FactoryTest {

	public static final String SOURCE_NAME = "FactoryTest";
	public static final String DESCRIPTION = "Infinite source of joy.";
	
	public static final String FACTORY_TEST_AUTHORITY = "org.openintents.historify.internal.factorytest";
	public static final Uri SOURCE_URI = UriUtils.sourceAuthorityToUri(FACTORY_TEST_AUTHORITY);
	public static final Uri EVENTS_URI = Uri.withAppendedPath(SOURCE_URI, Events.EVENTS_PATH);
	
}
