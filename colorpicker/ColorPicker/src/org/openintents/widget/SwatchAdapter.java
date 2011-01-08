package org.openintents.widget;

import java.util.List;

import org.openintents.colorpicker.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class SwatchAdapter extends BaseAdapter {

	private final List<Integer> color_list;

	Context context;
	LayoutInflater factory;
	public SwatchAdapter(Context context, List<Integer> color_list) {
		this.context = context;
        this.factory = LayoutInflater.from(this.context);
        
        this.color_list = color_list;
	}
	
	@Override
	public int getCount() {
		return color_list.size();
	}

    /* Use the array-Positions as unique IDs */
	@Override
    public Integer getItem(int position) {
		return color_list.get(position);
    }

	@Override
    public long getItemId(int position) { return position; }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
        	convertView = factory.inflate(R.layout.color_swatch, null);
        }

	    int color = color_list.get(position);
	    
	    SwatchView icon = (SwatchView) convertView.findViewById(android.R.id.icon);
	    icon.setColor(color);
	    icon.setGrow(true);

        return convertView;
	}
}