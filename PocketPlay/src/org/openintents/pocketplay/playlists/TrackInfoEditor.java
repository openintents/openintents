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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class TrackInfoEditor extends Activity{


	Uri baseUri=null;

	private TextView mTitle;
	private TextView mAlbum;
	private TextView mArtist;
	private TextView mGenre;
	private TextView mComment;
	private TextView mTags;
	
	private Cursor mCursor;
	private String[] mProjection=new String[] {
		android.provider.MediaStore.Audio.Media._ID,
		android.provider.MediaStore.Audio.Media.DATA,
		android.provider.MediaStore.Audio.Media.TITLE,
		android.provider.MediaStore.Audio.Media.ARTIST,
		android.provider.MediaStore.Audio.Media.ALBUM,
	};


	public void onCreate(Bundle bundle){

		super.onCreate(bundle);

		setContentView(R.layout.trackinfoeditor);
		
		mTitle = (TextView) findViewById(R.id.title);
		mAlbum = (TextView) findViewById(R.id.album);
		mArtist = (TextView) findViewById(R.id.artist);
		//mGenre = (TextView) findViewById(R.id.genre);
		//mComment = (TextView) findViewById(R.id.comment);
		//mTags = (TextView) findViewById(R.id.tags);
		//mRating = (RatingBar) findViewById(R.id.rating);
		//mRating.setOnRatingBarChangeListener(this);
		



		Intent in=getIntent();
		baseUri=in.getData();

		mCursor=getContentResolver().query(
			baseUri,
			mProjection,
			null,
			null,
			null
		);

		if (mCursor!=null&&mCursor.moveToFirst())
		{

			setTextField(mTitle,mCursor,2);
			setTextField(mArtist,mCursor,3);
			setTextField(mAlbum,mCursor,4);
			
		}



	}




	private void setTextField(TextView view, Cursor c, int row) {

		if (c.getString(row) == null || c.getString(row).equals("")) {

			view.setHint("<unknown>");
		}
		view.setText(c.getString(row));

	}


}