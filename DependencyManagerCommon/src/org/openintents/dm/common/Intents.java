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

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

import android.content.res.XmlResourceParser;

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
 * Intents
 **/
public class Intents
{
  /***************************************************************************
   * Private constants
   **/
  private static final String LTAG = "Intents";



  /***************************************************************************
   * Actions
   **/
  // Action for installing packages
  public static final String ACTION_PACKAGE_INSTALL = "de.finkhaeuser.dm.intent.action.PACKAGE_INSTALL";



  /***************************************************************************
   * Convenience functions
   **/

  /**
   * Parses serialized Intents from the query string of the given URI, assuming
   * the parameter key of DepedencyManagerContract.QUERY_PARAM_INTENT. Returns
   * null if no intents are found in the Uri.
   **/
  public static List<Intent> parseIntents(Uri uri)
  {
    List<String> values = uri.getQueryParameters(
        DependencyManagerContract.QUERY_PARAM_INTENT);
    if (null == values) {
      return null;
    }

    LinkedList<Intent> results = new LinkedList<Intent>();
    for (String v : values) {
      try {
        results.add(Intent.parseUri(v, Intent.URI_INTENT_SCHEME));
      } catch (java.net.URISyntaxException ex) {
        // pass
      }
    }

    if (0 >= results.size()) {
      return null;
    }

    return results;
  }



  /**
   * Parses Intents from the named package's dependency metadata, and returns
   * them as a list. Returns null if no such metadata was found.
   **/
  public static List<Intent> parseIntents(Context context, String packageName)
  {
    // Grab applicaton info.
    PackageManager pm = context.getPackageManager();
    ApplicationInfo appInfo;
    try {
      appInfo = pm.getApplicationInfo(packageName,
          PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException ex) {
      Log.e(LTAG, String.format("Package '%s' not found!", packageName));
      return null;
    }

    if (null == appInfo) {
      Log.e(LTAG, String.format("Package '%s' not found!", packageName));
      return null;
    }

    // Start parsing metadata
    XmlResourceParser xml = appInfo.loadXmlMetaData(pm,
        Schemas.Client.META_DATA_LABEL);
    if (null == xml) {
      Log.e(LTAG, String.format("Package '%s' does not contain meta-data named '%s'",
            packageName, Schemas.Client.META_DATA_LABEL));
      return null;
    }


    LinkedList<Intent> result = null;
    Intent intent = null;
    try {
      int tagType = xml.next();
      while (XmlPullParser.END_DOCUMENT != tagType) {

        if (XmlPullParser.START_TAG == tagType) {

          AttributeSet attr = Xml.asAttributeSet(xml);

          if (xml.getName().equals(Schemas.Client.ELEM_DEPENDENCIES)) {
            result = new LinkedList<Intent>();
          }

          else if (xml.getName().equals(Schemas.Client.ELEM_INTENT)) {
            intent = new Intent();

            String ctype = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_COMPONENT_TYPE);
            if (null != ctype) {
              intent.putExtra(Schemas.Client.EXTRA_COMPONENT_TYPE, ctype);
            }
          }

          else if (xml.getName().equals(Schemas.Client.ELEM_COMPONENT)) {
            String name = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_NAME);
            if (null != name) {
             ComponentName cname = ComponentName.unflattenFromString(name);
             intent.setComponent(cname);
            }
          }

          else if (xml.getName().equals(Schemas.Client.ELEM_ACTION)) {
            if (null != intent.getAction()) {
              Log.w(LTAG, String.format("Only one <%s> tag is supported per "
                    + "<%s>; overwriting previous values.",
                    Schemas.Client.ELEM_ACTION,
                    Schemas.Client.ELEM_INTENT));
            }
            String name = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_NAME);
            if (null != name) {
              intent.setAction(name);
            }
          }

          else if (xml.getName().equals(Schemas.Client.ELEM_CATEGORY)) {
            String name = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_NAME);
            if (null != name) {
              intent.addCategory(name);
            }
          }

          else if (xml.getName().equals(Schemas.Client.ELEM_DATA)) {
            String uri_string = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_URI);
            Uri uri = null;
            if (null != uri_string) {
              uri = Uri.parse(uri_string);
            }

            String mimeType = attr.getAttributeValue(Schemas.Client.SCHEMA,
                Schemas.Client.ATTR_MIME_TYPE);

            if (null != uri && null != mimeType) {
              intent.setDataAndType(uri, mimeType);
            }
            else if (null != uri) {
              intent.setData(uri);
            }
            else if (null != mimeType) {
              intent.setType(mimeType);
            }
          }
        }

        else if (XmlPullParser.END_TAG == tagType) {

          if (xml.getName().equals(Schemas.Client.ELEM_INTENT)) {
            if (null != intent && (null != intent.getData()
                  || null != intent.getAction() || null != intent.getCategories()
                  || null != intent.getType() || null != intent.getComponent()))
            {
              result.add(intent);
            }
          }
        }

        tagType = xml.next();
      }

    } catch (XmlPullParserException ex) {
      Log.e(LTAG, String.format("XML parse exception when parsing metadata for '%s': %s",
            packageName, ex.getMessage()));
    } catch (IOException ex) {
      Log.e(LTAG, String.format("I/O exception when parsing metadata for '%s': %s",
            packageName, ex.getMessage()));
    }

    xml.close();

    if (null == result || 0 >= result.size()) {
      return null;
    }

    return result;
  }



  /**
   * Serializes the list of Intents into a query string format, using
   * DependencyManagerContract.QUERY_PARAM_INTENT as the parameter key.
   * Returns null if the list is null or empty.
   **/
  public static String serializeIntents(List<Intent> intents)
  {
    if (null == intents || 0 >= intents.size()) {
      return null;
    }

    String result = new String();
    for (Intent i : intents) {
      result = String.format("%s&%s=%s", result,
          DependencyManagerContract.QUERY_PARAM_INTENT,
          Uri.encode(i.toUri(Intent.URI_INTENT_SCHEME)));
    }

    if (0 >= result.length()) {
      return null;
    }

    // Trim leading ampersand
    return result.substring(1);
  }



  /**
   * Serialize a single Intent. @see serializeIntents
   **/
  public static String serializeIntent(Intent intent)
  {
    return String.format("%s=%s",
        DependencyManagerContract.QUERY_PARAM_INTENT,
        Uri.encode(intent.toUri(Intent.URI_INTENT_SCHEME)));
  }

}
