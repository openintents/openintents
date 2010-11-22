package org.openintents.distribution;

import org.openintents.calendarpicker.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class InfoActivity extends ListActivity implements OnItemClickListener {

    private int[] mApplications = {
            R.string.title_app_kanji_tutor, // Kanji Tutor
    		R.string.title_app_dev_rev, // Developer Revenue Analysis
    		R.string.title_app_taxo_tracker // Taxo Tracker
    };
    
    private int[] mPackageNames = {
    		R.string.package_app_kanji_tutor, // Kanji Tutor
    		R.string.package_app_dev_rev, // Developer Revenue Analysis
    		R.string.package_app_taxo_tracker, // Taxo Tracker
    };
    
    private int[] mMinVersionCodes = {
    		15, // Kanji Tutor
    		13, // Developer Revenue Analysis
    		1, // Taxo Tracker
    };
    
    private String[] mMinVersionName = {
    		"2.3", // Kanji Tutor
    		"1.4", // Developer Revenue Analysis
    		"1.0", // Taxo Tracker
    };
    
    private int[] mInfoText = {
    		R.string.info_app_kanji_tutor, // Kanji Tutor
    		R.string.info_app_dev_rev, // Developer Revenue Analysis
    		R.string.info_app_taxo_tracker, // Taxo Tracker
    };

    public static final String MARKET_PACKAGE_DETAILS_PREFIX = "market://details?id=";

    public static final String DEVELOPER_WEBSITE_URL_PREFIX = "http://www.openintents.org/en/";
    private int[] mDeveloperUris = {
    		R.string.website_app_kanji_tutor, // Kanji Tutor
    		R.string.website_app_dev_rev, // Developer Revenue Analysis
    		R.string.website_app_taxo_tracker, // Taxo Tracker
    };

    private String[] mIntentAction = {
    		Intent.ACTION_MAIN, // Kanji Tutor
    		Intent.ACTION_MAIN, // Developer Revenue Analysis
    		Intent.ACTION_MAIN, // Taxo Tracker
    };

    private String[] mIntentData = {
    		null, // Kanji Tutor
    		null, // Developer Revenue Analysis
    		null, // Taxo Tracker
    };
    
    /////////////////////////////////////////////////////////////////////////////
    
	public static final int DIALOG_INFO = 0;
	public static final int DIALOG_GET_FROM_MARKET = 100;
	
    private String[] mApplicationStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.infoactivity);

        mApplicationStrings = new String[mApplications.length];
        for (int i = 0; i < mApplications.length; i++) {
        	mApplicationStrings[i] = getString(mApplications[i]);
        }
        setListAdapter(new FontArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mApplicationStrings));
        ListView listview = getListView();
        
        listview.setOnItemClickListener(this);
        
        /*
        TypedArray a = obtainStyledAttributes(mTheme, R.styleable.ShoppingList);
		String typefaceName = a.getString(R.styleable.ShoppingList_textTypeface);
	    mTextSizeMedium = a.getDimensionPixelOffset(R.styleable.ShoppingList_textSizeMedium, 23);
	    mTextSizeLarge = a.getDimensionPixelOffset(R.styleable.ShoppingList_textSizeLarge, 28);
	    mTextColor = a.getColor(R.styleable.ShoppingList_textColor, Color.BLACK);
	    Drawable background = a.getDrawable(R.styleable.ShoppingList_background);

	    
	    View v = findViewById(R.id.background);
	    v.setBackgroundDrawable(background);
	    
		mTypeface = Typeface.createFromAsset(getResources().getAssets(), typefaceName);
        
        TextView tv = (TextView) findViewById(R.id.text);
        tv.setTypeface(mTypeface);
        tv.setTextSize(mTextSizeMedium);
        tv.setTextColor(mTextColor);
	    */
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		String package_name = getResources().getString(mPackageNames[pos]);
		if (isPackageAvailable(this, package_name, mMinVersionCodes[pos])) {
			showDialog(DIALOG_INFO + pos);
		} else {
			showDialog(DIALOG_GET_FROM_MARKET + pos);
		}
	}
	
	private class FontArrayAdapter<T> extends ArrayAdapter<T> {

		public FontArrayAdapter(Context context, int textViewResourceId,
				T[] objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv = (TextView) super.getView(position, convertView, parent);
			/*
			tv.setTypeface(mTypeface);
			tv.setTextSize(mTextSizeLarge);
	        tv.setTextColor(mTextColor);
			*/
			return tv;
		}
	}


	/**
	 * Indicates whether a specific package with minimum version code is available.
	 */
	public static boolean isPackageAvailable(final Context context, final String packageName,
			final int minVersionCode) {
		boolean result = false;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					packageName, 0);
			if (pi.versionCode >= minVersionCode) {
				result = true;
			}
        } catch (PackageManager.NameNotFoundException e) {
        	
        }
	    return result;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		
		if (id < DIALOG_GET_FROM_MARKET) {
			dialog = buildInfoDialog(id - DIALOG_INFO);
		} else {
			dialog = buildGetFromMarketDialog(id - DIALOG_GET_FROM_MARKET);
		}
		if (dialog == null) {
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	private AlertDialog buildInfoDialog(final int pos) {
		String infotext = getString(mInfoText[pos]);
		String infolaunch = getString(R.string.info_launch, mApplicationStrings[pos]);
		return new AlertDialog.Builder(this)
			//.setIcon(RD.drawable.icon)
			.setTitle(R.string.app_name)
			.setMessage(infotext)
			.setPositiveButton(infolaunch,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// click Ok
							launchApplication(pos);
						}
					})
			.create();
	}

	void launchApplication(int pos) {
		Intent intent = new Intent();
		intent.setAction(mIntentAction[pos]);
		if (mIntentData[pos] != null)
			intent.setData(Uri.parse(mIntentData[pos]));
		else if (mPackageNames[pos] != 0) {
			String package_name = getResources().getString(mPackageNames[pos]);
			intent.setPackage(package_name);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
		}
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private AlertDialog buildGetFromMarketDialog(int pos) {
		String info_not_available = getString(R.string.info_not_available, mApplicationStrings[pos], mMinVersionName[pos]);
		String info_get = getString(R.string.info_get, mApplicationStrings[pos]);

		String package_name = getResources().getString(mPackageNames[pos]);
		String package_uri = MARKET_PACKAGE_DETAILS_PREFIX + package_name;
		
		String developer_url = getResources().getString(mDeveloperUris[pos]);
		return new GetFromMarketDialog(this, 
				info_not_available,
				info_get,
				package_uri,
				developer_url);
	
	}
	
	private class GetFromMarketDialog extends AlertDialog implements OnClickListener {
		private static final String TAG = "GetFromMarketDialog";

	    Context mContext;
	    String mMarketUri;
	    String mDeveloperUri;
	    
	    public GetFromMarketDialog(Context context, String message, String buttontext, String market_uri, String developer_uri) {
	        super(context);
	        mContext = context;
	        mMarketUri = market_uri;
	        mDeveloperUri = developer_uri;

	        //setTitle(context.getText(R.string.menu_edit_tags));
	        setMessage(message);
	    	setButton(buttontext, this);
	        
	    }

		public void onClick(DialogInterface dialog, int which) {
	    	if (which == BUTTON1) {
	    		Uri uri = Uri.parse(mMarketUri);
	    		
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				
				uri= Uri.parse(mDeveloperUri);
				Intent intent2 = new Intent(Intent.ACTION_VIEW);
				intent2.setData(uri);
				startSaveActivity(mContext, intent, intent2);
	    	}
			
		}
	}
	
	/**
	 * Start an activity but prompt a toast if activity is not found
	 * (instead of crashing).
	 * 
	 * @param context
	 * @param intent
	 * @param intent2 Alternative intent to call, if the first is not reachable
	 */
	public static void startSaveActivity(Context context, Intent intent, Intent intent2) {
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			//Log.e(TAG, "Error starting activity.", e);
			try {
				context.startActivity(intent2);
			} catch (ActivityNotFoundException e2) {
				//Log.e(TAG, "Error starting second activity.", e2);
			}
		}
	}
}
