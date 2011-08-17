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
import org.openintents.historify.data.adapters.QuickPostSourcesAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * Displays QuickPost sources.
 * 
 * @author berke.andras
 * 
 */
public class QuickPostSourcesFragment extends Fragment {

	// sources
	private ListView mLstQuickPostSources;
	private QuickPostSourcesAdapter mSourcesAdapter;

	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_quickpost_sources, container, false);

		// init list
		mLstQuickPostSources = (ListView) layout
				.findViewById(R.id.quickpost_sources_lstQuickPostSources);

		// init list empty view
		View lstQuickPostsEmptyView = inflater.inflate(
				R.layout.list_empty_view, null);
		((TextView) lstQuickPostsEmptyView)
				.setText(R.string.sources_no_quickpost_sources);
		((ViewGroup) mLstQuickPostSources.getParent())
				.addView(lstQuickPostsEmptyView);
		mLstQuickPostSources.setEmptyView(lstQuickPostsEmptyView);

		mSourcesAdapter = new QuickPostSourcesAdapter(getActivity(),
				mLstQuickPostSources);
		mLstQuickPostSources.setAdapter(mSourcesAdapter);

		return layout;
	}

	/**
	 * Called when the fragment's activity has been created and this fragment's
	 * view hierarchy instantiated.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSourcesAdapter.load();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSourcesAdapter.release();
	}
}
