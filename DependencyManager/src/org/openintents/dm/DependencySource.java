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

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;

import android.content.res.XmlResourceParser;

import org.openintents.dm.common.Intents;
import org.openintents.dm.common.Schemas;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;
import android.util.AttributeSet;
import java.io.IOException;

import java.util.List;
import java.util.LinkedList;


import android.util.Log;


/**
 * Client object representing a data source for depedency information.
 **/
class DependencySource
{
  /***************************************************************************
   * Private constants
   **/
  public static final String LTAG = "DependencySource";


  /***************************************************************************
   * Private data
   **/
  // Name of the package that this DependencySource represents
  public static String packageName;

  // Name of the authority the ContentProvider in this package responds to,
  // which must conform to the DependencyManagerContract.
  public static String contentAuthority;


  /***************************************************************************
   * Implementation
   **/

  /**
   * Use this function to scan the system for any packages that appear to be
   * valid depedency sources.
   **/
  public static List<DependencySource> scanForSources(Context context)
  {
    PackageManager pm = context.getPackageManager();

    LinkedList<DependencySource> sources = new LinkedList<DependencySource>();

    // Find packages that respond to our ACTION_PACKAGE_INSTALL
    final Intent intent = new Intent(Intents.ACTION_PACKAGE_INSTALL);
    List<ResolveInfo> candidates = pm.queryIntentActivities(intent,
        PackageManager.GET_META_DATA);

    for (ResolveInfo resolveInfo : candidates) {
      ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
      DependencySource ds = DependencySource.createFromPackage(context, appInfo);
      if (null != ds) {
        sources.add(ds);
      }
    }

    if (0 >= sources.size()) {
      return null;
    }
    return sources;
  }



  /**
   * Use this function to create DependencySource objects.
   **/
  public static DependencySource createFromPackage(Context context,
      ApplicationInfo appInfo)
  {
    PackageManager pm = context.getPackageManager();

    String pkgName = appInfo.packageName;
    String authority = null;

    XmlResourceParser xml = appInfo.loadXmlMetaData(pm,
        Schemas.Source.META_DATA_LABEL);
    if (null == xml) {
      Log.e(LTAG, String.format("Package '%s' does not contain meta-data named '%s'",
            pkgName, Schemas.Source.META_DATA_LABEL));
      return null;
    }

    try {
      int tagType = xml.next();
      while (XmlPullParser.END_DOCUMENT != tagType) {

        if (XmlPullParser.START_TAG == tagType) {
          if (xml.getName().equals(Schemas.Source.ELEM_DEPENDENCY_SOURCE)) {
            AttributeSet attr = Xml.asAttributeSet(xml);
            if (null != attr) {
              authority = attr.getAttributeValue(Schemas.Source.SCHEMA,
                  Schemas.Source.ATTR_AUTHORITY);
            }
          }
        }

        tagType = xml.next();
      }

    } catch (XmlPullParserException ex) {
      Log.e(LTAG, String.format("XML parse exception when parsing metadata for '%s': %s",
            pkgName, ex.getMessage()));
    } catch (IOException ex) {
      Log.e(LTAG, String.format("I/O exception when parsing metadata for '%s': %s",
            pkgName, ex.getMessage()));
    }

    xml.close();

    // If authority is null, we consider parsing to have failed.
    if (null == authority) {
      Log.e(LTAG, String.format("No data source authority found for package '%s'",
            pkgName));
      return null;
    }

    return new DependencySource(pkgName, authority);
  }



  /**
   * Private constructor, use createFromPackage() to create objects
   **/
  private DependencySource(String pkgName, String authority)
  {
    super();
    packageName = pkgName;
    contentAuthority = authority;
  }



  public String getPackageName()
  {
    return packageName;
  }



  public String getContentAuthority()
  {
    return contentAuthority;
  }



  public String toString()
  {
    return String.format("<%s:%s>", packageName, contentAuthority);
  }
}
