package org.openintents.historify;

import org.openintents.historify.data.adapters.SourcesAdapter;
import org.openintents.historify.data.model.AbstractSource;
import org.openintents.historify.data.model.AbstractSource.SourceState;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SourcesActivity extends Activity {

	public static final String NAME = "SourcesActivity";

	private ListView lstSources;
	private SourcesAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sources);

		lstSources = (ListView) findViewById(R.id.sources_lstSources);
		
		mAdapter = new SourcesAdapter(this, lstSources);

		lstSources.setAdapter(mAdapter);
		lstSources.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
				AbstractSource source = (AbstractSource)parent.getItemAtPosition(position);
				boolean checked = lstSources.getCheckedItemPositions().get(position);
				onSourceClicked(source, checked);
			}
		});
		
	}
	
	private void onSourceClicked(AbstractSource source, boolean checked) {
		source.setState(checked ? SourceState.ENABLED : SourceState.DISABLED);
		mAdapter.update(source);
	}
}
