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
import android.widget.AdapterView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ContentUris;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.net.Uri;


public class PlaylistBrowser extends Activity{

	private Cursor mCursor;
	private ListView mList;

	private String[] projection= new String[]{
        android.provider.BaseColumns._ID,
		PlaylistsColumns.NAME,
		PlaylistsColumns.DATA
	};

	public static final String _TAG="PlaylistBrowser";


	private String state="";

	protected static final int CTX_MENU_EDIT=101;
	protected static final int CTX_MENU_DELETE=102;

	protected static final int SUB_ACTIVITY_EDIT_PLAYLIST=201;

	public void onCreate(Bundle bundle){
			super.onCreate(bundle);

			setContentView(R.layout.playlistbrowser);

			mList=(ListView)findViewById(R.id.playlistlist);
			mList.addFooterView(new AddPlaylistFooterView(this));

			mCursor=managedQuery(Playlists.EXTERNAL_CONTENT_URI,projection,null,null,null);
			Log.d(_TAG,"cursoritems>"+mCursor.getCount());
			SimpleCursorAdapter sca=new SimpleCursorAdapter(
				this,
                android.R.layout.simple_list_item_1, 
				mCursor,
				new String[]{PlaylistsColumns.NAME},
                new int[] {android.R.id.text1}
			);
			mList.setAdapter(sca);

			mList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

				public void onCreateContextMenu(ContextMenu contextmenu,
						View view, ContextMenu.ContextMenuInfo obj) {
					contextmenu
							.add(0, CTX_MENU_EDIT, 0, "Edit Title");
					contextmenu
						.add(0, CTX_MENU_DELETE, 0,"Delete");

				}

			});
		

			mList.setOnItemClickListener(
				new AdapterView.OnItemClickListener(){

					public void onItemClick(AdapterView parent,View v,int position,long id){
						
						Intent i=new Intent();
						
						i.setData(
							ContentUris.withAppendedId(Playlists.EXTERNAL_CONTENT_URI,id)
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



		state=getIntent().getAction();
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
			
			case CTX_MENU_EDIT:
				Intent i= new Intent();
				i.setAction(Intent.ACTION_EDIT);
				
				i.setData(Uri.withAppendedPath(Playlists.EXTERNAL_CONTENT_URI, String
						.valueOf(menuInfo.id)));

				startActivityForResult(i,SUB_ACTIVITY_EDIT_PLAYLIST);
				break;
			case CTX_MENU_DELETE:
				delete(menuInfo.id, menuInfo.position);
				break;
		}
		return true;
	}



	private void delete(final long id, int position) {
	
		
	}

}