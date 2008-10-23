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

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.EditText;

import android.database.Cursor;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Audio;

import android.content.ContentValues;

public class AddPlaylistFooterView extends LinearLayout{
	
	private Cursor mCursor;

	private Context context;
	private Button mAddButton;
		
	EditText mText1;
	

	public AddPlaylistFooterView(Context context){
		super(context);
		this.context = context;
		this.setOrientation(HORIZONTAL);

		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout internalLayout= (RelativeLayout)inflater.
			inflate(R.layout.addplaylistfooter,null);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
		);

		addView(internalLayout, lp);
		
		mAddButton=(Button)internalLayout.findViewById(R.id.add_playlist);

		mAddButton.setOnClickListener(new Button.OnClickListener(){
	
			public void onClick(View v) {
				addPlaylist();
			}
		});

		mText1=(EditText)internalLayout.findViewById(R.id.name_playlist);

	}


	private void addPlaylist(){

		ContentValues cv= new ContentValues();
		String name=mText1.getText().toString();
		if (name==null || name.equals(""))
		{
			name="new unnamed playlist";
		}

		cv.put(PlaylistsColumns.NAME,name);
		this.context.getContentResolver().insert(
			Playlists.EXTERNAL_CONTENT_URI,
			cv
		);

		mText1.setText("");



	}
}