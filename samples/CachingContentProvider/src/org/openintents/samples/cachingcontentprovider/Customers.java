package org.openintents.samples.cachingcontentprovider;
/*
<!-- 
 * Copyright (C) 2010 OpenIntents UG
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
 --> 
 */
import android.net.Uri;
import android.provider.BaseColumns;

public class Customers implements BaseColumns {
    // This class cannot be instantiated
    private Customers() {}

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.ccp/customers");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of customers.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openintents.examples.ccp.customers";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single customer.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openintents.examples.ccp.customer";

    /**
     * The name of the customer
     * <P>Type: TEXT</P>
     */
    public static final String NAME = "name";

    /**
     * The email address of a customer
     * <P>Type: TEXT</P>
     */
    public static final String EMAIL = "note";

    public static final String[] DEFAULT_PROJECTION_MAP={_ID,NAME,EMAIL};
    
    public static final String 	DEFAULT_SORT_ORDER=_ID+" asc";
}