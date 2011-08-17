/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.ui.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.SourcesAdapter;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.utils.WebsiteHelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * 
 * Displays and manages the different event sources.
 * 
 * @author berke.andras
 */
public class SourcesConfigurationFragment extends Fragment {

	// sources
	private ListView mLstSources;
	private SourcesAdapter mSourcesAdapter;

	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_sources_configuration, container, false);

		// init sources list
		mLstSources = (ListView) layout.findViewById(R.id.sources_lstSources);
		mLstSources
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						if (position == parent.getCount() - 1) {
							// need more message
							new WebsiteHelper().navigateToMoreInfo(getActivity());
						} else {
							EventSource source = (EventSource) parent
									.getItemAtPosition(position);
							boolean checked = mLstSources
									.getCheckedItemPositions().get(position);
							onSourceClicked(source, checked);
						}

					}
				});

		// sources adapter
		mSourcesAdapter = new SourcesAdapter(getActivity(), mLstSources);
		mLstSources.setAdapter(mSourcesAdapter);

		return layout;

	}

	/** Called when the user clicks on a source. */
	private void onSourceClicked(EventSource source, boolean checked) {
		source.setEnabled(checked);
		mSourcesAdapter.update(source);
	}

	/** Class to handle when the user clicks on a source's "More" button. */
	public static class OnMoreButtonClickedListener implements OnClickListener {
		public void onClick(View v) {
			// the intent to be fired is stored in the view's tag
			String action = (String) v.getTag();

			if (action != null) {
				Intent i = new Intent();
				i.setAction(action);
				try {
					v.getContext().startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSourcesAdapter.release();
	}
}
