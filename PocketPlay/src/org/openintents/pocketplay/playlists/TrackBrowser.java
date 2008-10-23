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
import android.content.ContentUris;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.view.MenuItem;
import android.net.Uri;


public class TrackBrowser extends Activity{

	private Cursor mCursor;
	private ListView mList;

	private String[] projection= new String[]{
		android.provider.MediaStore.Audio.Media._ID,
		android.provider.MediaStore.Audio.Media.DATA,
		android.provider.MediaStore.Audio.Media.TITLE,
		android.provider.MediaStore.Audio.Media.ARTIST,
		android.provider.MediaStore.Audio.Media.ALBUM 
			};

	public static final String _TAG="TrackBrowser";

	protected static final int CTX_MENU_EDIT=101;
	protected static final int CTX_MENU_DELETE=102;

	protected static final int SUB_ACTIVITY_EDIT_PLAYLIST=201;

	private String state="";


	public void onCreate(Bundle bundle){
			super.onCreate(bundle);

			setContentView(R.layout.playlistbrowser);

			mList=(ListView)findViewById(R.id.playlistlist);

			mCursor=managedQuery(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection,null,null,null);
			Log.d(_TAG,"cursoritems>"+mCursor.getCount());
			SimpleCursorAdapter sca=new SimpleCursorAdapter(
				this,
                android.R.layout.simple_list_item_1, 
				mCursor,
				new String[]{Media.TITLE},
                new int[] {android.R.id.text1}
			);
			mList.setAdapter(sca);

			state=getIntent().getAction();

			mList.setOnItemClickListener(
				new AdapterView.OnItemClickListener(){

					public void onItemClick(AdapterView parent,View v,int position,long id){
						
						Intent i=new Intent();
						
						i.setData(
							ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
							);
						if (state.equals(Intent.ACTION_PICK))
						{
							
							setResult(RESULT_OK,i);
							finish();
						}else{
							i.setAction(Intent.ACTION_VIEW);
							startActivity(i);
							finish();
						}

					}
			});
	}

		
	
}