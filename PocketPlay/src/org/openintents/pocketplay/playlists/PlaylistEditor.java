package org.openintents.pocketplay.playlists;
/*
 <!-- 
 * Copyright (C) 2007-2008 OpenIntents.org
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
 -->*/

import org.openintents.pocketplay.R;

import android.app.Activity;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Audio;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.os.Bundle;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.net.Uri;
import android.content.ContentValues;
import android.content.ContentUris;



public class PlaylistEditor extends Activity implements AddTrackFooterView.DoAddTrackListener{

	private Cursor mCursor;
	private ListView mList;

	private String[] projection= new String[]{
		android.provider.MediaStore.Audio.Media._ID,
		android.provider.MediaStore.Audio.Media.TITLE,
		android.provider.MediaStore.Audio.Media.ARTIST,
		android.provider.MediaStore.Audio.Media.ALBUM 
			};

	public static final String _TAG="PlaylistEditor";

	
	private static final int SUB_ACTIVITY_TRACKBROWSER=101;

	private long playlistID=-1;

	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

			setContentView(R.layout.playlisteditor);

			mList=(ListView)findViewById(R.id.playlistlist);
			AddTrackFooterView atf=new AddTrackFooterView(this);
			atf.setDoAddTrackListener(this);
			mList.addFooterView(atf);


			Intent caller=getIntent();

			mCursor=managedQuery(Uri.withAppendedPath(caller.getData(),"members"),projection,null,null,null);
			Log.d(_TAG,"cursoritems>"+mCursor.getCount());
			SimpleCursorAdapter sca=new SimpleCursorAdapter(
				this,
                android.R.layout.simple_list_item_1, 
				mCursor,
				new String[]{Media.TITLE},
                new int[] {android.R.id.text1}
			);
			mList.setAdapter(sca);
			//TODO: rethink if this cursor needs requery in onResume
			/*
			playlistID=	mCursor.getLong(
						mCursor.getColumnIndexOrThrow(android.provider.BaseColumns._ID)
					);
*/
			playlistID=Long.parseLong(getIntent().getData().getLastPathSegment());
	}


	public void addTrack(){
		Intent intent=new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setData(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//		intent.setClassName("com.android.music","com.android.music.TrackBrowserActivity");
		//intent.setClassName("org.openintents.pocketplay.playlists","org.openintents.pocketplay.playlists.TrackBrowser");
		startActivityForResult(intent,SUB_ACTIVITY_TRACKBROWSER);


	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		
		if (requestCode==SUB_ACTIVITY_TRACKBROWSER)
		{
			if (resultCode==RESULT_OK)
			{

						
				ContentValues cv= new ContentValues();
				long trackID=-1;
				try
				{
					trackID=Long.parseLong(data.getData().getLastPathSegment());	
				}
				catch (NumberFormatException nfe)
				{
					Log.e(_TAG,"Error in activity result: uri had no id leaf, uri>"+data.getData().toString());
					return;
				}
				

				cv.put(Playlists.Members.AUDIO_ID,trackID);
				//since we add to the end of the list, playorder=length of list
				cv.put(Playlists.Members.PLAY_ORDER,mCursor.getCount());
				//cv.put(Playlists.Members.PLAYLIST_ID,playlistID);
				Uri uri=ContentUris.withAppendedId(Playlists.EXTERNAL_CONTENT_URI,playlistID);
				uri=Uri.withAppendedPath(uri,"members");
				Log.d(_TAG,"CONTENT URI>>"+uri.toString()+"<<");
				Log.d(_TAG,"CONTENT Values>>"+cv.toString()+"<<");
				
				getContentResolver().insert(uri,cv);
				
				


			}
		}



	}
}