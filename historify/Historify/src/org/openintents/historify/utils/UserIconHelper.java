package org.openintents.historify.utils;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

/**
 * 
 * Helper class for storing and retrieving the user's avatar image provided by
 * the PreferencesFragment.
 * 
 * @author berke.andras
 * 
 */
public class UserIconHelper {

	private static final String FILE_NAME = "user.icon";

	public Bitmap openIcon(Context context) {
		try {
			return BitmapFactory.decodeStream(context.openFileInput(FILE_NAME));
		} catch (Exception e) {
			return null;
		}

	}

	public boolean saveIcon(Context context, String sourceFile) {

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(sourceFile, options);

			int w = options.outWidth;
			if (w == -1)
				return false;

			options.inJustDecodeBounds = false;

			if (w > 200) {
				options.inSampleSize = 4;
			}

			Bitmap b = BitmapFactory.decodeFile(sourceFile, options);

			if (b == null)
				return false;

			FileOutputStream fos = context.openFileOutput(FILE_NAME,
					Activity.MODE_PRIVATE);
			b.compress(CompressFormat.PNG, 100, fos);
			fos.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void deleteIcon(Context context) {

		File f = context.getFileStreamPath(FILE_NAME);
		f.delete();
	}
}
