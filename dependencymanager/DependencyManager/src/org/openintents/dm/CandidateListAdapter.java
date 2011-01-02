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

package org.openintents.dm;

import java.util.HashMap;

import org.openintents.dm.common.DependencyManagerContract.CandidateColumns;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * List Adapter for a cursor as returned from DependencyResolutionProvider.
 **/
public class CandidateListAdapter extends BaseAdapter
{
  /***************************************************************************
   * Private constants
   **/
  // Log ID
  private static final String LTAG = "CandidateListAdapter";

  // View types. These are indices into mLayoutIds, so must start at zero and
  // be sequential numbers.
  private static final int VIEW_TYPE_APP    = 0;
  private static final int VIEW_TYPE_SEARCH = 1;



  /***************************************************************************
   * The ScrollListener triggers thumbnail downloads if scrolling stops, and
   * interrupts them when it starts again.
   **/
  public static class ScrollListener implements AbsListView.OnScrollListener
  {
    private CandidateListAdapter  mAdapter;

    private boolean           mSentInitial = false;
    private int               mFirst;
    private int               mCount;

    public ScrollListener(CandidateListAdapter adapter)
    {
      mAdapter = adapter;
    }



    public int getFirst()
    {
      return mFirst;
    }



    public int getCount()
    {
      return mCount;
    }



    public void onScroll(AbsListView view, int firstVisibleItem,
        int visibleItemCount, int totalItemCount)
    {
      mFirst = firstVisibleItem;
      mCount = visibleItemCount;

      if (!mSentInitial && visibleItemCount > 0) {
        mSentInitial = true;
        mAdapter.startHeavyLifting(mFirst, mCount);
      }
    }



    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
      if (SCROLL_STATE_IDLE != scrollState) {
        mAdapter.cancelHeavyLifting();
        return;
      }

