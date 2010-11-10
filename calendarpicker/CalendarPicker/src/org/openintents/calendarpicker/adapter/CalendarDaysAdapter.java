package org.openintents.calendarpicker.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.CalendarUtils;
import org.openintents.calendarpicker.CalendarUtils.ViewHolderCalendarDay;
import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openintents.calendarpicker.R;

public class CalendarDaysAdapter extends BaseAdapter {

	final static public String TAG = "Calendar";
	
    private LayoutInflater mInflater;
	public GregorianCalendar cal;
	public int active_month;
    private List<CalendarDay> day_list = new ArrayList<CalendarDay>();
	
    Context context;
    public CalendarDaysAdapter(Context context, LayoutInflater inflator, List<SimpleEvent> events) {
    
    	this.context = context;
    	mInflater = inflator;
    	
    	cal = new GregorianCalendar();
    	// Zero the time of the calendar
    	cal.set(
    		cal.get(GregorianCalendar.YEAR),
    		cal.get(GregorianCalendar.MONTH),
    		cal.get(GregorianCalendar.DATE),
    		0, 0, 0);

    	Log.d(TAG, "In CalendarDaysAdapter");
    	active_month = CalendarUtils.generate_days(cal, day_list, events);
    	Log.i(TAG, "With active month: " + active_month);
    }


    
    
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderCalendarDay holder;
        if (convertView == null) {
        	
        	convertView = mInflater.inflate(R.layout.calendar_day_item, null);
        	
        	holder = new ViewHolderCalendarDay();
        	holder.thumb = (ImageView) convertView.findViewById(R.id.thumb_holder);
            holder.title = (TextView) convertView.findViewById(R.id.label_holder);
            holder.datum = (TextView) convertView.findViewById(R.id.datum_holder);

            convertView.setTag(holder);

        } else {

            // Get the ViewHolder back to get fast access to the View elements.
            holder = (ViewHolderCalendarDay) convertView.getTag();
        }

        CalendarDay cal_day = (CalendarDay) getItem(position);
        Date d = cal_day.date;
        



        
        
        // This doesn't work at all:
//        convertView.setFocusable(d.getMonth() == active_month);
        
        holder.title.setText( Integer.toString( d.getDate() ) );
        
        int event_count = cal_day.day_events.size();
        holder.datum.setText( event_count > 0 ? Integer.toString( event_count ) : "" );
        

        
        if (d.getMonth() != active_month) {
        	holder.title.setTextColor(Color.DKGRAY);
        	holder.datum.setTextColor(Color.DKGRAY);
        } else {
        	holder.title.setTextColor(context.getResources().getColorStateList(R.color.daycell));
        	holder.datum.setTextColor(context.getResources().getColorStateList(R.color.daycell));
        	/*
        	holder.title.setTextColor(Color.LTGRAY);
        	holder.datum.setTextColor(Color.WHITE);
        	*/
        	
            if (event_count > 0) {
            	convertView.setBackgroundColor(Color.argb(0x40, 0xFF, 0xFF, 0));
            } else {
            	convertView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        
        return convertView;
    }

	@Override
	public boolean areAllItemsEnabled() {

		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		
		CalendarDay day = (CalendarDay) getItem(position);
		Log.d(TAG, "Current month: " + day.date.getMonth() + "; Active month: " + active_month);
		return day.date.getMonth() == active_month;
	}
	
    public final int getCount() {
        return day_list.size();
    }

    public final Object getItem(int position) {
        return day_list.get(position);
    }

    public final long getItemId(int position) {
        return position;
    }
}