package org.openintents.pocketplay.playback;
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Audio;
import android.content.ContentValues;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.HashMap;



public class AudioPlayerService extends Service implements MediaPlayerEngine.PlayerEngineListener {

    /**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IAudioPlayerCallback> mCallbacks
            = new RemoteCallbackList<IAudioPlayerCallback>();

	IAudioPlayerCallback tryme;

	private static final String TAG="AudioPlayerService";

    /** Specifies the relevant columns. */
    String[] mProjection = new String[] {
        android.provider.BaseColumns._ID,
        android.provider.MediaStore.MediaColumns.TITLE,
        android.provider.MediaStore.Audio.AudioColumns.ARTIST,
        android.provider.MediaStore.MediaColumns.DATA
    };

    /** Specifies the relevant columns. */
    String[] mPlaylistProjection = new String[] {
        android.provider.BaseColumns._ID,
        android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID,
        android.provider.MediaStore.Audio.Playlists.Members.PLAY_ORDER
    };
    
	/** URI of current media file. */
	private Uri mURI;

	/** Uri to playlist info*/
	private Uri mPlaylistBaseURI;

	/** Uri to playlist members (data to read)*/
	private Uri mPlaylistURI;
	
	/** Location of the media file. */
	private String mFilename;
	
	/** Title of the media file. */
	private String mTitle;
    
	private String mCurrentTitle;
	private String mCurrentArtist;
	private String mCurrentPlaylist;
	private int mCurrentPlaylistPosition;

	private Cursor mCursor;
	private Cursor mPlaylistCursor;

	private MediaPlayerEngine engine;



	private static final String PREFS_NAME="pocketplay_preferences";
	private static final String PREFS_PREFIX="pldata_";
	private static final String PREFS_LAST_PLAYLIST	="prefs_last_playlist";
	private static final String PREFS_LOAD_LAST_LIST="prefs_load_last_list";
	private String DEFAULT_PLAYLIST="all"; //name is language dependent



	public static final int INFO_PLAYLIST_URI	=0;
	public static final int INFO_PLAYLIST_NAME	=1;
	public static final int INFO_PLAYLIST_POS	=2;
	public static final int INFO_TRACK_URI		=3;
	public static final int INFO_TRACK_TITLE	=4;
	public static final int INFO_TRACK_ARTIST	=5;

	public void onCreate(Bundle bundle){

		engine=new MediaPlayerEngine(this);
		engine.setPlayerEngineListener(this);

		registerPhoneListener();		

	}

    @Override
    public IBinder onBind(Intent intent) {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
		registerPhoneListener();
        return mBinder;
    }


	private void registerPhoneListener(){
		Log.d(TAG,"REGISTERING TELEPHONY LISTENER");

		TelephonyManager tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(new CallHandler(),CallHandler.LISTEN_CALL_STATE);

	}

