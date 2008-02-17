package org.openintents.tags.content;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class HTTPUtils {

	private static final String TAG = "HTTPUtils";

	public static InputStream open(String url) throws Exception {
		HttpClient client = new HttpClient();

		GetMethod getMethod = new GetMethod(url);
		client.executeMethod(getMethod);
		InputStream in = getMethod.getResponseBodyAsStream();

		return in;
	}
	
	public static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}
}
