package org.openintents.main;

import org.openintents.R;
import org.openintents.locations.LocationsView;
import org.openintents.tags.TagsView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class OpenIntents extends Activity implements OnItemClickListener {

	private String[] activitylist = { "Show locations", "Show tags",
			"Lookup geo->label", "Lookup label->geo" };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		ListView list = (ListView) findViewById(R.id.activities);
		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, activitylist));
		list.setOnItemClickListener(this);

	}

	public void onItemClick(AdapterView adapterView, View view, int position,
			long id) {
		switch (position) {
		case 0:
			Intent intent = new Intent(this, LocationsView.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(this, TagsView.class);
			startActivity(intent);
			break;

		}

	}

}