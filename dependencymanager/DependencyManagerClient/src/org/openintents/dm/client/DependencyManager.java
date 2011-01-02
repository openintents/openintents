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

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.openintents.dm.IDependencyManager;

/**
 * Client object to the IDependencyManager interface; simplifies binding to
 * the service.
 **/
public class DependencyManager
{
  /***************************************************************************
   * Private constants
   **/
  private static final String LTAG = "DependencyManager";



  /***************************************************************************
   * Implement BindListener to receive a DependencyManager instance that's
   * bound to IDependencyManager.
   **/
  public interface BindListener
  {
    public void onBound(DependencyManager dm);
    public void onUnBound();
  }



  /***************************************************************************
   * Private data
   **/
  private Context                     mContext;
  private BindListener                mBindListener;

  // Service stub
  private volatile IDependencyManager mStub = null;

  // Service connection
  private ServiceConnection           mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service)
    {
      mStub = IDependencyManager.Stub.asInterface(service);
      mBindListener.onBound(DependencyManager.this);
    }


    public void onServiceDisconnected(ComponentName className)
    {
      mStub = null;
      mBindListener.onUnBound();
    }
  };



  /***************************************************************************
   * Extra functions
   **/

  /**
   * Returns true if binding was successful, false otherwise. Shortly after a
   * successful bind, BindListener's onBound() method will be called with
   * a DependencyManager instance through which you can communicate with the
   * service.
   **/
  public static boolean bindService(Context context, BindListener listener)
  {
    DependencyManager dm = new DependencyManager(context, listener);
    return dm.bindService();
  }



  /**
   * Always unbind each DependencyManager object.
   **/
  public void unbindService()
  {
    if (null == mStub) {
      return;
    }

    mContext.unbindService(mConnection);
  }



  private DependencyManager(Context context, BindListener listener)
  {
    mContext = context;
    mBindListener = listener;
  }



  private boolean bindService()
  {
    return mContext.bindService(
        new Intent(IDependencyManager.class.getName()),
        mConnection, Context.BIND_AUTO_CREATE);
  }



  /***************************************************************************
   * Implementation of IDependencyManager API
   **/

  public void resolveDependencies(String packageName)
  {
    try {
      mStub.resolveDependencies(packageName);
    } catch (RemoteException ex) {
      Log.e(LTAG, "Exception " + ex.getMessage());
    }
  }



  List<Intent> scanPackageForDependencies(String packageName)
  {
    try {
      return mStub.scanPackageForDependencies(packageName);
    } catch (RemoteException ex) {
      Log.e(LTAG, "Exception " + ex.getMessage());
      return null;
    }
  }



  List<Intent> removeResolvableIntents(List<Intent> intents)
  {
    try {
      return mStub.removeResolvableIntents(intents);
    } catch (RemoteException ex) {
      Log.e(LTAG, "Exception " + ex.getMessage());
      return null;
    }
  }



  void displayChoicesForIntents(List<Intent> intents)
  {
    try {
      mStub.displayChoicesForIntents(intents);
    } catch (RemoteException ex) {
      Log.e(LTAG, "Exception " + ex.getMessage());
    }
  }

}
