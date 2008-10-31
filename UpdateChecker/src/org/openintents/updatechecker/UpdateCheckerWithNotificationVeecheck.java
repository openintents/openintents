package org.openintents.updatechecker;

import static com.tomgibara.android.veecheck.Veecheck.LOG_TAG;

import java.io.IOException;
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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public class UpdateCheckerWithNotificationVeecheck extends
		UpdateCheckerWithNotification {

	private static final int CONNECTION_TIMEOUT = 10 * 1000;

	private static final int SO_TIMEOUT = 10 * 1000;

	private static final Encoding DEFAULT_ENCODING = Encoding.UTF_8;

	private static final Pattern CHARSET = Pattern
			.compile("charset\\s*=\\s*([-_a-zA-Z0-9]+)");

	public UpdateCheckerWithNotificationVeecheck(Context context,
			String packageName, String appName, int currentVersionCode,
			String currentVersionName) {
		super(context, packageName, appName, currentVersionCode,
				currentVersionName);
	}

	public void checkForUpdate(String uri) {
		VeecheckResult result = null;
		try {
			VeecheckVersion version = new VeecheckVersion(mPackageName, String
					.valueOf(mCurrentVersion), mCurrentVersionName);
			try {
				result = performRequest(version, uri);
			} catch (Exception e) {
				Log.w(LOG_TAG, "Failed to process versions.", e);
				return;
			} finally {
			}

			if (result.matched) {
				Log.d(LOG_TAG, "Matching intent found.");
				mLatestVersion = Integer.parseInt(result.latestVersion
						.getVersionCode());
				// TODO create better comment
				mComment = result.latestVersion.getVersionName();
				
				// create intent
				Intent intent = new Intent();
				intent.setAction(result.action);
				if (result.data != null) {
					Uri intentUri = Uri.parse(result.data);
					intent.setData(intentUri);
				}
				intent.setType(result.type);
				if (result.extras != null) {
					for (Entry<String, String> e : result.extras.entrySet()) {
						intent.putExtra(e.getKey(), e.getValue());
					}
				}
				
				ResolveInfo info = mContext.getPackageManager().resolveActivity(intent, 0);
				if (info != null){
					mUpdateIntent = intent;
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
