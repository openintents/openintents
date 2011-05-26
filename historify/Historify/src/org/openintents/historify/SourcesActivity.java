package org.openintents.historify;

import org.openintents.historify.data.adapters.FilterModesAdapter;
import org.openintents.historify.data.adapters.SourcesAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;
import org.openintents.historify.view.NewFilterModeDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SourcesActivity extends Activity {

	public static final String NAME = "SourcesActivity";

	private Spinner mSpinnerFilterMode;
	private FilterModesAdapter mFilterModesAdapter;
	private View mBtnAddFilterMode;
	private View mBtnDeleteFilterMode;
	
	private ListView mLstSources;
	private SourcesAdapter mSourcesAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sources);

		mSpinnerFilterMode = (Spinner) findViewById(R.id.sources_spinnerFilterMode);
		mLstSources = (ListView) findViewById(R.id.sources_lstSources);

		mFilterModesAdapter = new FilterModesAdapter(this);
		mSpinnerFilterMode.setAdapter(mFilterModesAdapter);
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

		mBtnAddFilterMode = findViewById(R.id.source_btnAddFilterMode);
		mBtnAddFilterMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onShowNewFilterModeDialog();
			}
		});
		
		mBtnDeleteFilterMode = findViewById(R.id.source_btnDeleteFilterMode);
		mBtnDeleteFilterMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onDeleteFilterMode();
			}
		});
		
		mSourcesAdapter = new SourcesAdapter(this, mLstSources);
		mLstSources.setAdapter(mSourcesAdapter);
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

	}


	private void onFilterModeSelected(FilterModesAdapter adapter, int position) {

		if (adapter.isDefault(position)) {
			mSourcesAdapter.setFilterMode(null, position);
			mBtnDeleteFilterMode.setEnabled(false);
		} else if (adapter.isAddNew(position)) {
			onShowNewFilterModeDialog();
			mBtnDeleteFilterMode.setEnabled(false);
		} else {
			mSourcesAdapter.setFilterMode(adapter.getItem(position), position);
			mBtnDeleteFilterMode.setEnabled(true);
		}

	}

	private void onShowNewFilterModeDialog() {

		NewFilterModeDialog dialog = new NewFilterModeDialog(this,
				mFilterModesAdapter.getItems());
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				Contact selected = ((NewFilterModeDialog) dialog)
						.getSelectedContact();
				onAddNewFilterMode(selected);
			}

		});
		dialog.show();

	}

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

	private void onDeleteFilterMode() {
		
		int actFilterModePosition = mSourcesAdapter.getFilterModePosition();
		if(!mFilterModesAdapter.isDefault(actFilterModePosition) && !mFilterModesAdapter.isAddNew(actFilterModePosition)) {
			
			mFilterModesAdapter.delete(actFilterModePosition);
			mSpinnerFilterMode.setSelection(0);
		}
	}

	
	private void onSourceClicked(AbstractSource source, boolean checked) {
		source.setEnabled(checked);
		mSourcesAdapter.update(source);
	}

}
