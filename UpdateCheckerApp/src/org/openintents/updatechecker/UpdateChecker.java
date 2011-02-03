/*
 * Copyright (C) 2008  OpenIntents.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openintents.updatechecker;

import static com.tomgibara.android.veecheck.Veecheck.LOG_TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.xml.sax.SAXException;

import com.tomgibara.android.veecheck.VeecheckResult;
import com.tomgibara.android.veecheck.VeecheckVersion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public class UpdateChecker {
	protected static final String TAG = "UpdateChecker";
	public static final String EXTRA_LATEST_VERSION = "latest_version";
	public static final String EXTRA_COMMENT = "comment";
	public static final String EXTRA_PACKAGE_NAME = "package_name";
	public static final String EXTRA_APP_NAME = "app_name";
	public static final String EXTRA_CURRENT_VERSION = "currrent_version";
	public static final String EXTRA_CURRENT_VERSION_NAME = "current_version_name";
	public static final String EXTRA_VEECHECK = "veecheck";
	public static final String EXTRA_UPDATE_INTENT = "update_intent";
	public static final String EXTRA_LATEST_VERSION_NAME = "latest_version_name";

	private int mLatestVersion;
	private String mLatestVersionName;
	private String mComment;
	private String mNewApplicationId;
	private Intent mUpdateIntent;

	private static final int CONNECTION_TIMEOUT = 10 * 1000;

	private static final int SO_TIMEOUT = 10 * 1000;

	private static final Encoding DEFAULT_ENCODING = Encoding.UTF_8;

	private static final Pattern CHARSET = Pattern
			.compile("charset\\s*=\\s*([-_a-zA-Z0-9]+)");

	private Context mContext;
	private String mPackageName;
	private int mCurrentVersion;
	private String mCurrentVersionName;

	public UpdateChecker(Context context, String packageName,
			int currentVersion, String currentVersionName) {
		mContext = context;
		mPackageName = packageName;
		mCurrentVersion = currentVersion;
		mCurrentVersionName = currentVersionName;
	}

	public void checkForUpdate(String link) {

		mLatestVersion = -1;
		mComment = null;
		mNewApplicationId = null;
		mLatestVersionName = null;

		try {
			Log.d(TAG, "Looking for version at " + link);

			if (mLatestVersion > 0 || mLatestVersionName != null) {
				return;
			} else {
				URL u = new URL(link);
				URLConnection connection = u.openConnection();
				connection.setReadTimeout(CONNECTION_TIMEOUT);
				Object content = connection.getContent();
				if (content instanceof InputStream) {
					InputStream is = (InputStream) content;

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));

					String firstLine = reader.readLine();
					if (firstLine != null && firstLine.indexOf("<") >= 0) {

						parseVeeCheck(link);
					} else {
						parseTxt(firstLine, reader);
					}

				} else {
					Log.d(TAG, "Unknown server format: "
							+ ((String) content).substring(0, 100));
				}
			}
		} catch (MalformedURLException e) {
			Log.v(TAG, "MalformedURLException", e);
		} catch (IOException e) {
			Log.v(TAG, "IOException", e);
		} catch (Exception e) {
			Log.v(TAG, "Exception", e);
		}

	}

	private void parseTxt(String firstLine, BufferedReader reader)
			throws IOException {

		mLatestVersion = (firstLine != null ? Integer.parseInt(firstLine) : 0);
		Log.d(TAG, "Lastest version available: " + mLatestVersion);

		mNewApplicationId = reader.readLine();
		Log.d(TAG, "New version application ID: " + mNewApplicationId);

		mComment = reader.readLine();
		Log.d(TAG, "comment: " + mComment);
	}

	public int getLatestVersion() {
		return mLatestVersion;
	}

	public String getApplicationId() {
		return mNewApplicationId;
	}

	public String getComment() {
		return mComment;
	}

	public String getLatestVersionName() {
		return mLatestVersionName;
	}

	public Intent getUpdateIntent() {
		return mUpdateIntent;
	}

	public int getCurrentVersion() {
		return mCurrentVersion;
	}

	public String getCurrentVersionName() {
		return mCurrentVersionName;
	}

	public void setMarketUpdateIntent(String packageName, String appName) {

		if (mUpdateIntent == null) {
			mUpdateIntent = new Intent(Intent.ACTION_VIEW);
			if (packageName != null) {
				mUpdateIntent.setData(Uri.parse("market://search?q=pname:"
						+ packageName));
			} else if (getApplicationId() != null) {
				mUpdateIntent.setData(Uri.parse("market://details?id="
						+ getApplicationId()));
			} else if (appName != null) {
				mUpdateIntent
						.setData(Uri.parse("market://search?q=" + appName));
			} else {
				// TODO
			}
		}

	}

	public void parseVeeCheck(String uri) {
		VeecheckResult result = null;
		try {
			VeecheckVersion version = new VeecheckVersion(mPackageName, String
					.valueOf(mCurrentVersion), mCurrentVersionName);
			try {
				result = performRequest(version, uri);
			} catch (Exception e) {
				Log.v(LOG_TAG, "Failed to process versions.", e);
				return;
			} finally {
			}

			if (result.matched) {
				Log.d(LOG_TAG, "Matching intent found.");
				if (result.latestVersion.getVersionCode() != null) {
					try {
						mLatestVersion = Integer.parseInt(result.latestVersion
								.getVersionCode());
					} catch (NumberFormatException e) {
						mLatestVersion = 0;
					}
				} else {
					mLatestVersion = 0;
				}
				mLatestVersionName = result.latestVersion.getVersionName();
				mComment = null;

				// create intent
				Intent intent = new Intent();
				if (Intent.ACTION_VIEW.equals(result.action)) {
					intent.setAction(result.action);
					if (result.data != null) {
						Uri intentUri = Uri.parse(result.data);
						if (result.type != null) {
							intent.setDataAndType(intentUri, result.type);
						} else {
							intent.setData(intentUri);
						}
					} else {
						if (result.type != null) {
							intent.setType(result.type);
						}
					}

					if (result.extras != null) {
						for (Entry<String, String> e : result.extras.entrySet()) {
							intent.putExtra(e.getKey(), e.getValue());
						}
					}
					ResolveInfo info = mContext.getPackageManager()
							.resolveActivity(intent, 0);
					if (info != null) {
						mUpdateIntent = intent;
					}
				} else {
					Log.v(TAG, "no view action but " + result.action);
				}

			} else {
				result = null;
				Log.d(LOG_TAG, "No matching intent found.");
			}

		} finally {

		}
	}

	public VeecheckResult performRequest(VeecheckVersion version, String uri)
			throws ClientProtocolException, IOException, IllegalStateException,
			SAXException {
		HttpClient client = new DefaultHttpClient();
		// TODO ideally it should be possible to adjust these constants
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
		HttpGet request = new HttpGet(version.substitute(uri));
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		try {
			StatusLine line = response.getStatusLine();
			// TODO this is lazy, we should consider other codes here
			if (line.getStatusCode() != 200)
				throw new IOException("Request failed: "
						+ line.getReasonPhrase());
			Header header = response.getFirstHeader(HTTP.CONTENT_TYPE);
			Encoding encoding = identityEncoding(header);
			VeecheckResult handler = new VeecheckResult(version);
			Xml.parse(entity.getContent(), encoding, handler);
			return handler;
		} finally {
			entity.consumeContent();
		}
	}

	private Encoding identityEncoding(Header header) {
		if (header == null)
			return DEFAULT_ENCODING;
		String value = header.getValue();
		Matcher matcher = CHARSET.matcher(value);
		if (!matcher.find())
			return DEFAULT_ENCODING;
		String charset = matcher.group(1).replace("_", "").replace("-", "")
				.toUpperCase();
		// we don't construct a static map for these
		// it will only get torn down when the application terminates
		if (charset.equals("UTF8"))
			return Encoding.UTF_8;
		if (charset.equals("USASCII"))
			return Encoding.US_ASCII;
		if (charset.equals("ASCII"))
			return Encoding.US_ASCII;
		if (charset.equals("ISO88591"))
			return Encoding.ISO_8859_1;
		if (charset.equals("UTF16"))
			return Encoding.UTF_16;
		return DEFAULT_ENCODING;
	}

}