package org.openintents.countdown;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class NotificationReceiverActivity extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countdown_notificationreceiver);
        
        Intent i = getIntent();
        Uri uri = i.getData();
        int notification_id = Integer.parseInt(uri.getLastPathSegment());
        
        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // cancel the notification that we started in IncomingMessage
        nm.cancel(notification_id);
    }
}