      mAdapter.startHeavyLifting(mFirst, mCount);
    }
  }



  /***************************************************************************
   * Data members
   **/
  private Context             mContext;
  private int[]               mLayoutIds;

  // Result cursor
  private Cursor              mCursor;

  // Maps back and forth between the views currently returned from getView,
  // and the position in mMedia they display.
  private HashMap<Integer, View>  mPositionToView = new HashMap<Integer, View>();
  private HashMap<View, Integer>  mViewToPosition = new HashMap<View, Integer>();



  /***************************************************************************
   * Static data members
   **/
  // The no_image drawable does not need to be loader every time.
  private static Drawable   smNoImage;



  /***************************************************************************
   * Implementation
   **/
  public CandidateListAdapter(Context context, int[] layoutIds, Cursor cursor)
  {
    mContext = context;
    mLayoutIds = layoutIds;
    mCursor = cursor;

    mCursor.registerContentObserver(new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange)
        {
          mCursor.requery();
          notifyDataSetChanged();
        }
    });
  }



  /***************************************************************************
   * Adapter Implementation
   **/
  public int getCount()
  {
    if (null == mCursor) {
      return 0;
    }
    return mCursor.getCount();
  }



  public Object getItem(int position)
  {
    // XXX unused.
    return null;
  }



  public long getItemId(int position)
  {
    // XXX We're not filtering, so this is fine.
    return position;
  }



  public int getViewTypeCount()
  {
    // We return two types of views: one for apps, and one for external
    // searches.
    return 2;
  }



  public int getItemViewType(int position)
  {
    mCursor.moveToPosition(position);
    if (mCursor.isNull(mCursor.getColumnIndex(
            CandidateColumns.EXTERNAL_SEARCH_URI)))
    {
      return VIEW_TYPE_APP;
    }
    return VIEW_TYPE_SEARCH;
  }



  public View getView(int position, View convertView, ViewGroup parent)
  {
    // Create new view, if required.
    View view = convertView;
    if (null == view) {
      int layoutId = mLayoutIds[getItemViewType(position)];
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
          Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(layoutId, null);
    }

    // Grab the position the view currently points to.
    Integer cur_position = mViewToPosition.get(view);
    if (null != cur_position) {
      // The view has been used already at some point.
      if (cur_position != position) {
        // The view is currently used for a different position. We'll need
        // to update both maps.
        mPositionToView.remove(cur_position);
      }
    }
    mViewToPosition.put(view, position);
    mPositionToView.put(position, view);

    // Load item data.
    mCursor.moveToPosition(position);

    String store_name = mCursor.getString(mCursor.getColumnIndex(
          CandidateColumns.STORE_DISPLAY_NAME));
    String store_package = mCursor.getString(mCursor.getColumnIndex(
          CandidateColumns.STORE_PACKAGE));
    String display_name = mCursor.getString(mCursor.getColumnIndex(
          CandidateColumns.DISPLAY_NAME));
    String uri_str = mCursor.getString(mCursor.getColumnIndex(
          CandidateColumns.ICON_URI));
    Uri icon_uri = null;
    if (null != uri_str) {
      icon_uri = Uri.parse(uri_str);
    }

    Uri search_uri = null;
    String app_package = null;
    String app_vendor = null;
    int app_price = -1;
    String app_currency = null;
    String app_matches = null;
    if (!mCursor.isNull(mCursor.getColumnIndex(
            CandidateColumns.EXTERNAL_SEARCH_URI)))
    {
      uri_str = mCursor.getString(mCursor.getColumnIndex(
            CandidateColumns.EXTERNAL_SEARCH_URI));
      if (null != uri_str) {
        search_uri = Uri.parse(uri_str);
      }
    }
    else {
      app_package = mCursor.getString(mCursor.getColumnIndex(
            CandidateColumns.APP_PACKAGE));
      app_vendor = mCursor.getString(mCursor.getColumnIndex(
            CandidateColumns.APP_VENDOR_NAME));
      if (!mCursor.isNull(mCursor.getColumnIndex(
              CandidateColumns.APP_PRICE)))
      {
        app_price = mCursor.getInt(mCursor.getColumnIndex(
              CandidateColumns.APP_PRICE));
        app_currency = mCursor.getString(mCursor.getColumnIndex(
              CandidateColumns.APP_CURRENCY));
      }
      app_matches = mCursor.getString(mCursor.getColumnIndex(
            CandidateColumns.APP_MATCHES));
    }

    // Fill view
    setText(view, R.id.display_name, display_name,
        R.string.def_display_name);
    setText(view, R.id.app_vendor_name, app_vendor,
        R.string.def_app_vendor_name);
    setText(view, R.id.store_display_name, store_name,
        R.string.def_store_display_name);
    if (app_price > 0) {
      // TODO This needs a bit of work. Not all currencies' smaller unit divides
      //  the bigger by 100, and the formatting leaves a bit to be desired.
      String price = String.format("%.2f %s", app_price / 100f, app_currency);
      setText(view, R.id.app_price, price, R.string.def_app_price);
    }
    else {
      setText(view, R.id.app_price, null, R.string.app_price_free);
    }

    // Set icon to default - real icons will be loaded later.
    if (null == smNoImage) {
      smNoImage = mContext.getResources().getDrawable(R.drawable.icon);
    }
    ImageView image_view = (ImageView) view.findViewById(R.id.icon);
    if (null != image_view) {
      image_view.setImageDrawable(smNoImage);
    }

    return view;
  }



  private void setText(View parent, int viewId, String text, int defaultId)
  {
    TextView view = (TextView) parent.findViewById(viewId);
    if (null == view) {
      return;
    }

    if (null == text) {
      text = mContext.getResources().getString(defaultId);
    }

    view.setText(text);
  }



  private void startHeavyLifting(int first, int count)
  {
    // TODO load icons for first... count here.
  }



  private void cancelHeavyLifting()
  {
    // TODO cancel loading of icons here.
  }
}
