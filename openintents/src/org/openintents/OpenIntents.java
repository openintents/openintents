/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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
 */

/*
<!--
$Id:: Shopping.java 24 2008-03-20 11:30:15Z muthu.ramadoss                      $: Id of last commit
$Rev:: 24                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-03-20 17:00:15 +0530 (Thu, 20 Mar 2008)                            $: Date of last commit
$HeadURL:: https://ibt-macd.googlecode.com/svn/trunk/capsules/cooking/src/com/i#$: Head URL of last commit
-->
*/

package org.openintents;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Provides OpenIntents action and category specifiers.
 * <p/>
 * These specifiers extend the standard Android specifiers.
 */
public abstract class OpenIntents
{

    // -----------------------------------------------
    //                     Tags
    // -----------------------------------------------
    /**
     * identifier for tag action.
     */
    public static final String TAG_ACTION = "org.openintents.action.TAG";

    // -----------------------------------------------
    //                     Categories
    // -----------------------------------------------
    /**
     * Main category specifier.
     * <p/>
     * Applications placed into this category in the AndroidManifest.xml file are
     * displayed in the main view of OpenIntents.
     */
    public static final String MAIN_CATEGORY = "org.openintents.category.MAIN";

    /**
     * Settings category specifier.
     * <p/>
     * Applications placed into this category in the AndroidManifest.xml file are
     * displayed in the settings tab of OpenIntents.
     */
    public static final String SETTINGS_CATEGORY
            = "org.openintents.category.SETTINGS";

    /**
     * Add stripes to the list view.
     *
     * @param listView
     * @param activity
     */
    public static void setupListStripes(ListView listView, Activity activity)
    {
        // Get Drawables for alternating stripes
        Drawable[] lineBackgrounds = new Drawable[2];

        lineBackgrounds[0] =
                activity.getResources().getDrawable(R.drawable.gold);
        lineBackgrounds[1] =
                activity.getResources().getDrawable(R.drawable.yellow_green);

        // Make and measure a sample TextView of the sort our adapter will
        // return
        View view = activity.getViewInflate().inflate(
                android.R.layout.simple_list_item_1, null, null);

        TextView v = (TextView) view.findViewById(android.R.id.text1);
        v.setText("X");
        // Make it 100 pixels wide, and let it choose its own height.
        v.measure(
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 100),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED,
                        0));
        int height = v.getMeasuredHeight();
        listView.setStripes(lineBackgrounds, height);
    }
}
