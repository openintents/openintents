package org.openintents.applications.newsreader;



import org.openintents.news.views.MessageListAdapter;
import org.openintents.provider.News;
import org.openintents.tags.MagnoliaTagging;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.ListView;

public class AFeedMessages extends ListActivity {

	private static final int MENU_CHANNELITEM=1001;
	private static final int MENU_CHANNELSETTINGS=1002;
	private static final int MENU_TAGS=1003;
	
	
	private static final int SUBMENU_CHANNELITEM_TAG=2001; 	
	private static final int SUBMENU_CHANNELITEM_DELETE=2002;
	private static final int SUBMENU_CHANNELITEM_FOLLOW=2003;
	private static final int SUBMENU_CHANNELITEM_MAGNOLIA=2004; 
	
	private static final int SUBMENU_CHANNELSETTINGS_TAG=3001;
	private static final int SUBMENU_CHANNELSETTINGS_EDITSETTINGS=3002;
	private static final int SUBMENU_CHANNELSETTINGS_DELETEALL=3003;
	
	private static final String _TAG="AFeedMessages";

	private Cursor mCursor;
	
	private String feedType	="";
	private String feedID	="";
	private String feedName	="";

	

	private static String[] RSS_PROJECTION=new String[]{
		News.RSSFeedContents._ID,
		News.RSSFeedContents._COUNT,
		News.RSSFeedContents.ITEM_TITLE,
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


    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
        
		init();
    }
        
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {    
		Log.d(_TAG,"Clicked at >>"+position);
		v.debug();
       ((MessageListAdapter)getListAdapter()).toggle(position);
    }
    
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
			}else{
				Log.e(_TAG,"Cursor was empty");
			}
		}
		
		
		
		this.setTitle(feedName);
		// Use our own list adapter
        setListAdapter(new MessageListAdapter(mCursor,this,feedType));
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		Log.v("AFeedMessages","onCreateOptionsMenu:entering");
		
		android.view.SubMenu submenu;

		
		boolean result= super.onCreateOptionsMenu(menu);
		
		menu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_FOLLOW,"Open in Browser");
		menu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_DELETE,"Delete",R.drawable.shoppinglistdelete001b);
		
		submenu=menu.addSubMenu(0,AFeedMessages.MENU_TAGS, "Tags",R.drawable.tagging_application001a);
		//submenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_TAG,"Tag Local",R.drawable.tagging_application001a);
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_MAGNOLIA,"Magnolia",R.drawable.tagging_magnolia_application001a);

/*
		submenu=menu.addSubMenu(0,AFeedMessages.MENU_CHANNELITEM, "Message");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_FOLLOW,"Open in Browser");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_TAG,"Tag This");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_DELETE,"Delete");
	
		submenu=menu.addSubMenu(0,AFeedMessages.MENU_CHANNELSETTINGS,"Channel");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELSETTINGS_TAG,"Tag This");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELSETTINGS_EDITSETTINGS,"Edit Settings");
		submenu.add(0,AFeedMessages.SUBMENU_CHANNELSETTINGS_DELETEALL,"Delete All");

*/
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

	@Override
	public boolean onOptionsItemSelected(Item item){
		Log.v(_TAG,"onOptionsItemSelected: item.id>>"+item.getId()+"<<");
		int iID=item.getId();
		if (iID==SUBMENU_CHANNELITEM_FOLLOW)
		{
			followItemLink();
		}else if (iID==SUBMENU_CHANNELITEM_DELETE)
		{
			menuDelete();
		}else if (iID==SUBMENU_CHANNELITEM_TAG)
		{
			menuTag();
		}else if (iID==SUBMENU_CHANNELITEM_MAGNOLIA)
		{			
			menuMagnolia();
		}

		return super.onOptionsItemSelected(item);
	}



	private void followItemLink(){
		int pos=getSelectedItemPosition();
		Log.d(_TAG,"followItemLink: pos>>"+pos+"<<");
		if (pos>-1)
		{
		
			mCursor.moveTo(pos);
			Uri uri=null;
			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				String strUri=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_LINK));
				uri=Uri.parse(strUri);
			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{
				String strUri=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_LINK));
				uri=Uri.parse(strUri);
			}				
			Intent intent = new Intent(Intent.VIEW_ACTION, uri);
			startActivity(intent);
		}
	}

	private void menuDelete(){
		boolean res=false;
		int pos=getSelectedItemPosition();
		if (pos>-1)
		{
		
			mCursor.moveTo(pos);
			res=mCursor.deleteRow();
			Log.d(_TAG," deletet row #"+pos+" >"+res);
		}
	}

	private void menuTag(){}

	private void menuMagnolia(){

		Bundle b=new Bundle();
		int pos=getSelectedItemPosition();
		if (pos>-1)
		{
			
			mCursor.moveTo(pos);
			String strUri="";
			String desc="";

			if (feedType.equals(News.FEED_TYPE_RSS))
			{
				strUri=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_LINK));
				desc=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_TITLE));
			
			}else if (feedType.equals(News.FEED_TYPE_ATOM))
			{
				strUri=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_LINK));
				desc=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_TITLE));
			
			}
			b.putString(MagnoliaTagging.URI,strUri);
			b.putString(MagnoliaTagging.DESCRIPTION,desc);
			Intent intent = new Intent();
			intent.setAction("org.openintents.action.TAGMAGNOLIA");
			intent.addCategory(Intent.DEFAULT_CATEGORY);
			intent.putExtras(b);
			startActivity(intent);
		}
	}

    
    
}/*eoc*/
