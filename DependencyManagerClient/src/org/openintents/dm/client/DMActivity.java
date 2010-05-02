/**
 * This file is part of the Android DependencyManager project hosted at
 * http://code.google.com/p/android-dependencymanager/
 *
 * Copyright (C) 2009,2010 Jens Finkhaeuser <jens@finkhaeuser.de>
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

package org.openintents.dm.client;

import android.app.Activity;

import android.os.Bundle;

import android.util.Log;

/**
 * Helper class - simplifies setting up a connection to DependencyManager, etc.
 **/
public class DMActivity extends Activity implements DependencyManager.BindListener
{
  /***************************************************************************
   * Private constants
   **/
  private static final String LTAG = "DMActivity";



  /***************************************************************************
   * Private data
   **/
  // Result of the bind operation - can be queried from users of the class
  private boolean mBindResult = false;

  // Dependency manager instance
  private DependencyManager mDependencyManager;



  /***************************************************************************
   * Implementation
   **/
  @Override
  public void onStart()
  {
    super.onStart();
    resolveDependencies();
  }



  @Override
  public void onStop()
  {
    super.onStop();
    if (null != mDependencyManager) {
      mDependencyManager.unbindService();
      mDependencyManager = null;
    }
  }



  /***************************************************************************
   * DMActivity API Implementation
   **/

  /**
   * Bind to DependencyManager; this function does not do anything if we're
   * already bound. Successful binding triggers dependency resolution.
   **/
  public void bindDependencyManager()
  {
    if (mBindResult) {
      return;
    }
    mBindResult = DependencyManager.bindService(this, this);
  }



  /**
   * Returns true if binding succeeded, false otherwise.
   **/
  public boolean bindResult()
  {
    return mBindResult;
  }



  /**
   * Manually trigger dependency resolution. This will rebind to
   * DependencyManager if the binding was lost.
   **/
  public void resolveDependencies()
  {
    if (null == mDependencyManager) {
      Log.i(LTAG, "Not bound to a DependencyManager isntance, rebinding...");
      mBindResult = false;
      bindDependencyManager();
      return;
    }

    mDependencyManager.resolveDependencies(getPackageName());
  }



  /***************************************************************************
   * DependencyManager.BindListener Implementation
   **/

  /**
   * Result of binding to DependencyManager service; once we're bound we'll
   * try to resolve static dependencies of this package.
   **/
  public void onBound(DependencyManager dm)
  {
    mDependencyManager = dm;
    resolveDependencies();
  }



  /**
   * If we're unbound for whatever reason, make sure that resolveDependencies()
   * fails gracefully.
   **/
  public void onUnBound()
  {
    mDependencyManager = null;
    mBindResult = false;
  }
}
