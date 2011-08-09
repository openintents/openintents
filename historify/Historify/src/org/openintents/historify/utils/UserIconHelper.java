package org.openintents.historify.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class UserIconHelper {

	private static final String FILE_NAME = "user.icon";
	
	public Bitmap openIcon(Context context) {
		try {
			return BitmapFactory.decodeStream(context.openFileInput(FILE_NAME));	
		} catch(Exception e) {
			return null;
		}
		
	}
	
	public boolean saveIcon(Context context, String sourceFile) {
	
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(sourceFile,options);
			
			int w = options.outWidth;
			if(w==-1) return false;
			
			options.inJustDecodeBounds = false;
			
			if(w>200) {
				options.inSampleSize = 4;
			}
			
			Bitmap b = BitmapFactory.decodeFile(sourceFile,options);
			
			if(b==null) return false;
			
			
			FileOutputStream fos = context.openFileOutput(FILE_NAME, Activity.MODE_PRIVATE);
			b.compress(CompressFormat.PNG, 100, fos);
			fos.close();
			
			Log.v("bw","  "+b.getWidth());
			
			return true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public void delete(Context context) {
		
		File f = context.getFileStreamPath(FILE_NAME);
		f.delete();
	}
}
