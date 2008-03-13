package org.openintents.applications.newsreader;



import org.openintents.provider.News;
import org.openintents.news.*;
import org.openintents.news.services.*;
import org.openintents.news.views.*;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Newsreader extends Activity {

	//Define Menu Constants
	public static final int MENU_CREATE=1001;
	
	public static final int MENU_DELETE=1002;
	public static final int MENU_SERVICESETTINGS=1003;
	public static final int MENU_EDIT=1004;
	
	public static final String _TAG="Newsreader";
	
	private Cursor mRSSCursor;
	private Cursor mAtomCursor;
	
	private ListView mRSSListView;
	private ListView mAtomListView;
	private ListView mChannelsView;
	
	private LinearLayout.LayoutParams mLayoutParams;
	
	private static final String[] RSS_PROJECTION= new String[] {
		News.RSSFeeds._ID,
		News.RSSFeeds._COUNT,
		News.RSSFeeds.CHANNEL_NAME
		
	};
	

	private static final String[] RSS_CONTENTS_PROJECTION= new String[] {
		News.RSSFeedContents.CHANNEL_ID,
		News.RSSFeedContents._COUNT		
		
	};

	private static final String[] ATOM_PROJECTION=new String[]{
		News.AtomFeeds._ID,
		News.AtomFeeds._COUNT,
		News.AtomFeeds.FEED_TITLE
	};
	
	@Override
	public void onCreate(Bundle icicle){
		
		this.setTitle("Openintents Newsreader");
		try{ 
		
		super.onCreate(icicle);
		Log.v(_TAG,Integer.toString(R.layout.newsreader));

		try {
			//get all feeds.
			News.mContentResolver=getContentResolver();
			mRSSCursor=managedQuery(News.RSSFeeds.CONTENT_URI,RSS_PROJECTION,null,null);
			startManagingCursor(mRSSCursor);
			Log.d(_TAG,"mRSSCursor"+mRSSCursor);
			Log.d(_TAG,"# Items in mRSSCursor>>"+mRSSCursor.count()+"<<");
		}catch(Exception x){Log.e(_TAG,"RSSCURSOR Error, e>>"+x.getMessage()+"<<");}
		
		/*
		 //TODO: activate and test atom support.
		try {
			//get all feeds.
			mAtomCursor=managedQuery(News.AtomFeeds.CONTENT_URI,ATOM_PROJECTION,null,null);
			startManagingCursor(mAtomCursor);
			Log.d(_TAG,"mAtomCursor"+mAtomCursor);
			Log.d(_TAG,"# Items in mAtomCursor>>"+mAtomCursor.count()+"<<");
		}catch(Exception x){Log.e(_TAG,"AtomCURSOR Error, e>>"+x.getMessage()+"<<");}
		*/

		setContentView(R.layout.newsreader);

		mChannelsView=(ListView) findViewById(R.id.newsreader_listitems);
		mChannelsView.setOnItemClickListener(
			new OnItemClickListener(){
			
				 public void onItemClick(AdapterView parent, View v, int position, long id){
					 parent.setSelection(position);
					Log.d("mCHANNELSVIEW",":CLICK!");
					Newsreader.this.openChannel();
				}
			
			}
		);
		updateChannelsList();

		
        /*
         //TODO: activate and Test Atom Support
        mAtomListView=(ListView)findViewById(R.id.newsreader_atomlistitems);
        ListAdapter atomListAdapter= new SimpleCursorAdapter(
        	this,
        	R.layout.newsreader_feedrow,
        	mAtomCursor,
        	new String[] {News.AtomFeeds.FEED_TITLE},
        	new int[] {R.id.newsreader_feedrow_feedname}
        
        );
        mAtomListView.setAdapter(atomListAdapter);
        mAtomListView.debug();
        Log.d(_TAG,"count from (atom) obj>>"+mAtomListView.getAdapter().getCount());
        
        */
		//createMenuEntrys();
		 
		 
		}
		catch(Exception e){Log.e(_TAG,"onCreate::ERROR>>"+e.getMessage()+"<<");}
	}
	

	/*
	
	private void calculateListSize(){
		Log.d(_TAG,"calculateListSize: entering");
	//	int rssCount=mRSSListView.getAdapter().getCount();
		int rssCount=4;
		if (rssCount==0){rssCount=1;}
		/*
		int atomCount=mAtomListView.getAdapter().getCount();
		if (atomCount==0){atomCount=1;}
		*/
		/*
		int iHeight=12;
		
		mLayoutParams= new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT); 
		mLayoutParams.width=130;
		mLayoutParams.height=iHeight*rssCount;
		
		mRSSListView.setLayoutParams(mLayoutParams);
		
		
		
		/*
		mLayoutParams.height=iHeight*atomCount;
		mAtomListView.setLayoutParams(mLayoutParams);
		*/
		/*
		Log.d(_TAG,"calculateListSize: leaving");

		
	}
	
	*/

	@Override
	public boolean onOptionsItemSelected(Item item){
		Log.v(_TAG,"onOptionsItemSelected: item.id>>"+item.getId()+"<<");
		int iID=item.getId();
		if (iID==MENU_CREATE)
		{
			menuCreate();
		}else if (iID==MENU_EDIT)
		{
			menuEdit();
		}else if (iID==MENU_DELETE)
		{
			menuDelete();
		}else if (iID==MENU_SERVICESETTINGS)
		{			
			menuServiceSettings();
		}

		return super.onOptionsItemSelected(item);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		boolean result= super.onCreateOptionsMenu(menu);
		
		menu.add(0,MENU_CREATE,"Add Feed",R.drawable.addfeed);
		menu.add(0,MENU_EDIT,"Edit Feed",R.drawable.feedsettings);
		menu.add(0,MENU_DELETE,"Delete Feed",R.drawable.delfeed);
		menu.add(0,MENU_SERVICESETTINGS, "ServiceSettings",R.drawable.settings001a);
		
		return result;
		
	}

	
	
	private void menuCreate() {
		Log.v(_TAG,"menuCreate:entering");		
		
		Intent i= new Intent(this,AFeedSettings.class);
		//startSubActivity(i,AFeedSettings.ACTIVITY_CREATE);
		i.setAction(Intent.INSERT_ACTION);
		i.setData(getIntent().getData());
		startActivity(i);
	}

	private void menuEdit() {
		Object o=mChannelsView.getSelectedItem();
		int res;
		if (o!=null)
		{
		
			Log.d(_TAG,"SelectedItem>>"+o.toString()+"<<");
			HashMap h=(HashMap)o;		
			String feedType=(String)h.get(News.FEED_TYPE);
			long _id=Long.parseLong(((String)h.get(News._ID)));
			Uri u=null;
		
			Intent i= new Intent(this,AFeedSettings.class);
			//startSubActivity(i,AFeedSettings.ACTIVITY_EDIT);
			i.setAction(Intent.EDIT_ACTION);
			i.setData(getIntent().getData());
			Bundle b=new Bundle();
			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				 u=ContentUris.withAppendedId(News.RSSFeeds.CONTENT_URI,_id);								
			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{
				u=ContentUris.withAppendedId(News.AtomFeeds.CONTENT_URI,_id);
			}
			b.putString("URI",u.toString());
			b.putString(News.FEED_TYPE,feedType);
			i.putExtras(b);
			startActivity(i);		
		}
	}
	

	private void menuDelete() {
		// TODO Auto-generated method stub
		Object o=mChannelsView.getSelectedItem();
		int res;
		/*
		long i=mChannelsView.getSelectedItemId();
		int n=mChannelsView.getSelectedItemPosition();
		
		Log.d(_TAG,"SelectedItemId>>"+i+"<<");
		Log.d(_TAG,"SelectedItemPOS>>"+n+"<<");
		*/
		if (o!=null)
		{
		
			Log.d(_TAG,"SelectedItem>>"+o.toString()+"<<");
			HashMap h=(HashMap)o;
			String feedType=(String)h.get(News.FEED_TYPE);
			long _id=Long.parseLong(((String)h.get(News._ID)));
			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				Uri u=ContentUris.withAppendedId(News.RSSFeeds.CONTENT_URI,_id);
				res=News.delete(u,null,null);
				Log.d(_TAG,"deleted "+res+" rows");
			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{
				Uri u=ContentUris.withAppendedId(News.AtomFeeds.CONTENT_URI,_id);
				res=News.delete(u,null,null);
				Log.d(_TAG,"deleted "+res+" rows");
			}
			updateChannelsList();
		}else{Log.d(_TAG,"SelectedItem>>null<<");}
		
	}


	private void menuServiceSettings() {
		Log.v(_TAG,"menuServieSettings:entering");
		// TODO Auto-generated method stub
		Intent i= new Intent(this,ServiceSettings.class);
		startSubActivity(i,ServiceSettings.ACTIVITY_MODIFY);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(_TAG, "onResume: entering");
		//calculateListSize();
       // mRSSListView.debug();
       // Log.d(_TAG,"count from obj>>"+mRSSListView.getAdapter().getCount());
	   updateChannelsList();
	
        Log.d(_TAG,"onResume: leaving");
	}
	
	@Override
	protected void onFreeze(Bundle icicle){
		Log.d(_TAG, "onFreeze: entering");
		
	}
	


	private void updateChannelsList(){

		int cursorItems=mRSSCursor.count();

		String[] namesList= new String[cursorItems];
		int namesRowIndex=mRSSCursor.getColumnIndex(News.RSSFeeds.CHANNEL_NAME);
		int idRowIndex=mRSSCursor.getColumnIndex(News.RSSFeeds._ID);
		List <HashMap> dataList=new Vector<HashMap>();
		mRSSCursor.first();


		for (int i=0;i<cursorItems ;i++ )
		{
			HashMap<String,String> data= new HashMap<String,String>();
			
			String tID=mRSSCursor.getString(idRowIndex);
			Log.d(_TAG,"ID IS >>"+tID+"<<");
			Cursor tempCursor= News.mContentResolver.query(
				News.RSSFeedContents.CONTENT_URI,
				RSS_CONTENTS_PROJECTION,
				News.RSSFeedContents.CHANNEL_ID+"="+tID,
				null,
				""
			);
			int count=tempCursor.count();
			tempCursor.close();
			data.put(News._ID,tID);
			data.put(News.RSSFeeds.CHANNEL_NAME,mRSSCursor.getString(namesRowIndex));
			data.put(News.MESSAGE_COUNT,Integer.toString(count));
			data.put(News.FEED_TYPE,News.FEED_TYPE_RSS);
			dataList.add(data);
			mRSSCursor.next();
		}

		ListAdapter channelsAdapter = new ChannelListAdapter(
			this,
			dataList
			);


        //Log.d(_TAG,"rssListAdapter count>>"+rssListAdapter.getCount()+"<<");
        Log.d(_TAG,"cahnnelsAdapter count>>"+channelsAdapter.getCount()+"<<");
        mChannelsView.setAdapter(channelsAdapter);
        mChannelsView.debug();
        //Log.d(_TAG,"count from obj>>"+mRSSListView.getAdapter().getCount());



	}
	
    public void openChannel(){    

		// TODO Auto-generated method stub
		Object o=mChannelsView.getSelectedItem();
		int res;
		/*
		long i=mChannelsView.getSelectedItemId();
		int n=mChannelsView.getSelectedItemPosition();
		
		Log.d(_TAG,"SelectedItemId>>"+i+"<<");
		Log.d(_TAG,"SelectedItemPOS>>"+n+"<<");
		*/
		if (o!=null)
		{
		
			Log.d(_TAG,"SelectedItem>>"+o.toString()+"<<");
			HashMap h=(HashMap)o;
			String feedType=(String)h.get(News.FEED_TYPE);
			String _id=(String)h.get(News._ID);
			Bundle b =new Bundle();
			b.putString(News.FEED_TYPE,feedType);
			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				b.putString(News.RSSFeedContents.CHANNEL_ID,_id);
				b.putString(News.RSSFeeds.CHANNEL_NAME,(String)h.get(News.RSSFeeds.CHANNEL_NAME));
				
				

			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{

				b.putString(News.AtomFeedContents.FEED_ID,_id);
				b.putString(News.AtomFeeds.FEED_TITLE,(String)h.get(News.AtomFeeds.FEED_TITLE));
				

			}
			
			Intent i= new Intent(this,AFeedMessages.class);
			//startSubActivity(i,AFeedSettings.ACTIVITY_EDIT);
			i.setAction(Intent.EDIT_ACTION);
			i.setData(getIntent().getData());
			i.putExtras(b);
			startActivity(i);

		}else{Log.d(_TAG,"SelectedItem>>null<<");}    }




	
}/*eoc*/
