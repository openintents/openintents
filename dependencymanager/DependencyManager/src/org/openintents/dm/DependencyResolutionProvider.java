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

package org.openintents.dm;

import android.net.Uri;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.Intent;

import android.database.Cursor;

import java.util.List;
import java.util.LinkedList;

import org.openintents.dm.common.DependencyManagerContract;
import org.openintents.dm.common.Intents;
import org.openintents.dm.common.DependencyManagerContract.CandidateColumns;


import android.util.Log;

/**
 * The DependencyResolutionProvider aggregates results from data sources for
 * dependency, and returns them in a sorted list.
 **/
public class DependencyResolutionProvider extends ContentProvider
{
  /***************************************************************************
   * Private constants
   **/
  // Log Tag
  private static final String LTAG                  = "DependencyResolutionProvider";

  // Base URI for this ContentProvider
  private static final String CONTENT_AUTHORITY     = DependencyManagerContract.CONTENT_AUTHORITY;

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

  // Timout (in milliseconds) for the validity of the mSources field below.
  private static final long SOURCES_VALID_TIMEOUT   = 30 * 1000;


  /***************************************************************************
   * Static members
   **/
  private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_LIST_CANDIDATES, ID_LIST_CANDIDATES);
  }



  /***************************************************************************
   * Private data
   **/
  // Lock for mSources & related fields
  private Object                  mSourcesLock = new Object();
  // Sources from which this provider draws information.
  private List<DependencySource>  mSources;
  // Timestamp at which mSources was last updated.
  private long                    mSourcesTimestamp = -1;



  /***************************************************************************
   * Comparator classes for AggregateCursor use.
   **/

  /**
   * Compares two rows by the number of Intents matched in the APP_MATCHES
   * field. No further comparison of *which* Intents were matched is made.
   **/
  private static class MatchCountComparator extends AggregateCursor.RowComparator
  {
    public int compareCurrentRows(Cursor c1, Cursor c2)
    {
      String match_str1 = c1.getString(c1.getColumnIndex(APP_MATCHES));
      String match_str2 = c2.getString(c2.getColumnIndex(APP_MATCHES));

      // Shortcuts to full comparison.
      if (null == match_str1 && null == match_str2) {
        return 0;
      }
      else if (null == match_str1) {
        return 1;
      }
      else if (null == match_str2) {
        return -1;
      }

      if (match_str1.equals(match_str2)) {
        return 0;
      }

      // Shortcuts are exhausted, now we'll need to deserialize the Intents and
      // compare the match size.
      List<Intent> matches1 = Intents.parseIntents(Uri.parse(
            String.format("dummy:///?%s", match_str1)));
      List<Intent> matches2 = Intents.parseIntents(Uri.parse(
            String.format("dummy:///?%s", match_str2)));

      if (matches1.size() > matches2.size()) {
        return -1;
      }
      else if (matches1.size() < matches2.size()) {
        return 1;
      }
      return 0;
    }
  }



  /***************************************************************************
   * Implementation
   */

  @Override
  public boolean onCreate()
  {
    synchronized (mSourcesLock)
    {
      updateSources();
    }

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

    // Grab the sources to query for this request.
    LinkedList<DependencySource> sources = null;
    synchronized (mSourcesLock)
    {
      updateSources();
      sources = new LinkedList<DependencySource>(mSources);
    }
    if (0 >= sources.size()) {
      throw new IllegalStateException("Can't respond to query; no data sources known.");
    }

    // Create AggregateCursor into which sources can insert their data.
    // TODO add other comparators, select based on sortOrder argument
    AggregateCursor cursor = new AggregateCursor(projection,
        new MatchCountComparator(), true);

    // The notification uri for the cursor is the original uri.
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    // For each source, create a thread in which to fetch data from.
    for (DependencySource source : sources) {
      DependencySourceThread th = new DependencySourceThread(getContext(), cursor, source);
      th.setQuery(uri, projection, selection, selectionArgs);
      th.start();
    }

    return cursor;
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



  /**
   * If necessary, update mSources. Expects mSourcesLock to be held by the
   * caller.
   **/
  private void updateSources()
  {
    long now = System.currentTimeMillis();
    long diff = now - mSourcesTimestamp;
    if (-1 == mSourcesTimestamp || null == mSources
        || SOURCES_VALID_TIMEOUT <= diff)
    {
      mSources = DependencySource.scanForSources(getContext());
      mSourcesTimestamp = now;
    }
  }
}
