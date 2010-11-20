package org.openintents.calendarpicker.container;

import org.openintents.calendarpicker.activity.PeriodBrowsingActivity.TimespanEventMaximums;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;
import org.openintents.calendarpicker.view.FlingableMonthView;

import android.graphics.Color;
import android.util.Log;

public class ColorMappingConfiguration {
	
    final static public String TAG = "ColorMappingConfiguration";

	enum ColorMappingSource {
		EVENT_COUNT, EXTRA_QUANTITY
	}



	public boolean enabled = false;


    public static interface ColorMappingHost {
    	ColorMappingConfiguration getColorMappingConfig();
    }

	ColorMappingSource mapping_source = ColorMappingSource.EVENT_COUNT;
	public int extra_quantity_index = 0;
	public void setColormapSource(int extra_quantity_index) {
		if (extra_quantity_index < CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length) {
    		if (extra_quantity_index >= 0) {
    			this.mapping_source = ColorMappingSource.EXTRA_QUANTITY;
    			this.extra_quantity_index = extra_quantity_index;
    		} else
    			this.mapping_source = ColorMappingSource.EVENT_COUNT;
		} else
			Log.e(TAG, "Color mapping source is out of range: " + extra_quantity_index);
	}
	
	

	public int[] color_stops = new int[] {Color.BLACK, Color.MAGENTA};
	
	/** Assign equidistant color stops.  Validates the input */
	public int interpolateColorStops(float fraction) {
		
		int max_color_index = this.color_stops.length - 1;
		float high_fractional_value = fraction * max_color_index;
		int low_index = (int) high_fractional_value;
		int high_index = (int) Math.ceil(fraction * max_color_index);
		
		float partial_fraction = high_fractional_value - low_index;
		
		return FlingableMonthView.interpolateColor(this.color_stops[low_index], this.color_stops[high_index], partial_fraction);
	}

	
	public float getFraction(TimespanEventAggregator day, TimespanEventMaximums maxes) {

		float fraction = 0;
		switch (this.mapping_source) {
		case EVENT_COUNT:
		{
			fraction = maxes.max_event_count_per_day == 0 ? 0 : day.getEventCount() / (float) maxes.max_event_count_per_day;
			break;
		}
		case EXTRA_QUANTITY:
		{
			float max = maxes.max_quantities_per_day[this.extra_quantity_index];
			fraction = max == 0 ? 0 : day.getAggregateQuantity(this.extra_quantity_index) / max;
			break;
		}
		}
		
		return fraction;
	}

	
	public int getTileColor(TimespanEventAggregator day, TimespanEventMaximums maxes) {
		return interpolateColorStops(getFraction(day, maxes));
	}
}
