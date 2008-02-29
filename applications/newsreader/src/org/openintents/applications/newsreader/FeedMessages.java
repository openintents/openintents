package org.openintents.applications.newsreader;



import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.database.Cursor;


import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.app.ExpandableListActivity;


import org.openintents.provider.News;
import org.openintents.news.views.*;
import org.openintents.news.views.MessageCursorTreeAdapter;

public class FeedMessages extends ExpandableListActivity {

	private static final int MENU_CHANNELITEM=1001;
	private static final int MENU_CHANNELSETTINGS=1002;
	
	
	private static final int SUBMENU_CHANNELITEM_TAG=2001; 
	private static final int SUBMENU_CHANNELITEM_DELETE=2002;
	
	private static final int SUBMENU_CHANNELSETTINGS_TAG=3001;
	private static final int SUBMENU_CHANNELSETTINGS_EDITSETTINGS=3002;
	private static final int SUBMENU_CHANNELSETTINGS_DELETEALL=3003;
	
	private static final String _TAG="AFeedMessages";

	private Cursor mCursor;
	
	private String feedType	="";
	private String feedID	="";
	private String feedName	="";

	private ExpandableListAdapter mAdapter;

	private static String[] RSS_PROJECTION=new String[]{
		News.RSSFeedContents._ID,
		News.RSSFeedContents._COUNT,
		News.RSSFeedContents.ITEM_TITLE,
		News.RSSFeedContents.ITEM_LINK,
		News.RSSFeedContents.ITEM_DESCRIPTION
	};

	private static String[] RSS_SUB_PROJECTION=new String[]{
		News.RSSFeedContents._ID,
		News.RSSFeedContents._COUNT,
		News.RSSFeedContents.ITEM_LINK,
		News.RSSFeedContents.ITEM_DESCRIPTION
	};	


	private static String[] ATM_PROJECTION=new String[]{
		News.AtomFeedContents._ID,
		News.AtomFeedContents._COUNT,
		News.AtomFeedContents.ENTRY_TITLE,
		News.AtomFeedContents.ENTRY_LINK,
		News.AtomFeedContents.ENTRY_SUMMARY
	};


	private static String[] ATM_SUB_PROJECTION=new String[]{
		News.AtomFeedContents._ID,
		News.AtomFeedContents._COUNT,	
		News.AtomFeedContents.ENTRY_LINK,
		News.AtomFeedContents.ENTRY_SUMMARY
	};


    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
        
		init();
    }
        
    /*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {    
		Log.d(_TAG,"Clicked at >>"+position);
		v.debug();
       ((MessageListAdapter)getListAdapter()).toggle(position);
    }
    
	*/
	@Override
	protected void onFreeze(Bundle icicle){
		stopManagingCursor(mCursor);
		Log.d("AFEEDLIST", "onFreeze: entering");
		
	}  

	@Override
	protected void onResume(){
		super.onResume();
		Log.d(_TAG,"onResume: entering");
		init();
	}
	

	private void init(){
		Bundle b=getIntent().getExtras();
		feedType=b.getString(News.FEED_TYPE);
		
		
		News.mContentResolver=getContentResolver();
		if (feedType.equals(News.FEED_TYPE_RSS))
		{
			feedID	=b.getString(News.RSSFeedContents.CHANNEL_ID);
			feedName=b.getString(News.RSSFeeds.CHANNEL_NAME);
			
			mCursor=managedQuery(News.RSSFeedContents.CONTENT_URI,RSS_PROJECTION,News.RSSFeedContents.CHANNEL_ID+"="+feedID,null);
			Log.d(_TAG,"feedName>>"+feedName+"<<");
			Log.d(_TAG,"feedID>>"+feedID+"<<");
			Log.d(_TAG,"mCursor.count()>>"+mCursor.count()+"<<");
			if (mCursor.count()>0)
			{

				mAdapter = new MessageCursorTreeAdapter(
						this,
						mCursor,
						R.layout.newsmessage_items,
						//android.R.layout.simple_expandable_list_item_1,
						R.layout.newsmessage_items,
						new String[] {News.RSSFeedContents.ITEM_TITLE}, // Name for group layouts
						//new int[] {android.R.id.text1},
						new int[] {R.id.newsmessage_items_title,},
						new String[] {
									News.RSSFeedContents.ITEM_DESCRIPTION,
									News.RSSFeedContents.ITEM_LINK}, // Number for child layouts
						new int[] {
									R.id.newsmessage_items_message,
									R.id.newsmessage_items_link
									},
						feedType
						);


			}else {
				Log.e(_TAG,"Cursor was empty");
			}
		}else if (feedType.equals(News.FEED_TYPE_ATOM))
		{

			feedID=b.getString(News.AtomFeedContents.FEED_ID);
			feedName=b.getString(News.AtomFeeds.FEED_TITLE);

			mCursor=managedQuery(News.AtomFeedContents.CONTENT_URI,ATM_PROJECTION,News.AtomFeedContents.FEED_ID+"="+feedID,null);
			Log.d(_TAG,"State Edit::mCursor.count()>>"+mCursor.count()+"<<");
			if (mCursor.count()>0)
			{

				mAdapter = new MessageCursorTreeAdapter(
						this,
						mCursor,
						android.R.layout.simple_expandable_list_item_1,
						android.R.layout.simple_expandable_list_item_1,
						new String[] {News.RSSFeedContents.ITEM_TITLE}, // Name for group layouts
						new int[] {android.R.id.text1},
						new String[] {
									News.RSSFeedContents.ITEM_DESCRIPTION,
									News.RSSFeedContents.ITEM_LINK}, // Number for child layouts
						new int[] {
									R.id.newsmessage_items_message,
									R.id.newsmessage_items_link
									},
						feedType
						);



			}else{
				Log.e(_TAG,"Cursor was empty");
			}
		}
		
		
		
		this.setTitle(feedName);
		// Use our own list adapter

		
		setListAdapter(mAdapter);

        //setListAdapter(new MessageListAdapter(mCursor,this,feedType));
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		Log.v("AFeedMessages","onCreateOptionsMenu:entering");
		
		android.view.SubMenu submenu;

		
		boolean result= super.onCreateOptionsMenu(menu);
		
		submenu=menu.addSubMenu(0,FeedMessages.MENU_CHANNELITEM, "News");
		submenu.add(0,FeedMessages.SUBMENU_CHANNELITEM_TAG,"Tag This");
		submenu.add(0,FeedMessages.SUBMENU_CHANNELITEM_DELETE,"Delete");
	
		submenu=menu.addSubMenu(0,FeedMessages.MENU_CHANNELSETTINGS,"Channel");
		submenu.add(0,FeedMessages.SUBMENU_CHANNELSETTINGS_TAG,"Tag This");
		submenu.add(0,FeedMessages.SUBMENU_CHANNELSETTINGS_EDITSETTINGS,"Edit Settings");
		submenu.add(0,FeedMessages.SUBMENU_CHANNELSETTINGS_DELETEALL,"Delete All");

		return result;
		
	}
	
	/*
	@Override
	public boolean onCreatePanelMenu(int featureID,Menu menu){/*
		Log.v("AFeedMessages","onCreatePanelMenu:entering");
		Log.v("AFeedMessages","onCreatePanelMenu:featureID>>"+featureID+"<<");
		if (featureID==AFeedMessages.MENU_CHANNELITEM){
			
			menu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_TAG,"Tag This");
			menu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_DELETE,"Delete");
		}
		return true;
		
	}
    */




    

		
    
}/*eoc*/
