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

package org.openintents.dm.common;

import android.net.Uri;

/**
 * Contract between DependencyManager and dependency information data sources,
 * as well as clients of DependencyManager.
 **/
public class DependencyManagerContract
{
  // Part of the contract is a list of URIs that clients can try to open in
  // order to install DependencyManager.
  public static final Uri INSTALL_URIS[]            = {
    Uri.parse("market://search?q=pname:org.openintents.dm"), // Install via Android Market
  };

  // Authority for DependencyResolutionProvider
  public static final String CONTENT_AUTHORITY      = "org.openintents.dm";

  // Content URI paths. Requires a list of query-string encoded intents to list
  //    candidates for.
  public static final String PATH_LIST_CANDIDATES   = "list-candidates";

  // Key for Intents serialized into URI query parameters. Intents must be
  // encoded using Intent.toUri() with the URI_INTENT_SCHEME specified,
  // and values for this key therefore must be urlencoded.
  public static final String QUERY_PARAM_INTENT     = "intent";

  // Content Types
  public static final String CANDIDATE_LIST_TYPE    = "vnd.android.cursor.dir/vnd.dependency.candidate";
  public static final String CANDIDATE_TYPE         = "vnd.android.cursor.item/vnd.dependendcy.candidate";

  // Timeout (in msec) for dependency source requeries - DependencyManager will
  // listen to notifications on the source's content URI for this time, and
  // propagate changes.
  public static final int SOURCE_REQUERY_TIMEOUT    = 5 * 1000;


  /***************************************************************************
   * Fields returned as part of CANDIDATE_TYPE/CANDIDATE_LIST_TYPE.
   **/
  public static class CandidateColumns
  {
    // ** Results always include:
    // STORE_PACKAGE is TEXT NOT NULL, descriping the package name of the store
    //    app returning these results.
    public static final String STORE_PACKAGE        = "dm_store_package";
    // STORE_DISPLAY_NAME is TEXT NOT NULL, describing the display name of the
    //    store app returning these results
    public static final String STORE_DISPLAY_NAME   = "dm_store_display_name";
    // DISPLAY_NAME is TEXT, describing either the display name of the package
    //    that would satisfy a dependency, or the external search entry.
    public static final String DISPLAY_NAME         = "dm_display_name";
    // ICON_URI is TEXT NOT NULL, to be parsed as a URI. The URI's scheme
    //    determines how to access the icon image. The icon should represent the
    //    package described via APP_PACAKGE et al. if those fields are returned,
    //    or the store app if EXTERNAL_SEARCH_URI is non-NULL.
    public static final String ICON_URI             = "dm_icon_uri";
    // ** Results either include a non-NULL EXTERNAL_SEARCH_URI...
    // EXTERNAL_SEARCH_URI is TEXT, to be parsed as a URI. Opening the URI via
    //    an ACTION_VIEW Intent should launch the external search.
    //    XXX It's pretty clear that ACTION_VIEW and a URI is not ideal for
    //    specifying searches, but it happens to be what Android Market expects.
    //    Since the whole point of including an (optional) external search
    //    mechanism is to provide a bridge to legacy systems, implementing what
    //    Android Market expects is only sensible.
    public static final String EXTERNAL_SEARCH_URI  = "dm_external_search_uri";
    // ** ... or the following fields, but never both.
    // APP_PACKAGE is TEXT, describing the package name of the package that
    //    would satisfy a dependency.
    public static final String APP_PACKAGE          = "dm_app_package";
    // APP_VENDOR_NAME is TEXT, describing the vendor/publisher of the package.
    public static final String APP_VENDOR_NAME      = "dm_app_vendor_name";
    // APP_PRICE is INTEGER, describing the price of the package in their
    //    smallest monetary unit, i.e. cents for dollars and euros, etc.
    public static final String APP_PRICE            = "dm_app_price";
    // APP_CURRENCY is TEXT, specifying the ISO 4217 code for the currency the
    //    price above is given in (see http://en.wikipedia.org/wiki/ISO_4217).
    //    A price of zero or less indicates a free app.
    public static final String APP_CURRENCY         = "dm_app_currency";
    // APP_MATCHES is TEXT, specifying a list of query-string encoded intents
    //    that this package serves.
    public static final String APP_MATCHES          = "dm_app_matches";

    // Default projection
    public static final String[] CANDIDATE_PROJECTION = {
      STORE_PACKAGE,
      STORE_DISPLAY_NAME,
      DISPLAY_NAME,
      ICON_URI,
      EXTERNAL_SEARCH_URI,
      APP_PACKAGE,
      APP_VENDOR_NAME,
      APP_PRICE,
      APP_CURRENCY,
      APP_MATCHES,
    };
  }
}

