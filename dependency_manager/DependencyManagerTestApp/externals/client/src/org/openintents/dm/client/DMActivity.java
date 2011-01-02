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
  private boolean mBindResult;

  // Dependency manager instance
  private DependencyManager mDependencyManager;



  /***************************************************************************
   * Implementation
   **/
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mBindResult = DependencyManager.bindService(this, this);
  }



  public void onBound(DependencyManager dm)
  {
    mDependencyManager = dm;
    mDependencyManager.resolveDependencies(getPackageName());
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



  /**
   * Returns true if binding succeeded, false otherwise.
   **/
  public boolean bindResult()
  {
    return mBindResult;
  }
}
