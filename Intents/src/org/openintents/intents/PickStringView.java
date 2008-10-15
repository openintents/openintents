package org.openintents.intents;

import android.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class PickStringView extends ListActivity {

	static final String EXTRA_LIST = "list";
	protected static final String EXTRA_RETURN_EXTRA = "return_extra";
	private String mReturnExtra;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// store until action is finished
		mReturnExtra = getIntent().getExtras().getString(
				PickStringView.EXTRA_RETURN_EXTRA);

		// get list of strings
		String list = getIntent().getExtras().getString(
				PickStringView.EXTRA_LIST);
		String[] actions = list.split(" ");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.simple_list_item_1, actions);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView adapterview, View view,
							int i, long l) {
						String item = (String) getListAdapter().getItem(i);
						Intent intent = new android.content.Intent(item);
						intent.putExtra(EXTRA_RETURN_EXTRA, mReturnExtra);
						setResult(Activity.RESULT_OK, intent);
						finish();
					}

				});
	}
}
