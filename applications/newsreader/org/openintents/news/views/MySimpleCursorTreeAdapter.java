package org.openintent.news.views;



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
//import org.openintents.news.views.*;

public class MySimpleCursorTreeAdapter extends SimpleCursorTreeAdapter {

	private String feedType="";
	private static final String _TAG="MySimpleCursorTreeAdapter";

	private int mIDRow=0;
	private int mFeedIDRow=0;

	public MySimpleCursorTreeAdapter( Context context,Cursor cursor, int groupLayout,
			int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
			int[] childrenTo,String feedType) {

		super(
				context,
				cursor, 
				groupLayout, 
				groupFrom, 
				groupTo, 
				childLayout, 
				childrenFrom,
				childrenTo
			);
		Log.d(_TAG,"feedType is >>"+feedType+"<<");
		this.feedType=feedType;
		this.mIDRow		=cursor.getColumnIndex(News.RSSFeedContents._ID);
		this.mFeedIDRow	=cursor.getColumnIndex(News.RSSFeedContents.CHANNEL_ID);
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		Cursor childCursor=null;

		Log.d(_TAG,"EHLLLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOO");
		Log.d(_TAG,"::getChildrenCursor: feedType is >>"+feedType+"<<");
		Log.d(_TAG,"::getChildrenCursor: NEWSfeedType is >>"+News.FEED_TYPE_RSS+"<<");

		//if (feedType.equals(News.FEED_TYPE_RSS))
	//	{
		//query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
			 childCursor=News.mContentResolver.query(
				News.RSSFeedContents.CONTENT_URI,
				RSS_SUB_PROJECTION,
				News.RSSFeedContents.CHANNEL_ID+"="+groupCursor.getString(mFeedIDRow)+" AND "
				+News.RSSFeedContents._ID+"="+groupCursor.getString(mIDRow),
				null,
				null
				);

				Log.d(_TAG,"::getChildrenCursor: dumping childCursor:\n"+dumpCursor(childCursor)+"\n-------------------------------------");
	/*			
		}else if (feedType.equals(News.FEED_TYPE_ATOM))
		{
			 childCursor=managedQuery(
				News.AtomFeedContents.CONTENT_URI,
				FeedMessages.ATM_PROJECTION,
				News.AtomFeedContents.FEED_ID+"="+feedID+" AND "
				+News.AtomFeedContents._ID+"="+groupCursor.getString(mIDRow)
				,null);

		}
*/
		return childCursor;
	}

	private String dumpCursor(Cursor c){
		StringBuffer buf=new StringBuffer();
		int curLen=c.count();
		buf.append("\n------------------------------------------\n");
		buf.append("-c.count()>"+curLen+"<\n");
		buf.append("-c.supportsUpdates()>"+c.supportsUpdates()+"<\n");
		buf.append("--columns:\n");
		String[] colNames=c.getColumnNames();
		for (int i1=0;i1<colNames.length ;i1++ )
		{
			buf.append("---["+i1+"] >>"+colNames[i1]+"<< \n");
		}
		buf.append("--\n");

		buf.append("----rows:\n");
		
		c.first();
		for (int i=0;i<curLen ;i++ )
		{

			buf.append("---row["+i+"]\n");
			for (int n=0;n<colNames.length ;n++ )
			{
				buf.append("----["+n+"] >>"+colNames[n]+"<< =>"+c.getString(n)+"< \n");
			}
			buf.append("---\n");
		}

		buf.append("\n------------------------------------------");
		c.first();
		return buf.toString();

	}







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












}