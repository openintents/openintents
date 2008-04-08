package org.openintents.applications.newsreader;

import org.openintents.provider.News;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

public class AFeedSettings extends Activity {
	
	
	public static final int STATE_CREATE=1001;
	public static final int STATE_EDIT=1002;
	
	public static final int STATE_SAVE=201;
	public static final int STATE_CANCEL=202;

	private Uri cUri=null;

	private static final String _TAG="AFeedSettings";
	
	private int mState;
	private Cursor mCursor;
	
	private String	feedName;
	private String	feedURL;
	private long	feedID=0;
	private String	feedType;
	private String	updateCycle;


	private String[] uCycles=new String[] {"10","30","60","90","120","180"};

	private static String[] RSS_PROJECTION=new String[]{
		News.RSSFeeds._ID,
		News.RSSFeeds._COUNT,
		News.RSSFeeds.CHANNEL_NAME,
		News.RSSFeeds.CHANNEL_LINK,
		News.RSSFeeds.UPDATE_CYCLE
	};
	

	private static String[] ATM_PROJECTION=new String[]{
		News.AtomFeeds._ID,
		News.AtomFeeds._COUNT,
		News.AtomFeeds.FEED_TITLE,
		News.AtomFeeds.FEED_LINK_SELF,
		News.AtomFeeds.UPDATE_CYCLE
	};

