package org.openintents.enumclient;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class EnumLookup extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		try {
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			Selector selector = Selector.open();
			channel.register(selector, 0);
		} catch (IOException e) {
			Log.e("enum", "error", e);

		}

		Lookup l;
		try {
			l = new Lookup("0.2.2.2.8.9.0.6.0.3.9.4.e164.arpa", Type.ANY);
			Lookup.getDefaultResolver().setTCP(true);
			l.run();
			if (l.getAnswers() != null) {
				for (Record r : l.getAnswers()) {
					Log.i("enum", r.toString());
				}
			} else {
				Log.e("enum", "no result");
			}

		} catch (TextParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}