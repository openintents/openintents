package org.openintents.historify.ui.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.preferences.Pref.MyAvatar;
import org.openintents.historify.utils.UserIconHelper;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Fragment for displaying and managing user preferences.
 * 
 * @author berke.andras
 */
public class PreferencesFragment extends Fragment {

	private static final int REQUEST_PICK_IMAGE = 0; // anything except 0 won't
														// work due to a bug in
														// fragment support library

	private ImageView imgUserIcon;
	private TextView txtUserIcon;
	private Button btnSet, btnRestore;

	private Spinner spinnerStartupAction, spinnerTimeLineTheme;

	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_preferences, container, false);

		imgUserIcon = (ImageView) layout
				.findViewById(R.id.preferences_imgUserIcon);
		txtUserIcon = (TextView) layout
				.findViewById(R.id.preferences_txtUserIcon);
		btnSet = (Button) layout.findViewById(R.id.preferences_btnSet);
		btnRestore = (Button) layout.findViewById(R.id.preferences_btnRestore);

		spinnerStartupAction = (Spinner) layout
				.findViewById(R.id.preferences_spinnerStartupAction);
		spinnerTimeLineTheme = (Spinner) layout
				.findViewById(R.id.preferences_spinnerTimeLineTheme);

		initView();

		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadSettings();
	}

	private void loadSettings() {

		PreferenceManager pm = PreferenceManager.getInstance(getActivity());

		MyAvatar avatarSetting = ContactIconHelper.loadMyAvatar(getActivity(),
				imgUserIcon);
		txtUserIcon.setText(avatarSetting.toString());

		btnRestore
				.setVisibility(avatarSetting == MyAvatar.defaultIcon ? View.INVISIBLE
						: View.VISIBLE);

		String startupSetting = pm.getStringPreference(Pref.STARTUP_ACTION,
				Pref.DEF_STARTUP_ACTION);
		for (int i = 0; i < spinnerStartupAction.getCount(); i++) {
			if (startupSetting
					.equals(spinnerStartupAction.getItemAtPosition(i)))
				spinnerStartupAction.setSelection(i);
		}

		String timelineThemeSetting = pm.getStringPreference(
				Pref.TIMELINE_THEME, Pref.DEF_TIMELINE_THEME);
		for (int i = 0; i < spinnerTimeLineTheme.getCount(); i++) {
			if (timelineThemeSetting.equals(spinnerTimeLineTheme
					.getItemAtPosition(i)))
				spinnerTimeLineTheme.setSelection(i);
		}
	}

	private void initView() {

		btnSet.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSetIconSelected();
			}
		});

		btnRestore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				restoreIcon();
				loadSettings();
			}
		});

		// startup actions
		String[] startupActions = new String[] {
				getActivity().getString(R.string.preferences_startup_welcome),
				getActivity().getString(
						R.string.preferences_startup_last_contacted),
				getActivity()
						.getString(R.string.preferences_startup_last_shown) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, startupActions);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerStartupAction.setAdapter(adapter);

		spinnerStartupAction
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> adapterView,
							View arg1, int pos, long arg3) {
						persistSpinnerSetting(adapterView, pos,
								Pref.STARTUP_ACTION);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		// timeline themes
		String[] timelineThemes = new String[] {
				getActivity().getString(
						R.string.preferences_timeline_theme_bubbles),
				getActivity().getString(
						R.string.preferences_timeline_theme_rows) };
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, timelineThemes);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerTimeLineTheme.setAdapter(adapter);

		spinnerTimeLineTheme
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> adapterView,
							View arg1, int pos, long arg3) {
						persistSpinnerSetting(adapterView, pos,
								Pref.TIMELINE_THEME);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	protected void onSetIconSelected() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setDataAndType(Media.EXTERNAL_CONTENT_URI, "image/*");

		startActivityForResult(intent, REQUEST_PICK_IMAGE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_PICK_IMAGE
				&& resultCode == Activity.RESULT_OK) {
			try {
				Cursor c = getActivity().getContentResolver().query(
						Thumbnails.EXTERNAL_CONTENT_URI,
						new String[] { Thumbnails.DATA },
						Thumbnails.IMAGE_ID + " = "
								+ ContentUris.parseId(data.getData()), null,
						Thumbnails.WIDTH);

				if (c.moveToFirst()) {
					String file = c.getString(0);
					if (new UserIconHelper().saveIcon(getActivity(), file)) {
						PreferenceManager
								.getInstance(getActivity())
								.setPreference(Pref.MY_AVATAR_ICON,
										Pref.MyAvatar.customizedIcon.toString());
					} else {
						restoreIcon();
					}

				} else {
					Log.w("Preferences", "Unable to get thumbnail");
				}
			} catch (Exception e) {
				e.printStackTrace();
				restoreIcon();
			}

		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	protected void restoreIcon() {
		new UserIconHelper().deleteIcon(getActivity());
		PreferenceManager.getInstance(getActivity()).setPreference(
				Pref.MY_AVATAR_ICON, Pref.DEF_AVATAR_ICON.toString());
	}

	protected void persistSpinnerSetting(AdapterView<?> adapterView, int pos,
			String prefKey) {
		PreferenceManager.getInstance(getActivity()).setPreference(prefKey,
				adapterView.getItemAtPosition(pos).toString());

	}
}