	private EditText mChannelLink =null;
	private EditText mChannelName=null;
	private Spinner mType=null;
	private Spinner mUpdateCycle=null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.afeedsettings);
        
        //use only one instance to save memory
        Button bInstance=(Button)findViewById(R.id.saveFeedSettings);
        bInstance.setOnClickListener(mSaveFeedSettings);
        bInstance=(Button)findViewById(R.id.CancelFeedSettings);
        bInstance.setOnClickListener(mCancelFeedSettings);
        
		mChannelLink=(EditText)findViewById(R.id.feedurl);
        mChannelName=(EditText)findViewById(R.id.feedname);

		mType=(Spinner)findViewById(R.id.afeedsettings_feedtype);
		mUpdateCycle=(Spinner)findViewById(R.id.afeedsettings_updatecycle);

		ArrayAdapter ad= new ArrayAdapter(
						this,
						android.R.layout.simple_spinner_item,
						new String[] {News.FEED_TYPE_RSS,News.FEED_TYPE_ATOM}
						);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mType.setAdapter(ad);
		mType.setSelection(0);

		ad= new ArrayAdapter(
						this,
						android.R.layout.simple_spinner_item,
						uCycles
						//new String[] {"10","30","60","90","120","180"}
						);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mUpdateCycle.setAdapter(ad);
		mUpdateCycle.setSelection(1);


        if (getIntent().getAction().equals(Intent.INSERT_ACTION)){
        	mState=STATE_CREATE;
        	//mChannelLink.setText("EHLO CREATOR!");
        }else if (getIntent().getAction().equals(Intent.EDIT_ACTION)){
        	mState=STATE_EDIT;
			Bundle b=getIntent().getExtras();
			feedType=b.getString(News.FEED_TYPE);
        	cUri=Uri.parse(b.getString("URI"));
			Log.d(_TAG,"State Edit:: cUri>>"+cUri+"<< type>>"+feedType+"<<");
			News.mContentResolver=getContentResolver();
			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				mType.setSelection(0);
				mCursor=managedQuery(cUri,RSS_PROJECTION,null,null);
				Log.d(_TAG,"State Edit::mCursor.count()>>"+mCursor.count()+"<<");
				if (mCursor.count()>0)
				{
					mCursor.first();
					mChannelName.setText(mCursor.getString(mCursor.getColumnIndex(News.RSSFeeds.CHANNEL_NAME)));
					mChannelLink.setText(mCursor.getString(mCursor.getColumnIndex(News.RSSFeeds.CHANNEL_LINK)));
					preselectSpinner(mCursor.getString(mCursor.getColumnIndex(News.RSSFeeds.UPDATE_CYCLE)));
					
				}else {
					Log.e(_TAG,"Cursor was empty");
				}
			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{
				mType.setSelection(1);
				mCursor=managedQuery(cUri,ATM_PROJECTION,null,null);
				Log.d(_TAG,"State Edit::mCursor.count()>>"+mCursor.count()+"<<");
				if (mCursor.count()>0)
				{
					mCursor.first();
					mChannelName.setText(mCursor.getString(mCursor.getColumnIndex(News.AtomFeeds.FEED_TITLE)));
					mChannelLink.setText(mCursor.getString(mCursor.getColumnIndex(News.AtomFeeds.FEED_LINK_SELF)));
					preselectSpinner(mCursor.getString(mCursor.getColumnIndex(News.AtomFeeds.UPDATE_CYCLE)));
					//mUpdateCycle.setSelectedItem(mCursor.getString(mCursor.getColumnIndex(News.AtomFeeds.UPDATE_CYCLE)));
				}else{
					Log.e(_TAG,"Cursor was empty");
				}
			}
			
        }else{
        	//unknown action, exit
        	
        }
        Log.d("AFEEDSETTINGS","cUri>>"+cUri+"<<");
       
        
    }
    
    
    //Save new Settings (overriding old) to datacontainer.
    private OnClickListener mSaveFeedSettings=new OnClickListener(){

		public void onClick(View arg0) {

			AFeedSettings.this.feedType=(String)AFeedSettings.this.mType.getSelectedItem();
			if (AFeedSettings.this.mState==AFeedSettings.STATE_CREATE)
			{

				AFeedSettings.this.createDataSet();
				AFeedSettings.this.setResult(Activity.RESULT_OK);
				AFeedSettings.this.finish();				

			}else if (AFeedSettings.this.mState==AFeedSettings.STATE_EDIT)
			{

				AFeedSettings.this.saveDataSet();
				AFeedSettings.this.mCursor.close();
				AFeedSettings.this.setResult(Activity.RESULT_OK);
				AFeedSettings.this.finish();				

			}

			
		}
		
		
    };
	
    //Cancel Method, Just Quit Activity
    private OnClickListener mCancelFeedSettings=new OnClickListener(){

		public void onClick(View arg0) {
			
			AFeedSettings.this.setResult(Activity.RESULT_CANCELED);
			AFeedSettings.this.finish();
		}
		
		
    };
	
    public void onPause(){
    	super.onPause();
    	

    	
    	
    }


	private void preselectSpinner(String data){
//mUpdateCycle.setSelectedItem(mCursor.getString(mCursor.getColumnIndex(News.RSSFeeds.UPDATE_CYCLE)));	
		if (data==null || data.equals(""))
		{
			return;
		}

		for (int i=0;i<uCycles.length ;i++ )
		{
			if (data.equals(uCycles[i]))
			{
				mUpdateCycle.setSelection(i);
				return;
			}
		}
	}
    
	private void saveDataSet(){
		String myLink=mChannelLink.getText().toString();
		Log.d(_TAG,"myLink was>>"+myLink);
		if (myLink!=null && !myLink.equals("") && !myLink.startsWith("http://"))
		{
		
			myLink="http://"+myLink;
			Log.d(_TAG,"myLink now>>"+myLink);
		}
		if (mCursor!=null&&feedType.equals(News.FEED_TYPE_RSS))
		{
			//TODO: delete empty row if canceld
			
			mCursor.updateString(
					mCursor.getColumnIndex(News.RSSFeeds.CHANNEL_NAME),
					mChannelName.getText().toString()
					);
			
			mCursor.updateString(
					mCursor.getColumnIndex(News.RSSFeeds.CHANNEL_LINK),
					myLink
					);	
			mCursor.updateString(
					mCursor.getColumnIndex(News.RSSFeeds.UPDATE_CYCLE),
					((String)mUpdateCycle.getSelectedItem())
					);
			mCursor.commitUpdates();
		}else if (mCursor!=null&&feedType.equals(News.FEED_TYPE_ATOM))
		{
			mCursor.updateString(
					mCursor.getColumnIndex(News.AtomFeeds.FEED_TITLE),
					mChannelName.getText().toString()
					);
			
			mCursor.updateString(
					mCursor.getColumnIndex(News.AtomFeeds.FEED_LINK_SELF),
					myLink
					);
			mCursor.updateString(
					mCursor.getColumnIndex(News.AtomFeeds.UPDATE_CYCLE),
					((String)mUpdateCycle.getSelectedItem())
					);			
			mCursor.commitUpdates();
		}else{
			Log.e(this._TAG,"Error: Cursor was Null. Nothin to Save");
		}
	}



	private void createDataSet(){
		//TODO: decide type of feed before creating new record.
		ContentValues v=new ContentValues();
		String myLink=mChannelLink.getText().toString();
		Log.d(_TAG,"myLink was>>"+myLink);
		if (myLink!=null && !myLink.equals("") && !myLink.startsWith("http://"))
		{
		
			myLink="http://"+myLink;
			Log.d(_TAG,"myLink now>>"+myLink);
		}
	

		if (feedType.equals(News.FEED_TYPE_RSS))
		{

			v.put(News.RSSFeeds.CHANNEL_NAME,mChannelName.getText().toString());
			v.put(News.RSSFeeds.CHANNEL_LINK,myLink);
			v.put(News.RSSFeeds.UPDATE_CYCLE,((String)mUpdateCycle.getSelectedItem()));


			cUri = News.insert(News.RSSFeeds.CONTENT_URI,v);
			mCursor=managedQuery(cUri,RSS_PROJECTION,null,null);

		}else if (feedType.equals(News.FEED_TYPE_ATOM))
		{

			v.put(News.AtomFeeds.FEED_TITLE,mChannelName.getText().toString());
			v.put(News.AtomFeeds.FEED_LINK_SELF,myLink);
			v.put(News.AtomFeeds.UPDATE_CYCLE,((String)mUpdateCycle.getSelectedItem()));


			cUri = News.insert(News.AtomFeeds.CONTENT_URI,v);
			mCursor=managedQuery(cUri,ATM_PROJECTION,null,null);

		}else{
			//should never happen
			Log.e(_TAG,"Unrecognized Feed Type. How did tis happen?");
		}
		Log.d(_TAG,"returned uri >>"+cUri+"<<");

	}


	private void checkFeed(){
		//TODO: instaciate thread && update feed first time.
		//TODO: download feed icon.

	}

    
}/*eoc*/
