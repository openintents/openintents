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

import android.app.Service;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.widget.Toast;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.ContentUris;
import android.content.Context;

import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Audio;
import android.content.ContentValues;


import java.util.HashMap;


public class PlaylistGeneratorService extends android.app.Service{

	private static final String TAG="PlaylistGeneratorService";


	public void onCreate(){

		
		new AllTracksGenerator(this,"all").run();
	}


	public IBinder onBind(Intent i){
		return null;
	}







	public void onDestroy(){


	}


	private class AllTracksGenerator implements Runnable
	{
		private Context context=null;

		/** Specifies the relevant columns. */
		String[] audioProjection = new String[] {
			android.provider.BaseColumns._ID,
			android.provider.MediaStore.MediaColumns.TITLE,
			android.provider.MediaStore.Audio.AudioColumns.ARTIST,
			android.provider.MediaStore.MediaColumns.DATA
		};

		private String[] playlistBaseProjection= new String[]{
			android.provider.BaseColumns._ID,
			PlaylistsColumns.NAME,
			PlaylistsColumns.DATA
		};

		/** Specifies the relevant columns. */
		String[] mPlaylistProjection = new String[] {
			android.provider.BaseColumns._ID,
			android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID,
			android.provider.MediaStore.Audio.Playlists.Members.PLAY_ORDER
		};


		private String listName="";

		public AllTracksGenerator(Context context,String listname){
			this.context=context;
			this.listName=listname;
		}

		public void run(){
			long playlistID=0;
			Cursor nameCursor=context.getContentResolver().query(
				Playlists.EXTERNAL_CONTENT_URI,
				playlistBaseProjection,
				PlaylistsColumns.NAME+" like '"+this.listName+"'",
				null,
				null
			);

			if (nameCursor==null || nameCursor.getCount()==0)
			{
				ContentValues cv=new ContentValues();
				cv.put(PlaylistsColumns.NAME,this.listName);
				Uri plUri=context.getContentResolver().insert(
					Playlists.EXTERNAL_CONTENT_URI,
					cv
					);
				playlistID=Long.parseLong(plUri.getLastPathSegment());
				Log.d(TAG,"parser id>"+playlistID);
			}else{
				nameCursor.moveToFirst();
				playlistID=nameCursor.getLong(
					nameCursor.getColumnIndexOrThrow(android.provider.BaseColumns._ID)
					);

			}
	/*
			Uri listUri=Uri.withAppendedPath(Playlists.EXTERNAL_CONTENT_URI, String
							.valueOf(menuInfo.id)).
				withAppendedPath(
	*/
			Uri listUri=Playlists.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String
							.valueOf(playlistID)).appendPath("members").build();


			Cursor listCursor=context.getContentResolver().query(
				listUri,
				mPlaylistProjection,
				null,
				null,
				null
			);

			Cursor audioCursor=context.getContentResolver().query(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				audioProjection,null,null,null);

			int indexID = audioCursor.getColumnIndex(android.provider.BaseColumns._ID);
			int indexDATA = audioCursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
			int indexTitle = audioCursor.getColumnIndex(android.provider.MediaStore.MediaColumns.TITLE);
			int indexArtist = audioCursor.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ARTIST);
			audioCursor.moveToFirst();

			int playorder=listCursor.getCount();
			listCursor.close();

			while (!audioCursor.isAfterLast())
			{

				long audioID=audioCursor.getLong(indexID);

				listCursor=context.getContentResolver().query(
							listUri,
							mPlaylistProjection,
							Playlists.Members.AUDIO_ID+"="+audioID,
							null,
							null
						);		

				if (listCursor==null || listCursor.getCount()==0)
				{
					ContentValues cv=new ContentValues();
					cv.put(Playlists.Members.AUDIO_ID,audioID);
					//since we add to the end of the list, playorder=length of list
					cv.put(Playlists.Members.PLAY_ORDER,playorder);

					context.getContentResolver().insert(listUri,cv);
					Log.d(TAG,"insert to pl>"+this.listName+"< cv>"+cv.toString());
					playorder++;
				}

				audioCursor.moveToNext();
			}

			audioCursor.close();

		}
	};
}