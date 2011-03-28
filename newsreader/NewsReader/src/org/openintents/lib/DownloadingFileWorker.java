package org.openintents.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.openintents.lib.HTTPUtils.StreamAndHeader;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * usually used in a new Thread in activities with handler
 * 
 * @author Mueffke
 * 
 */
public class DownloadingFileWorker {
	private static final String TAG = "LoadingWorker";

	private String url;

	private Runnable mDoneCallback;


	private Handler mHandler;

	/** available for progress call back */
	public int mSize;
	public int mCount;

	/** available for done call back */
	public String mAbsolutePath;
	public int mSavedSize;

	public DownloadingFileWorker(String url) {
		this.url = url;
	}

	/**
	 * Download a web file to SD card.
	 * 
	 * @return Absolute path to newly created file, or null if something failed.
	 */
	public String downloadToSdCard(String localFileName,
			String suffixFromHeader, String extension) {
		InputStream in = null;
		FileOutputStream fos = null;
		String absolutePath = null;
		try {
			Log.i(TAG, "Opening URL: " + url);
			StreamAndHeader inAndHeader = HTTPUtils.openWithHeader(url,
					suffixFromHeader);

			if (inAndHeader == null || inAndHeader.mStream == null) {
				return null;
				// early exit
			}
			in = inAndHeader.mStream;

			// Obtain path to SD card from environment:
			String sdcardpath = android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath();

			String headerValue = suffixFromHeader == null
					|| inAndHeader.mHeaderValue == null ? ""
					: inAndHeader.mHeaderValue;
			headerValue = headerValue.replaceAll("[-:]*\\s*", "");
			String filename = sdcardpath + "/" + localFileName + headerValue
					+ (extension == null ? "" : extension);

			mSize = in.available();
			Log.i(TAG, "Downloading " + filename + ", size: " + mSize);

			fos = new FileOutputStream(new File(filename));

			/*
			 * byte[] buffer = new byte[size]; in.read(buffer);
			 * fos.write(buffer, 0, size);
			 */

			
			int buffersize = 1024;
			byte[] buffer = new byte[buffersize];
			int readsize = buffersize;
			mCount = 0;
			//while (readsize == buffersize) {
			while (readsize != -1) {
				readsize = in.read(buffer, 0, buffersize);
				if (readsize > 0) {
					Log.i(TAG, "Read " + readsize + " bytes...");
					fos.write(buffer, 0, readsize);
					mCount += readsize;
					// progress();
				}
			}
			

			fos.flush();
			fos.close();

			FileInputStream controlIn = new FileInputStream(filename);
			mSavedSize = controlIn.available();
			Log.v(TAG, "saved size: " + mSavedSize);

			// Finally we remember the file path:
			mAbsolutePath = filename;
			done();

		} catch (Exception e) {
			Log.e(TAG, "LoadingWorker.run", e);
		} finally {
			HTTPUtils.close(in);
		}
		return mAbsolutePath;

	}

	/**
	 * Download a web file to SD card.
	 * 
	 * @return Absolute path to newly created file, or null if something failed.
	 */
	public String downloadMediaToSdCard(String localFileName) {
		Log.i(TAG, "downloadMediaToSdCard");
		FileOutputStream fos = null;
		try {
			Log.i(TAG, "Opening URL: " + url);
			HttpEntity entity = HTTPUtils.getEntity(url);

			if (entity == null) {
				return null;
				// early exit
			}

			// Obtain path to SD card from environment:
			String sdcardpath = android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath();
			String filename = sdcardpath + "/" + localFileName;

			fos = new FileOutputStream(new File(filename));

			entity.writeTo(fos);

			Log.i(TAG, "Download done: " + filename + ", size: " + mSize);

			fos.flush();
			fos.close();

			FileInputStream controlIn = new FileInputStream(filename);
			mSavedSize = controlIn.available();
			Log.v(TAG, "saved size: " + mSavedSize);

			// Finally we remember the file path:
			mAbsolutePath = filename;
			done();

		} catch (Exception e) {
			Log.e(TAG, "LoadingWorker.run", e);
		}
		return mAbsolutePath;

	}

	/**
	 * Download a web file to private files.
	 * 
	 * @return Local(!) file name, or null if something failed.
	 */
	public String downloadMediaToPrivateFiles(Context context,
			String localFileName) {
		Log.i(TAG, "downloadMediaToPrivateFiles");
		FileOutputStream fos = null;
		String returnFileName = null;
		try {
			Log.i(TAG, "Opening URL: " + url);
			HttpEntity entity = HTTPUtils.getEntity(url);

			if (entity == null) {
				return null;
				// early exit
			}

			fos = context.openFileOutput(localFileName,
					Context.MODE_WORLD_READABLE);

			entity.writeTo(fos);

			Log.i(TAG, "Download done: " + localFileName);

			fos.flush();
			fos.close();

			// Finally we remember the file path:
			mAbsolutePath = context.getFileStreamPath(localFileName)
					.getAbsolutePath();

			FileInputStream controlIn = new FileInputStream(mAbsolutePath);
			mSavedSize = controlIn.available();
			Log.v(TAG, "saved size: " + mSavedSize);

			if (mSavedSize > 0) {
				// At least something is there, we assume that
				// the download was ok.
				returnFileName = localFileName;
			}

			done();

		} catch (Exception e) {
			Log.e(TAG, "LoadingWorker.run", e);
		}

		return returnFileName;

	}

	private void done() {
		if (mHandler != null && mDoneCallback != null) {
			mHandler.post(mDoneCallback);
		}
	}

	public void setHandler(Handler handler) {
		mHandler = handler;

	}

	public void setDoneCallback(Runnable done) {
		mDoneCallback = done;
	}
}