    /**
     * The IRemoteInterface is defined through IDL
     */
    private final IAudioPlayerService.Stub mBinder = new IAudioPlayerService.Stub() {
        public void registerCallback(IAudioPlayerCallback cb) {
			Log.d(TAG,"registering Callback>"+cb);

            if (cb != null) mCallbacks.register(cb);
			tryme=cb;
        }

        public void unregisterCallback(IAudioPlayerCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }

		public void play(){
			AudioPlayerService.this.doPlay();
		};


		public void pause(){
			Log.d(TAG,"in STUB:: Pause");
			if (engine!=null)
			{
				engine.pauseMedia();
			}
		};

		public void stop(){
			if (engine!=null)
			{
				engine.stopMedia();
			}		
		
		}


		public void nextTrack(){
			String sURI;
			stop();
			if (mPlaylistCursor!=null && !mPlaylistCursor.isLast())
			{//play next track
				mPlaylistCursor.moveToNext();
				long audioID=mPlaylistCursor.getLong(
					mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
					);

				sURI=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()+"/" + audioID;
				mCurrentPlaylistPosition=mPlaylistCursor.getPosition();
				internalLoadFile(sURI);


			}else if (mPlaylistCursor!=null && mPlaylistCursor.isLast())
			{//switch to first track

				mPlaylistCursor.moveToFirst();
				long audioID=mPlaylistCursor.getLong(
					mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
					);
				
				sURI=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()+"/" + audioID;
				mCurrentPlaylistPosition=mPlaylistCursor.getPosition();
				internalLoadFile(sURI);
				
		
			}else{
				pause();
			}						
		}


		public void previousTrack(){
			String sURI;
			stop();
			if (mPlaylistCursor!=null && !mPlaylistCursor.isFirst())
			{//play previous track
				mPlaylistCursor.moveToPrevious();			
				long audioID=mPlaylistCursor.getLong(
					mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
					);

				mCurrentPlaylistPosition=mPlaylistCursor.getPosition();
				sURI=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()+"/" + audioID;
				internalLoadFile(sURI);

			}else if (mPlaylistCursor!=null && mPlaylistCursor.isFirst())
			{//switch to last track

				mPlaylistCursor.moveToLast();
				long audioID=mPlaylistCursor.getLong(
					mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
					);

				mCurrentPlaylistPosition=mPlaylistCursor.getPosition();
				sURI=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()+"/" + audioID;
				internalLoadFile(sURI);



			}else{
				pause();
			}		
		}



		public void loadFile(String sFileUri){
			//mURI=ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,audioID);	
			invalidatePlaylist();
			internalLoadFile(sFileUri);
		
		}


		private void internalLoadFile(String sFileUri){
			Log.d(TAG,"trying to load file>"+sFileUri+"<");

			mURI=Uri.parse(sFileUri);
			if (engine==null)
			{
				engine=new MediaPlayerEngine(AudioPlayerService.this);
				engine.setPlayerEngineListener(AudioPlayerService.this);
			}

			engine.loadFileFromUri(mURI);
		}


		public void loadPlaylist(String sPlaylistUri){
			
			stop();
			
			invalidatePlaylist();
			String sUri;
			Log.d(TAG,"trying to load playlist>"+sPlaylistUri+"<");
			mPlaylistBaseURI = Uri.parse(sPlaylistUri);
			mPlaylistURI = Uri.withAppendedPath(mPlaylistBaseURI,"members");
			determinePlaylistPosition();

			Cursor t=getContentResolver().query(mPlaylistBaseURI,new String[]{Playlists.NAME},null,null,null);
			t.moveToFirst();
			mCurrentPlaylist=t.getString(0);
			t.close();

			mPlaylistCursor=getContentResolver().query(
				mPlaylistURI,
				mPlaylistProjection,
				null,null,
				Playlists.Members.PLAY_ORDER
				);
			//mPlaylistCursor.moveToFirst();
			mPlaylistCursor.moveToPosition(mCurrentPlaylistPosition);
			long audioID=mPlaylistCursor.getLong(
				mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
				);

			sUri=android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() +"/"+ audioID;
			this.internalLoadFile(sUri);

		}

		public boolean isPlaying(){
			if (engine!=null)
			{
				return engine.isPlaying();
			}
			return false;
		}

		public void invalidatePlaylist(){
			Log.d(TAG,"saving active playlist position");
			SharedPreferences prefs=getSharedPreferences(PREFS_NAME,0);
			SharedPreferences.Editor editor=prefs.edit();
			editor.putInt(PREFS_PREFIX+mPlaylistURI,mCurrentPlaylistPosition);
			editor.commit();

			Log.d(TAG,"invalidating Playlist. Hopefully Single File mode");
			mPlaylistBaseURI = null;
			mPlaylistURI = null;
			mPlaylistCursor=null;     
			mCurrentPlaylistPosition=-1;
		}

		public String[] getNext5(){
			if (mPlaylistCursor==null)
			{
				return null;
			}
			Log.d(TAG,"calculating next 5 tracks");		
			int cpos=mPlaylistCursor.getPosition();
			int count=0;
			String[] result=null;
			try
			{
			
			mPlaylistCursor.moveToPosition(cpos+1);

			//if (!mPlaylistCursor.isLast())
			//{	
				//return 5 or less entrys
				int len= Math.min(mPlaylistCursor.getCount()-1,5);
				result=new String[len];	

				int titleIndex=mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID);

				//TODO: OPTOMIZE THIS! (now i'm to tired, zzzzzz zero)
				while (!mPlaylistCursor.isAfterLast() && count<len)
				{
					long audioID=mPlaylistCursor.getLong(
						mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
						);

					Uri sUri=ContentUris.withAppendedId(
						android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						audioID
					);

					Cursor c= getContentResolver().query(sUri,mProjection,null,null,null);
					c.moveToFirst();

					int indexDATA = c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
					int indexTitle = c.getColumnIndex(android.provider.MediaStore.MediaColumns.TITLE);
					int indexArtist = c.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ARTIST);

					result[count] = c.getString(indexArtist) + "-" +c.getString(indexTitle);
					c.close();
					mPlaylistCursor.moveToNext();
					count++;
				}

			/*
			}else{
				result=new String[0];
			}
			*/
			//restore
			mPlaylistCursor.moveToPosition(cpos);

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Log.e(TAG,"error>"+ex.getMessage());
			}
			return result;
		}

		public String[] getPrevious5(){
			if (mPlaylistCursor==null)
			{
				return null;
			}
			Log.d(TAG,"calculating previous 5 tracks");		
			int cpos=mPlaylistCursor.getPosition();
			int count=0;
			String[] result=null;
			mPlaylistCursor.moveToPosition(cpos-1);
			//if (!mPlaylistCursor.isFirst())
		//	{
				int len= Math.min(cpos,5);
				Log.d(TAG,"cpos-1>"+(cpos-1)+"< len>"+len);		

				result=new String[len];	
				while (!mPlaylistCursor.isBeforeFirst() && count<len)
				{
					long audioID=mPlaylistCursor.getLong(
						mPlaylistCursor.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
						);

					Uri sUri=ContentUris.withAppendedId(
						android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						audioID
					);

					Cursor c= getContentResolver().query(sUri,mProjection,null,null,null);
					c.moveToFirst();

					int indexDATA = c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
					int indexTitle = c.getColumnIndex(android.provider.MediaStore.MediaColumns.TITLE);
					int indexArtist = c.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ARTIST);

					result[count] = c.getString(indexArtist) + "-" +c.getString(indexTitle);
					c.close();
	
					mPlaylistCursor.moveToPrevious();
					count++;
				}

