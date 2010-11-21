/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.openintents.calendarpicker.contract.CalendarPickerConstants;
import org.openintents.calendarpicker.demo.provider.EventContentProvider;
import org.openintents.calendarpicker.demo.provider.SampleEventDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Demo extends Activity implements View.OnClickListener {

	static final String TAG = "Demo";

	private static final int REQUEST_CODE_DATE_SELECTION = 1;
	private static final int REQUEST_CODE_EVENT_SELECTION = 2;

	private static final int DIALOG_CALENDARPICKER_DOWNLOAD = 1;
	private static final int DIALOG_GOOGLE_CALENDAR_SELECTION = 2;
	private static final int DIALOG_DATE_PICKER_OPTIONS = 3;
	private static final int DIALOG_EVENT_PICKER_OPTIONS = 4;
	


	long selected_google_calendar_id = -1;

	public final static int DEFAULT_RANDOM_EVENTS = 15;
	
	// ========================================================================
	public static class EventWrapper {
		public long id, timestamp;
		public String title;
		public float quantity;
	}

	// ========================================================================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		for (int id : new int[] {R.id.button_pick_date, R.id.button_pick_event, R.id.button_visualize_events})
			findViewById(id).setOnClickListener(this);
	}

	// ========================================================================
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong("selected_google_calendar_id", selected_google_calendar_id);
	}

	// ========================================================================
	@Override
	protected void onRestoreInstanceState (Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		selected_google_calendar_id = savedInstanceState.getLong("selected_google_calendar_id");
	}

	// ========================================================================
	void launchDatePickerPlain() {
		Intent i = new Intent(Intent.ACTION_PICK);
		i.setType(CalendarPickerConstants.CalendarDatePicker.CONTENT_TYPE_DATETIME);
		downloadLaunchCheck(i, REQUEST_CODE_DATE_SELECTION);
	}
	
	// ========================================================================
	void launchDatePickerWithEvents() {
		SampleEventDatabase database = new SampleEventDatabase(this);
		database.clearData();
		long calendar_id = database.populateRandomEvents(new GregorianCalendar(), DEFAULT_RANDOM_EVENTS);
		Uri u = EventContentProvider.constructUri(calendar_id);
		Intent i = new Intent(Intent.ACTION_PICK, u);
		downloadLaunchCheck(i, REQUEST_CODE_DATE_SELECTION);
	}

	// ========================================================================
	void launchEventPickerIntentExtras() {
		
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(CalendarPickerConstants.CalendarEventPicker.CONTENT_TYPE_CALENDAR_EVENT);

		List<EventWrapper> generated_events = generateRandomEvents(DEFAULT_RANDOM_EVENTS, new GregorianCalendar());
		int event_count = generated_events.size();
		long[] event_ids = new long[event_count];
		long[] event_times = new long[event_count];
		String[] event_titles = new String[event_count];
		for (int i = 0; i < event_count; i++) {
			EventWrapper event = generated_events.get(i);
			event_ids[i] = event.id;
			event_times[i] = event.timestamp;
			event_titles[i] = event.title;
		}

		intent.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_IDS, event_ids);
		intent.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_TIMESTAMPS, event_times);
		intent.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_TITLES, event_titles);

		downloadLaunchCheck(intent, REQUEST_CODE_EVENT_SELECTION);
	}
	
	// ========================================================================
	void launchEventPickerContentProvider() {
		SampleEventDatabase database = new SampleEventDatabase(this);
		database.clearData();
		long calendar_id = database.populateRandomEvents(new GregorianCalendar(), DEFAULT_RANDOM_EVENTS);
		Uri u = EventContentProvider.constructUri(calendar_id);
		Intent i = new Intent(Intent.ACTION_PICK, u);
		downloadLaunchCheck(i, REQUEST_CODE_EVENT_SELECTION);
	}
	
	// ========================================================================
	void launchEventPickerGoogleCalendar() {
		showDialog(DIALOG_GOOGLE_CALENDAR_SELECTION);
	}

    final int[] COLORMAP_COLORS = new int[] {Color.BLUE, Color.GREEN, Color.RED};
	// ========================================================================
	void launchEventVisualizer() {

		SampleEventDatabase database = new SampleEventDatabase(this);
		database.clearData();
		long calendar_id = database.populateRandomEvents(new GregorianCalendar(), 3*DEFAULT_RANDOM_EVENTS);
		

		Uri u = EventContentProvider.constructUri(calendar_id);
		Intent i = new Intent(Intent.ACTION_VIEW, u);
		
		
		String extra_name = CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES[0];
		i.putExtra(extra_name, SampleEventDatabase.KEY_EVENT_QUANTITY0);
		i.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_FORMATS[0], "%.3f");
		
		i.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_VISUALIZE_QUANTITIES, true);
		i.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_BACKGROUND_COLORMAP_COLORS, COLORMAP_COLORS);
		i.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_SHOW_EVENT_COUNT, false);

		
		downloadLaunchCheck(i, -1);
	}
	
	// ========================================================================
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_pick_date:
		{
			showDialog(DIALOG_DATE_PICKER_OPTIONS);
			break;
		}
		case R.id.button_pick_event:
		{
			showDialog(DIALOG_EVENT_PICKER_OPTIONS);
			break;
		}
		case R.id.button_visualize_events:
		{
			launchEventVisualizer();
			break;
		}
		}
	}

	// ========================================================================
	void downloadLaunchCheck(Intent intent, int request_code) {
		if (CalendarPickerConstants.DownloadInfo.isIntentAvailable(this, intent))
			if (request_code >= 0)
				startActivityForResult(intent, request_code);
			else
				startActivity(intent);
		else
			showDialog(DIALOG_CALENDARPICKER_DOWNLOAD);
	}

	// ========================================================================
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		switch (id) {
		case DIALOG_CALENDARPICKER_DOWNLOAD:
		{
			boolean has_android_market = CalendarPickerConstants.DownloadInfo.isIntentAvailable(this,
					CalendarPickerConstants.DownloadInfo.getMarketDownloadIntent(CalendarPickerConstants.DownloadInfo.PACKAGE_NAME_CALENDAR_PICKER));

			Log.d(TAG, "has_android_market? " + has_android_market);

			dialog.findViewById(android.R.id.button1).setVisibility(
					has_android_market ? View.VISIBLE : View.GONE);
			break;
		}
		default:
			break;
		}
	}

	// ========================================================
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_DATE_PICKER_OPTIONS:
		{
			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.download_calendar_picker)
			.setItems(new String[] {"Plain", "With events"}, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						launchDatePickerPlain();
						break;
					case 1:
						launchDatePickerWithEvents();
						break;
					}
				}
			})
			.create();
		}
		case DIALOG_EVENT_PICKER_OPTIONS:
		{
			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.download_calendar_picker)
			.setItems(new String[] {"Custom ContentProvider", "Intent Extras", "Google Calendar"}, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						launchEventPickerContentProvider();
						break;
					case 1:
						launchEventPickerIntentExtras();
						break;
					case 2:
						launchEventPickerGoogleCalendar();
						break;
					}
				}
			})
			.create();
		}
		case DIALOG_CALENDARPICKER_DOWNLOAD:
		{
			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.download_calendar_picker)
			.setMessage(R.string.calendar_picker_modularization_explanation)
			.setPositiveButton(R.string.download_calendar_picker_market, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(CalendarPickerConstants.DownloadInfo.getMarketDownloadIntent(CalendarPickerConstants.DownloadInfo.PACKAGE_NAME_CALENDAR_PICKER));
				}
			})
			.setNeutralButton(R.string.download_calendar_picker_web, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(new Intent(Intent.ACTION_VIEW, CalendarPickerConstants.DownloadInfo.APK_DOWNLOAD_URI));
				}
			})
			.create();
		}
		case DIALOG_GOOGLE_CALENDAR_SELECTION:
		{
			Uri calendars = new Uri.Builder()
			.scheme(ContentResolver.SCHEME_CONTENT)
			.authority( CalendarPickerConstants.ANDROID_CALENDAR_AUTHORITY_2_0)
			.appendPath(CalendarPickerConstants.ANDROID_CALENDAR_PROVIDER_PATH_CALENDARS).build();

			final Cursor cursor = managedQuery(calendars, null, null, null, null);

			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Choose calendar")
			.setSingleChoiceItems(cursor, -1, "name", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cursor.moveToPosition(which);
					selected_google_calendar_id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
				}
			})
			.setPositiveButton("Select", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Selected calendar ID: " + selected_google_calendar_id);

					Uri uri = new Uri.Builder()
						.scheme(ContentResolver.SCHEME_CONTENT)
						.authority( CalendarPickerConstants.ANDROID_CALENDAR_AUTHORITY_2_0)
						.appendPath(CalendarPickerConstants.ANDROID_CALENDAR_PROVIDER_PATH_EVENTS).build();

					Intent intent = new Intent(Intent.ACTION_PICK, uri);
					intent.putExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_CALENDAR_ID, selected_google_calendar_id);
					downloadLaunchCheck(intent, REQUEST_CODE_EVENT_SELECTION);
				}
			})
			.create();
		}
		}

		return null;
	}

	// ========================================================================
	/** Input: an event count and a calendar to specify the year and month */
	public static List<EventWrapper> generateRandomEvents(int event_count, Calendar cal) {

		Calendar calendar = (Calendar) cal.clone();
		Random r = new Random();
		
		List<EventWrapper> events = new ArrayList<EventWrapper>();
		for (int event_id = 0; event_id < event_count; event_id++) {
			EventWrapper event = new EventWrapper();
			calendar.roll(Calendar.DATE, r.nextInt(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
			calendar.roll(Calendar.HOUR_OF_DAY, r.nextInt(calendar.getActualMaximum(Calendar.HOUR_OF_DAY)));
			calendar.roll(Calendar.MINUTE, r.nextInt(calendar.getActualMaximum(Calendar.MINUTE)));
			event.timestamp = calendar.getTimeInMillis();
			event.id = event_id;
			event.title = "Random Event 0x" + Integer.toHexString(0xFFFF & r.nextInt()).toUpperCase();
			event.quantity = r.nextFloat();
			events.add(event);
		}

		return events;
	}

	// ========================================================================
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {
			Log.i(TAG, "==> result " + resultCode + " from subactivity!  Ignoring...");
//            Toast t = Toast.makeText(this, "Action cancelled!", Toast.LENGTH_SHORT);
//            t.show();
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_DATE_SELECTION:
		{
			String iso_date = data.getStringExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME);
			long epoch_date = data.getLongExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, 0);

			((TextView) findViewById(R.id.date_picker_result)).setText( iso_date + "; " + epoch_date );
			break;
		}
		case REQUEST_CODE_EVENT_SELECTION:
		{
			long id = data.getLongExtra(BaseColumns._ID, -1);
			Toast.makeText(this, "Result: " + id, Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.event_picker_result)).setText( "" + id );
			break;
		}
		}
	}
}