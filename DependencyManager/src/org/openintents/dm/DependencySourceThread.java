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

import org.openintents.dm.common.DependencyManagerContract;

import android.content.Context;
import android.content.ContentResolver;

import android.os.Handler;
import android.os.Looper;

import android.database.Cursor;
import android.database.ContentObserver;

import android.net.Uri;


import android.util.Log;

/**
 * Thread for fetching data from a dependency source. Each source can take a
 * while to respond. Fetching from multiple sources would imply that in the
 * worst case, results never make it to the user if the first source blocks
 * indefinitely.
 * By assigning a thread for fetching from each source, and merging the rsults
 * into an AggregateCursor, we'll be able to present updates as quickly as they
 * arrive.
 *
 * XXX Access to the AggregateCursor from these threads are synchronized on the
 *     cursor object itself. Access to the same cursor from other threads must
 *     also be synchronized.
 **/
class DependencySourceThread extends Thread
{
  /***************************************************************************
   * Private constants
   **/
  public static final String LTAG = "DependencySourceThread";



  /***************************************************************************
   * Private data
   **/
  // Context
  private Context           mContext;

  // DependencySource to query
  private DependencySource  mSource;
  // AggregateCursor to insert results into
  private AggregateCursor   mResultCursor;

  // Query-related arguments
  private Uri               mOriginalUri;
  private Uri               mUri;
  private String[]          mProjection;
  private String            mSelection;
  private String[]          mSelectionArgs;

  // Flag for notifying thread of changes
  private volatile boolean  mContentChanged = false;
  private Object            mContentChangedLock = new Object();



  /***************************************************************************
   * Observer for late content changes.
   **/
  class LateChangeObserver extends ContentObserver
  {
    public LateChangeObserver()
    {
      super(new Handler());
    }



    public void onChange(boolean selfChange)
    {
      synchronized (mContentChangedLock) {
        mContentChanged = true;
      }
      interrupt();
    }
  }



  /***************************************************************************
   * Implementation
   **/
  public DependencySourceThread(Context context, AggregateCursor resultCursor,
      DependencySource source)
  {
    super();
    mContext = context;
    mResultCursor = resultCursor;
    mSource = source;
  }



  public void setQuery(Uri uri, String[] projection, String selection,
      String[] selectionArgs)
  {
    mOriginalUri = uri;
    mProjection = projection;
    mSelection = selection;
    mSelectionArgs = selectionArgs;

    // The original URI is for DependencyResolutionManager to handle. Before we
    // can fetch information from this source, we need to swap the original
    // authority for this source's.
    mUri = Uri.parse(mOriginalUri.toString().replace(mOriginalUri.getAuthority(),
          mSource.getContentAuthority()));
  }



  public void run()
  {
    ContentResolver cr = mContext.getContentResolver();

    // Fetch data from the source.
    Cursor c = cr.query(mUri, mProjection, mSelection, mSelectionArgs,
        null);
    if (null == c) {
      return;
    }

    // Merge results into the result cursor
    mergeResults(c);

    // Register for notificiations on the modified URI, then wait for a while
    // for such notifications to occur. Each time we receive a change, we'll
    // restart the sleep, which can technically extend the period during which
    // we receive changes indefinitely.
    Looper.prepare(); // XXX don't really care about that, but it's required.
    LateChangeObserver observer = new LateChangeObserver();
    c.registerContentObserver(observer);
    boolean changed = false;
    do {
      try {
        sleep(DependencyManagerContract.SOURCE_REQUERY_TIMEOUT);
      } catch (InterruptedException ex) {
        // pass
      }

      synchronized (mContentChangedLock) {
        changed = mContentChanged;
        mContentChanged = false;
      }

      // Now that we've slept, check for late changes. We'll reach here either
      // by the LateChangeObserver calling interrupt(), or by the sleep timing
      // out.
      if (changed) {
        c.requery();
        mergeResults(c);
      }
    } while (changed);

    // Stop waiting for changes
    c.unregisterContentObserver(observer);
  }



  private void mergeResults(Cursor c)
  {
    if (null == c || 0 >= c.getCount()) {
      return;
    }

    synchronized (mResultCursor) {
      mResultCursor.merge(c);
    }

    // Notify observers of the result cursor of the changes.
    mContext.getContentResolver().notifyChange(mOriginalUri, null);
  }
}