/*
			}else{
				Log.d(TAG,"playing first track, nothing before");
				result=new String[0];
			}
			*/
			//restore
			mPlaylistCursor.moveToPosition(cpos);				

			return result;		
		}

		public String[] getStatus(){

			String[] res= new String[6];
			if (mPlaylistBaseURI!=null)
			{
				res[INFO_PLAYLIST_URI]	= mPlaylistBaseURI.toString();
			}else{

				res[INFO_PLAYLIST_URI]	="";
			}
			res[INFO_PLAYLIST_NAME]	= mCurrentPlaylist;
			res[INFO_PLAYLIST_POS]	= String.valueOf(mCurrentPlaylistPosition);
			res[INFO_TRACK_URI]		= mURI.toString();
			res[INFO_TRACK_TITLE]	= mCurrentTitle;
			res[INFO_TRACK_ARTIST]	= mCurrentArtist;
			Log.d(TAG,"returning status info>"+res);
			return res;

		}
		
		public int getTrackPosition(){
				if (engine == null) 
				{					
					return 0;										
					//mOnUpdateViewListener.onPlayPauseStateChange();
				} else {
					return engine.getCurrentPosition();
			
				}
		}
			


    };


	public void doPlay(){
		Log.d(TAG,"doPlay:: entering");
			if (engine!=null)
			{
				engine.playMedia();
			}
	}





	protected void determinePlaylistPosition(){
		SharedPreferences prefs=getSharedPreferences(PREFS_NAME,0);
		int ppos=prefs.getInt(PREFS_PREFIX+mPlaylistURI,0);
		Log.d(TAG,"retrived playlist position>"+ppos);
		//TODO: look up position in prefs if playlist has been run before
		mCurrentPlaylistPosition=ppos;
	}

	public void onPlayerPlay(Uri turi,String currentArtist,String currentTitle){
		Log.d(TAG,"received onPlayerPlay from Engine");
		String suri="";
		Log.d(TAG,"uri>"+turi);
		//if we get null values here, we're fucked. 
		//the call to the RemoteInterface will fail with unrecognazible errors
		if (turi!=null)
		{
			suri=turi.toString();
		}
		if (currentArtist==null)
		{
			this.mCurrentArtist="";
		}else{
			this.mCurrentArtist=currentArtist;
		}

		if (currentTitle==null)
		{
			this.mCurrentTitle="";
		}else{
			this.mCurrentTitle=currentTitle;
		}
		// Broadcast to all clients the new value.
		
		final int N = mCallbacks.beginBroadcast();
		for (int i=0; i<N; i++) {
			try {
			//	Log.d(TAG,"i>"+i+"< broadcastitem>"+mCallbacks.getBroadcastItem(i));
			//	Log.d(TAG,"2#i>"+i+"< broadcastitem>"+mCallbacks.getBroadcastItem(i));
				mCallbacks.getBroadcastItem(i)
				
				.onTrackChange(
					suri,
					mCurrentArtist,
					mCurrentTitle,
					mCurrentPlaylist,
					mCurrentPlaylistPosition
				);

				Log.d(TAG,"!!i>"+i+"< broadcastitem called! ");

			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
				Log.e(TAG,"REmoteException while broadcasting");
			}
		}
		mCallbacks.finishBroadcast();
		
		//now broadcast on play.
		final int M = mCallbacks.beginBroadcast();
		for (int i=0; i<M; i++) {
			try {
			//	Log.d(TAG,"i>"+i+"< broadcastitem>"+mCallbacks.getBroadcastItem(i));
			//	Log.d(TAG,"2#i>"+i+"< broadcastitem>"+mCallbacks.getBroadcastItem(i));
				mCallbacks.getBroadcastItem(i)
				
				.onAudioPlay();

				Log.d(TAG,"!!i>"+i+"< broadcastitem called! ");

			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
				Log.e(TAG,"REmoteException while broadcasting");
			}
		}
		mCallbacks.finishBroadcast();

		
	}
	public void onPlayerPause(){
		// Broadcast to all clients the new value.
		final int N = mCallbacks.beginBroadcast();
		for (int i=0; i<N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onAudioPause();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	
	}
	public void onPlayerStop(){}
	public void onPlayerReset(){}
    /**
     * Is called when the song reached is final position.
     */
    public void onPlayerCompletion() { 
    	Log.d(TAG, "onPlayerCompletion called");    	
		//this will move to next track if playlist is loaded
		try
		{
			mBinder.nextTrack();	
		}
		catch (android.os.RemoteException re)
		{
			Log.e(TAG,"onPlayerCompletion received RemoteException while calling internal Binder.nextTrack");
		}
		
    } 

	public void onIncrementalChange(int time){
		// Broadcast to all clients the new value.
		final int N = mCallbacks.beginBroadcast();
		for (int i=0; i<N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onPositionChange(time);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();

	}








	private boolean iwant2resume=false;

	private class CallHandler extends PhoneStateListener	{

		
		public void onCallStateChanged(int state,String incomingNumber){
                 Log.v(TAG, "onCallStateChanged(), Incoming Number: " +incomingNumber);

                super.onCallStateChanged(state, incomingNumber);

				try
				{
					

                switch (state) {
				// user hangup call
                case TelephonyManager.CALL_STATE_IDLE:
                        Log.i(TAG, "CALL_STATE_IDLE");
						if (AudioPlayerService.this.iwant2resume)
						{
							AudioPlayerService.this.mBinder.play();
						}
                        break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
				// user pickup call
                        Log.i(TAG, "CALL_STATE_OFFHOOK");
						if (AudioPlayerService.this.mBinder.isPlaying())
						{
							AudioPlayerService.this.iwant2resume=true;
							AudioPlayerService.this.mBinder.pause();
						}else{
							AudioPlayerService.this.iwant2resume=false;
						}
                        break;
                case TelephonyManager.CALL_STATE_RINGING:
				// imcoming call
                        Log.i(TAG, "CALL_STATE_RINGING");
						if (AudioPlayerService.this.mBinder.isPlaying())
						{
							AudioPlayerService.this.iwant2resume=true;
							AudioPlayerService.this.mBinder.pause();
						}else{
							AudioPlayerService.this.iwant2resume=false;
						}
						break;
                default:
                        Log.w(TAG, "unknown call state: " + state);
                } 


				}
				catch (android.os.RemoteException re)
				{//this should never happen.
					Log.e(TAG,"REMOTE EXCEPTION HERE? why?");
					re.printStackTrace();
				}

		}



	};

}







