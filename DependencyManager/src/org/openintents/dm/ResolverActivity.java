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

import java.util.List;

import org.openintents.dm.common.DependencyManagerContract;
import org.openintents.dm.common.Intents;
import org.openintents.dm.common.DependencyManagerContract.CandidateColumns;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Displays a list of packages to install that would be able to serve one or
 * more given Intents
 **/
public class ResolverActivity extends ListActivity
{
  /***************************************************************************
   * Public constants
   **/
  public static final String EXTRA_INTENTS  = "org.openintents.dm.intents.extras.INTENTS";



  /***************************************************************************
   * Private constants
   **/
  private static final String LTAG          = "ResolverActivity";

  // Layut IDs. Indices into this array are the VIEW_TYPE_* values in
  // CandidateListAdapter
  private static final int LAYOUT_IDS[]     = {
    R.layout.resolver_list_item,
    R.layout.resolver_list_item_search,
  };


  /***************************************************************************
   * Private data
   **/
  // The list of Intents we need to resolve with this Activity.
  private List<Intent>  mIntents;

  // Cursor with resolution information.
  private Cursor        mCursor;



  /***************************************************************************
   * Implementation
   **/
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.resolver_activity);
    setTitle(R.string.candidate_list_title);

    // Grab extras
    Bundle extras = getIntent().getExtras();
    if (null == extras) {
      throw new IllegalArgumentException("Intent does not define any extras.");
    }
    mIntents = extras.getParcelableArrayList(EXTRA_INTENTS);
    if (null == mIntents) {
      throw new IllegalArgumentException("Intent does not define extra '" + EXTRA_INTENTS + "'.");
    }
    if (0 >= mIntents.size()) {
      throw new IllegalArgumentException("Intent extra '" + EXTRA_INTENTS + "' is empty.");
    }

    // Now we can query DependencyResolutionProvider for resolution infromation.
    Uri uri = Uri.parse(String.format("content://%s/%s?%s",
            DependencyManagerContract.CONTENT_AUTHORITY,
            DependencyManagerContract.PATH_LIST_CANDIDATES,
            Intents.serializeIntents(mIntents)));

    mCursor = managedQuery(uri, CandidateColumns.CANDIDATE_PROJECTION,
        null, null, null);

    // Create & use list adapter
    CandidateListAdapter adapter = new CandidateListAdapter(this, LAYOUT_IDS,
        mCursor);
    setListAdapter(adapter);
    getListView().setEmptyView(findViewById(R.id.empty_list));
    getListView().setOnScrollListener(
        new CandidateListAdapter.ScrollListener(adapter));
  }
}
