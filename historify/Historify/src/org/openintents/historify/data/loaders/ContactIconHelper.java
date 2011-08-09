/* 
 * Copyright (C) 2011 OpenIntents.org
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
 */

package org.openintents.historify.data.loaders;

import java.io.InputStream;
import java.util.Stack;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.ContactsAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.preferences.Pref.MyAvatar;
import org.openintents.historify.utils.UserIconHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.widget.ImageView;

/**
 * 
 * Class for loading and displaying icons of contacts asynchronously. Used for
 * lazy image loading by the {@link ContactsAdapter}.
 * 
 * @see <a
 *      href="https://github.com/thest1/LazyList">https://github.com/thest1/LazyList</a>
 * 
 * @author berke.andras
 */
public class ContactIconHelper {

	private Activity mContext;
	private TaskQueue mTaskQueue = new TaskQueue();
	private PhotosLoader mImageLoaderThread = new PhotosLoader();
	
	private int mDrawableResId;
	
	// Task for the image updater queue
	private class ImageLoadingTask {
		public final String lookupKey;
		public final ImageView imageView;

		public ImageLoadingTask(String lookupKey, ImageView imageView) {
			this.lookupKey = lookupKey;
			this.imageView = imageView;
		}
	}

	// Queue of image loading tasks
	private class TaskQueue {
		private Stack<ImageLoadingTask> photosToLoad = new Stack<ImageLoadingTask>();

		public void clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				if (photosToLoad.get(j).imageView == image)
					photosToLoad.remove(j);
				else
					++j;
			}
		}
	}

	// Thread responsible for loading the images as defined in the task queue
	private class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (mTaskQueue.photosToLoad.size() == 0)
						synchronized (mTaskQueue.photosToLoad) {
							mTaskQueue.photosToLoad.wait();
						}
					if (mTaskQueue.photosToLoad.size() != 0) {
						ImageLoadingTask photoToLoad;
						synchronized (mTaskQueue.photosToLoad) {
							photoToLoad = mTaskQueue.photosToLoad.pop();
						}

						Drawable icon = getIconDrawable(mContext,
								photoToLoad.lookupKey);

						Object tag = photoToLoad.imageView.getTag();
						if (tag != null
								&& ((String) tag).equals(photoToLoad.lookupKey)) {
							BitmapDisplayer bd = new BitmapDisplayer(icon,
									photoToLoad.imageView);
							mContext.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				// allow thread to exit
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Drawable drawable;
		ImageView imageView;

		public BitmapDisplayer(Drawable d, ImageView i) {
			drawable = d;
			imageView = i;
		}

		public void run() {
			if (drawable != null)
				imageView.setImageDrawable(drawable);
			else
				imageView.setImageResource(mDrawableResId);
		}
	}

	public ContactIconHelper(Activity context, int drawableResID) {

		mContext = context;
		mImageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		mDrawableResId = drawableResID;

	}

	public void loadContactIcon(Contact contact, ImageView imageView) {
		imageView.setTag(contact.getLookupKey());
		queuePhoto(contact.getLookupKey(), imageView);
	}

	public void stopThread() {
		mImageLoaderThread.interrupt();
	}

	private void queuePhoto(String lookupKey, ImageView imageView) {
		// This ImageView may be used for other images before. So there may be
		// some old tasks in the queue. We need to discard them.
		mTaskQueue.clean(imageView);
		ImageLoadingTask p = new ImageLoadingTask(lookupKey, imageView);
		synchronized (mTaskQueue.photosToLoad) {
			mTaskQueue.photosToLoad.push(p);
			mTaskQueue.photosToLoad.notifyAll();
		}

		// start thread if it's not started yet
		if (mImageLoaderThread.getState() == Thread.State.NEW)
			mImageLoaderThread.start();
	}

	/**
	 * For loading image synchronously.
	 */
	public static Drawable getIconDrawable(Context context, String lookupKey) {

		Drawable retval = null;

		try {

			Uri contentUri = Contacts.lookupContact(context
					.getContentResolver(), Contacts.CONTENT_LOOKUP_URI
					.buildUpon().appendPath(lookupKey).build());

			InputStream is = ContactsContract.Contacts
					.openContactPhotoInputStream(context.getContentResolver(),
							contentUri);

			if (is != null)
				return Drawable.createFromStream(is, "");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public static MyAvatar loadMyAvatar(Context context, ImageView imageView) {

		MyAvatar retval = MyAvatar.fromString(PreferenceManager.getInstance(context).getStringPreference(Pref.MY_AVATAR_ICON, Pref.DEF_AVATAR_ICON.toString()));
		
		if(retval==MyAvatar.defaultIcon) 
			imageView.setImageResource(R.drawable.contact_default_large);
		
		else {
			boolean succ = loadCustomAvatar(context, imageView);
			
			if(!succ) {
				retval = MyAvatar.defaultIcon;
				imageView.setImageResource(R.drawable.contact_default_large);
			}
			
		}
		
		return retval;
		
	}

	private static boolean loadCustomAvatar(Context context, ImageView imageView) {
		try {
			Bitmap b = new UserIconHelper().openIcon(context);
			if(b!=null) {
				imageView.setImageBitmap(b);
				return true;
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
		
	}

}
