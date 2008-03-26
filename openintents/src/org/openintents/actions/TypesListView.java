package org.openintents.actions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.openintents.OpenIntents;
import org.openintents.R;
import org.openintents.provider.Intents;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ArrayListCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.LinearLayout.LayoutParams;

public class TypesListView extends ListActivity {

	private static final String LOG_TAG = "intentsListView";

	protected static final int REQUEST_PICK = 1;
	private static final int REQUEST_VIEW_URI = 2;

	/** extra bundle name for type information of picked intent */
	public static final String EXTRA_TYPE = "type";
	/** extra bundle name for action name of picked intent */
	public static final String EXTRA_ACTION = "action";
	/**
	 * extra bundle name for uri of picked intent, used alternatively to
	 * EXTRA_TYPE
	 */
	public static final String EXTRA_URI = "uri";

	private static final int MENU_VIEW_ALL = 0;

	LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);

	final int flags = PackageManager.GET_RESOLVED_FILTER;

	protected Dialog mDialog = new Dialog(this);

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	// Create runnable for posting
	final Runnable mUpdateList = new Runnable() {
		public void run() {
			updateList();
		}

	};

	private ArrayListCursor mTypesCursor = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.intents_types_view);

		Thread t = new Thread() {
			@Override
			public void run() {
				createTypesList();
				mHandler.post(mUpdateList);
			}

		};
		t.start();
	}

	protected void updateList() {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				TypesListView.this, R.layout.intents_types, mTypesCursor,
				new String[] { "type" }, new int[] { R.id.intents_types_name });
		setListAdapter(adapter);

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView adapterview, View view,
							int i, long l) {
						getListView().setSelection(i);
						Object item = getListAdapter().getItem(i);
						Intent intent = new Intent(TypesListView.this,
								PickStringView.class);
						intent.putExtra(PickStringView.EXTRA_LIST,
								(String) ((ArrayListCursor) item).getString(1));
						startSubActivity(intent, REQUEST_PICK);

					}

				});

	}

	private void createTypesList() {
		if (mTypesCursor == null) {
			HashMap<String, ArrayList<IntentFilter>> map = createIntentsMap();

			ArrayList<ArrayList> list = new ArrayList<ArrayList>();

			for (Entry<String, ArrayList<IntentFilter>> e : map.entrySet()) {
				StringBuffer sb = new StringBuffer();
				HashSet<String> actions = new HashSet<String>();
				for (IntentFilter i : e.getValue()) {
					if (i.actionsIterator() != null) {
						for (Iterator<String> i2 = i.actionsIterator(); i2
								.hasNext();) {
							String action = i2.next();
							if (!actions.contains(action)) {
								sb.append(action + " ");
								actions.add(action);
							}
						}
					} else {
						Log.w(LOG_TAG, " no actions for " + i);
					}
				}

				ArrayList<Object> row = new ArrayList<Object>();
				row.add(e.getKey());
				row.add(sb);
				list.add(row);
			}

			mTypesCursor = new ArrayListCursor(
					new String[] { "type", "actions" }, list);
		}

	}

	private HashMap<String, ArrayList<IntentFilter>> createIntentsMap() {

		// inspect actions defined at Intent.class
		HashMap<String, ArrayList<IntentFilter>> map = new HashMap<String, ArrayList<IntentFilter>>();
		for (Field f : Intent.class.getFields()) {

			if (f.getName().endsWith("ACTION")) {
				String action = null;
				try {
					action = (String) f.get(Intent.class);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				if (action != null) {
					addIntentsToMap(map, action);
				}
			}
		}

		// inspect OpenIntents.TAG_ACTION
		addIntentsToMap(map, OpenIntents.TAG_ACTION);

		// return result
		return map;
	}

	private void addIntentsToMap(HashMap<String, ArrayList<IntentFilter>> map,
			String action) {

		// prepare intent
		Intent intent = new Intent();
		intent.setDataAndType(null, "*/*");
		intent.setAction(action);

		List<ResolveInfo> activities = this.getPackageManager()
				.queryIntentActivities(intent, flags);
		Log.i(LOG_TAG, intent + ":" + activities.size());

		resolveListToMap(intent, activities, map);
	}

	private void resolveListToMap(Intent intent, List<ResolveInfo> activities,
			HashMap<String, ArrayList<IntentFilter>> map) {

		for (ResolveInfo ri : activities) {
			StringBuffer fi = new StringBuffer();
			if (ri.filter != null) {
				Iterator<String> i = ri.filter.typesIterator();
				if (i != null) {
					while (i.hasNext()) {
						String type = i.next();
						fi.append(type);
						ArrayList<IntentFilter> set = map.get(type);
						if (set == null) {
							set = new ArrayList<IntentFilter>();
							map.put(type, set);
						}
						set.add(ri.filter);
						Log.i("test", type + ": " + intent);

					}
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, String s,
			Bundle bundle) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		String type = ((ArrayListCursor) getListAdapter().getItem(
				getSelectedItemPosition())).getString(0);

		switch (requestCode) {
		case REQUEST_PICK:
			if (Intent.VIEW_ACTION.equals(s)) {
				Intent intent = new Intent();
				intent.setAction(Intent.PICK_ACTION);
				intent.setDataAndType(null, type);
				if (getPackageManager().resolveActivity(intent, 0) == null) {
					intent.setAction(Intent.GET_CONTENT_ACTION);
				}
				if (getPackageManager().resolveActivity(intent, 0) != null) {
					startSubActivity(intent, REQUEST_VIEW_URI);
				} else {
					Bundle b = new Bundle();
					b.putString(Intents.EXTRA_ACTION, s);
					b.putString(Intents.EXTRA_TYPE, type);					
					finishOrAction(b);
				}
			} else {
				Bundle b = new Bundle();
				b.putString(Intents.EXTRA_ACTION, s);
				b.putString(Intents.EXTRA_TYPE, type);
				finishOrAction(b);
			}
			break;
		case REQUEST_VIEW_URI:
			Bundle b = new Bundle();
			b.putString(Intents.EXTRA_ACTION, Intent.VIEW_ACTION);
			b.putString(Intents.EXTRA_URI, s);
			finishOrAction(b);
		}
	}

	private void finishOrAction(Bundle b) {
		if (getCallingActivity() != null){
			setResult(Activity.RESULT_OK, null, b);
			finish();
		} else {			
			Intent intent = new Intent();
			intent.setAction(b.getString(Intents.EXTRA_ACTION));
			Uri uri = null;
			String uriString = b.getString(Intents.EXTRA_URI);
			if (uriString != null){
				uri = Uri.parse(uriString);
			}
			intent.setDataAndType(uri, b.getString(Intents.EXTRA_TYPE));
			startActivity(intent );
		}
		
	}

	@Override
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onOptionsItemSelected(item);
		switch (item.getId()) {
		case MENU_VIEW_ALL:
			Intent intent = new Intent(this, IntentsListView.class);
			startActivity(intent);
			break;
		}
		return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_VIEW_ALL, R.string.intents_list);

		return true;
	}

}
