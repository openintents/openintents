/*
 * Copyright (C) 2008 The Android Open Source Project
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

package org.openintents.calendarpicker;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.View;


public class MiniMonthDrawable extends Drawable {

	
	public class CalendarDayCell {

		RectF day_rect;
		CalendarDay calendar_day;
	}
	

    private List<CalendarDayCell> day_cells;

    private List<CalendarDay> day_list = new ArrayList<CalendarDay>();
	int active_month;

	
	final static String TAG = "Chartdroid";

	GregorianCalendar cal;
	Context context;
	View view;
	float day_border_width = 1;
	public MiniMonthDrawable(Context c, View v, GregorianCalendar cal) {
		context = c;
		view = v;
		this.cal = cal;

    	active_month = CalendarUtils.generate_days(cal, day_list, new ArrayList<SimpleEvent>());
    	
	}
	
	
	public List<CalendarDayCell> arrange_day_cells(Canvas canvas, List<CalendarDay> day_list, View view) {

		int view_w = view.getWidth();
		int view_h = view.getHeight();
		
		float cell_width = (view_w - day_border_width) / 7f;
		float cell_height = view_h / (day_list.size()/7);
		
		
	    List<CalendarDayCell> day_cells = new ArrayList<CalendarDayCell>();

	    
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setAntiAlias(false);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.STROKE);

		


		int i = 0;
		for (CalendarDay day : day_list) {
			
			if (day.date.getMonth() == active_month) {
				float left = cell_width*(i % 7);
				float top = cell_height*(i / 7);
				
				
		        RectF mRect = new RectF(left, top, left + cell_width, top + cell_height);
				canvas.drawRect(mRect, paint);
			}
			
			i++;
		}
	    
	    return day_cells;
	}
	

	
	public void draw(Canvas canvas) {

		int view_w = view.getWidth();
		int view_h = view.getHeight();
//		Log.e(TAG, "View dimensions: (" + view_w + ", " + view_h + ")");

		/*
		Paint paint = new Paint();
		paint.setColor(this.color);
		paint.setStrokeWidth(5);
		
        RectF mRect = new RectF(0, 0, view_w, view_h);	// The mini-cal background
		canvas.drawRect(mRect, paint);
		*/
		

		Paint text_paint = new Paint();
		text_paint.setAntiAlias(true);
		text_paint.setFakeBoldText(true);
		text_paint.setColor(Color.DKGRAY);
		text_paint.setTextAlign(Align.CENTER);
		text_paint.setTextSize( Math.min(view_w, view_h)/2f );
//		text_paint.setStyle(Style.STROKE);
		
		Rect cap_text_bounds = new Rect();
		text_paint.getTextBounds("A", 0, 1, cap_text_bounds);
		
		
		float capital_height = cap_text_bounds.top;
		String month_string = new DateFormatSymbols().getShortMonths()[ cal.getTime().getMonth() ];
		canvas.drawText(month_string, view_w/2f, (view_h - capital_height)/2f, text_paint);

    	day_cells = arrange_day_cells(canvas, day_list, view);
	}

	public int getOpacity() {
//		return hosted_drawable.getOpacity();
		return 0xFF;
	}

	public void setAlpha(int alpha) {
//		hosted_drawable.setAlpha(alpha);
		
		return;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
//		hosted_drawable.setColorFilter(cf);
		
		return;
	}
	
	
/*
	@Override
	public boolean getPadding(Rect padding) {
		
		padding.bottom = 2;
		padding.top = 2;
		padding.left = (int) r*3;
		padding.right = 2;
		
		return true;
	}
*/
}
    


