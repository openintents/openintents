package org.openintents.distribution;

import org.openintents.colorpicker.R;

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
            R.string.title_app_flashlight, // OI Flashlight
    };
    
    private String[] mPackageNames = {
    		"org.openintents.flashlight", // OI Flashlight
    };
    
    private int[] mMinVersionCodes = {
    		10007, // OI Flashlight
    };
    
    private String[] mMinVersionName = {
    		"1.0.6", // OI Flashlight
    };
    
    private int[] mInfoText = {
    		R.string.info_app_flashlight, // OI Flashlight
    };

    public static final String MARKET_PACKAGE_DETAILS_PREFIX = "market://details?id=";
    private String[] mMarketUris = {
    		MARKET_PACKAGE_DETAILS_PREFIX + mPackageNames[0], // OI Flashlight
    };

    public static final String DEVELOPER_WEBSITE_URL_PREFIX = "http://www.openintents.org/en/";
    private String[] mDeveloperUris = {
    		DEVELOPER_WEBSITE_URL_PREFIX + "flashlight", // OI Flashlight
    };

    private String[] mIntentAction = {
    		"org.openintents.action.START_FLASHLIGHT", // OI Flashlight
    };

    private String[] mIntentData = {
    		null, // OI Flashlight
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
		if (isPackageAvailable(this, mPackageNames[pos], mMinVersionCodes[pos])) {
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

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			
		}
	}

	private AlertDialog buildGetFromMarketDialog(int pos) {
		String info_not_available = getString(R.string.info_not_available, mApplicationStrings[pos], mMinVersionName[pos]);
		String info_get = getString(R.string.info_get, mApplicationStrings[pos]);
		return new GetFromMarketDialog(this, 
				info_not_available,
				info_get,
				mMarketUris[pos],
				mDeveloperUris[pos]);
	
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
