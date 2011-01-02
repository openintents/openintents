/**
 * This file is part of the Android DependencyManager project hosted at
 * http://code.google.com/p/android-dependencymanager/
 *
 * Copyright (C) 2009 Jens Finkhaeuser <jens@finkhaeuser.de>
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
 **/

package org.openintents.dm.teststore;

import android.net.Uri;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.Intent;

import android.database.Cursor;
import android.database.MatrixCursor;


import java.util.List;
import java.util.LinkedList;

import org.openintents.dm.common.DependencyManagerContract;
import org.openintents.dm.common.Intents;
import org.openintents.dm.common.DependencyManagerContract.CandidateColumns;

import android.util.Log;

/**
 *  This provider will create a cursor that returns three entries:
 *  1. The first one will match all intents specified in the uri.
 *  2. The second one will match only the first intent specified in the
 *     uri. If only one intent is specified, the first and second will
 *     match exactly the same.
 *  3. The third will return a non-NULL EXTERNAL_SEARCH_URI, that will
 *     perform an example search on Android Market.
 **/
public class TestStoreProvider extends ContentProvider
{
  /***************************************************************************
   * Private constants
   **/
  // Log Tag
  private static final String LTAG                  = "TestStoreProvider";

  // Base URI for this ContentProvider
  private static final String CONTENT_AUTHORITY     = "org.openintents.dm.teststore";

  // Content URI paths
  private static final String PATH_LIST_CANDIDATES  = DependencyManagerContract.PATH_LIST_CANDIDATES;

  // Content Types
  private static final String CANDIDATE_LIST_TYPE   = DependencyManagerContract.CANDIDATE_LIST_TYPE;
  private static final String CANDIDATE_TYPE        = DependencyManagerContract.CANDIDATE_TYPE;

  // CANDIDATE_TYPE-related fields
  private static final String STORE_PACKAGE         = CandidateColumns.STORE_PACKAGE;
  private static final String STORE_DISPLAY_NAME    = CandidateColumns.STORE_DISPLAY_NAME;
  private static final String DISPLAY_NAME          = CandidateColumns.DISPLAY_NAME;
  private static final String ICON_URI              = CandidateColumns.ICON_URI;
  private static final String EXTERNAL_SEARCH_URI   = CandidateColumns.EXTERNAL_SEARCH_URI;
  private static final String APP_PACKAGE           = CandidateColumns.APP_PACKAGE;
  private static final String APP_VENDOR_NAME       = CandidateColumns.APP_VENDOR_NAME;
  private static final String APP_PRICE             = CandidateColumns.APP_PRICE;
  private static final String APP_CURRENCY          = CandidateColumns.APP_CURRENCY;
  private static final String APP_MATCHES           = CandidateColumns.APP_MATCHES;

  // IDs for URI matches.
  private static final int ID_LIST_CANDIDATES       = 1;



  /***************************************************************************
   * Static members
   **/
  private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_LIST_CANDIDATES, ID_LIST_CANDIDATES);
  }



  /***************************************************************************
   * Implementation
   */

  @Override
  public boolean onCreate()
  {
    return true;
  }



  @Override
  public String getType(Uri uri)
  {
    int match = sUriMatcher.match(uri);

    switch (match) {
      case ID_LIST_CANDIDATES:
        return CANDIDATE_LIST_TYPE;

      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }



  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder)
  {
    // Ensure the query is valid
    int match = sUriMatcher.match(uri);
    if (UriMatcher.NO_MATCH == match) {
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    // Parse the intents the client asks for.
    List<Intent> intents = Intents.parseIntents(uri);
    if (null == intents) {
      throw new IllegalArgumentException("No Intents specified in URI " + uri);
    }

    Log.d(LTAG, "Query: " + uri);

    // Create cursor.
    MatrixCursor c = new MatrixCursor(CandidateColumns.CANDIDATE_PROJECTION, 3);

    // First entry: match all intents
    c.addRow(new Object[] {
          getContext().getPackageName(),    // STORE_PACKAGE
          "TestStore",                      // STORE_DISPLAY_NAME
          "Example App 1",                  // DISPLAY_NAME
          null, // FIXME
          null,                             // EXTERNAL_SEARCH_URI
          "com.example.app1",               // APP_PACKAGE
          "Vendor of App 1",                // APP_VENDOR_NAME
          new Integer(0),                   // APP_PRICE
          "EUR",                            // APP_CURRENCY
          Intents.serializeIntents(intents) // APP_MATCHES
        });

    // Second entry: match only the first intent
    LinkedList first = new LinkedList<Intent>();
    first.add(intents.get(0));
    c.addRow(new Object[] {
          getContext().getPackageName(),    // STORE_PACKAGE
          "TestStore",                      // STORE_DISPLAY_NAME
          "Example App 2",                  // DISPLAY_NAME
          null, // FIXME
          null,                             // EXTERNAL_SEARCH_URI
          "com.example.app2",               // APP_PACKAGE
          "Vendor of App 2",                // APP_VENDOR_NAME
          new Integer(299),                 // APP_PRICE
          "USD",                            // APP_CURRENCY
          Intents.serializeIntents(first)   // APP_MATCHES
        });

    // Third entry: return a search string searching the Android Market for
    // packages from OpenIntents.
    c.addRow(new Object[] {
          "com.android.vending",                // STORE_PACKAGE
          "Android Market",                     // STORE_DISPLAY_NAME
          "Search in Android Market...",        // DISPLAY_NAME
          null, // FIXME
          "market://search?q=pub:OpenIntents",  // EXTERNAL_SEARCH_URI
          null,                                 // APP_PACKAGE
          null,                                 // APP_VENDOR_NAME
          null,                                 // APP_PRICE
          null,                                 // APP_CURRENCY
          null,                                 // APP_MATCHES
        });

    return c;
  }



  @Override
  public Uri insert(Uri uri, ContentValues values)
  {
    throw new java.lang.SecurityException(LTAG + " does not support "
        + "write access.");
  }



  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs)
  {
    throw new java.lang.SecurityException(LTAG + " does not support "
        + "write access.");
  }



  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs)
  {
    throw new java.lang.SecurityException(LTAG + " does not support "
        + "write access.");
  }
}
