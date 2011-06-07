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

package org.openintents.historify.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.FilterModesAdapter;
import org.openintents.historify.data.adapters.SourcesAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.utils.Toaster;
import org.openintents.historify.view.ContactChooserDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 
 * Displays and manages filters for sources and contacts.
 * 
 * @author berke.andras
 */
public class SourcesConfigurationFragment extends Fragment {

	// filter modes
	private Spinner mSpinnerFilterMode;
	private FilterModesAdapter mFilterModesAdapter;

	private View mBtnAddFilterMode;
	private View mBtnDeleteFilterMode;

	// sources
	private ListView mLstSources;
	private SourcesAdapter mSourcesAdapter;

	
	/** Called to have the fragment instantiate its user interface view.*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.sources_configuration_fragment, container, false);
		
		// init filter modes spinner
		mSpinnerFilterMode = (Spinner) layout.findViewById(R.id.sources_spinnerFilterMode);
		mSpinnerFilterMode
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						onFilterModeSelected((FilterModesAdapter) parent
								.getAdapter(), position);
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		// init buttons
		mBtnAddFilterMode = layout.findViewById(R.id.source_btnAddFilterMode);
		mBtnAddFilterMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onShowNewFilterModeDialog();
			}
		});
		mBtnDeleteFilterMode = layout.findViewById(R.id.source_btnDeleteFilterMode);
		mBtnDeleteFilterMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onDeleteFilterMode();
			}
		});

		// init sources list
		mLstSources = (ListView) layout.findViewById(R.id.sources_lstSources);
		mLstSources
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						AbstractSource source = (AbstractSource) parent
								.getItemAtPosition(position);
						boolean checked = mLstSources.getCheckedItemPositions()
								.get(position);
						onSourceClicked(source, checked);
					}
				});

		// filter modes adapter
		mFilterModesAdapter = new FilterModesAdapter(getActivity());
		mSpinnerFilterMode.setAdapter(mFilterModesAdapter);

		// sources adapter
		mSourcesAdapter = new SourcesAdapter(getActivity(), mLstSources);
		mLstSources.setAdapter(mSourcesAdapter);
		
		return layout;

	}
	
	/** Called when the user clicks on a source. */
	private void onSourceClicked(AbstractSource source, boolean checked) {
		source.setEnabled(checked);
		mSourcesAdapter.update(source);
	}

	/** Called when an item selected on the filter modes spinner. */
	private void onFilterModeSelected(FilterModesAdapter adapter, int position) {

		if (adapter.isDefault(position)) {
			// show filters for all contacts
			mSourcesAdapter.setFilterMode(null, position);
			mBtnDeleteFilterMode.setEnabled(false);

		} else if (adapter.isAddNew(position)) {
			// add filters for a contact
			onShowNewFilterModeDialog();
			mBtnDeleteFilterMode.setEnabled(false);

		} else {
			// show filters for a contact
			mSourcesAdapter.setFilterMode(adapter.getItem(position), position);
			mBtnDeleteFilterMode.setEnabled(true);
		}

	}

	/**
	 * Displays a dialog which let the user select a contact to insert new
	 * filters for.
	 */
	private void onShowNewFilterModeDialog() {

		ContactChooserDialog dialog = new ContactChooserDialog(getActivity(),
				mFilterModesAdapter.getItems());

		dialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				Contact selected = ((ContactChooserDialog) dialog)
						.getSelectedContact();
				onAddNewFilterMode(selected);
			}

		});

		dialog.show();

	}

	/** Called when user dismisses a {@link ContactChooserDialog}. */
	private void onAddNewFilterMode(Contact contact) {

		if (contact == null) {
			// dialog has been canceled
			// restore previously selected item on the spinner
			mSpinnerFilterMode.setSelection(mSourcesAdapter
					.getFilterModePosition());

		} else {
			// contact has been selected
			// adding new filter mode for the contact
			int position = mFilterModesAdapter.insert(contact, mSourcesAdapter);

			if (position != -1) {
				int previousPostion = mSpinnerFilterMode
						.getSelectedItemPosition();
				if (position == previousPostion) {
					// The inserted item is at the current position.
					// Spinner's OnItemSelectedListener won't be notified
					// so we call onFilterModeSelected explicitly.
					onFilterModeSelected(mFilterModesAdapter, position);
				} else {
					// The inserted item is at a new position,
					// so set the selection on the spinner.
					// onFilterModeSelected will be called by the spinner.
					mSpinnerFilterMode.setSelection(position);
				}
			} else {
				// invalid position
				mSpinnerFilterMode.setSelection(0);
			}
		}

	}

	/** Delete all filters installed for the currently selected contact. */
	private void onDeleteFilterMode() {

		int actFilterModePosition = mSourcesAdapter.getFilterModePosition();

		if (!mFilterModesAdapter.isDefault(actFilterModePosition)
				&& !mFilterModesAdapter.isAddNew(actFilterModePosition)) {

			String contactName = mFilterModesAdapter.getItem(
					actFilterModePosition).getName();

			mFilterModesAdapter.delete(actFilterModePosition);
			mSpinnerFilterMode.setSelection(0);

			Toaster.toast(this, R.string.sources_filter_deleted, contactName);
		}
	}

}
