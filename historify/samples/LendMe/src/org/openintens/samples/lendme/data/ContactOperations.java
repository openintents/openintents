package org.openintens.samples.lendme.data;

import java.util.ArrayList;

import org.openintens.samples.lendme.ItemsActivity;
import org.openintens.samples.lendme.R;
import org.openintens.samples.lendme.Toaster;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RemoteViews.ActionException;

public class ContactOperations {

	public static String loadContactName(ContentResolver resolver, String contactKey) {
		
		Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,contactKey);
		String name = null;
		
		Cursor c = resolver.query(lookupUri, new String[] {Contacts.DISPLAY_NAME}, null, null,null);
		if(c.moveToFirst()) {
			name = c.getString(0);
		}
		c.close();
		
		return name;
	}

	public static String[] loadContactPhones(ContentResolver resolver, String contactKey) {
		
		String phoneSelection = Phone.LOOKUP_KEY + " = '" + contactKey + "'";

		// querying phone numbers of the contact
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				new String[] { Phone.NUMBER }, phoneSelection, null, null);

		String[] retval = new String[phoneCursor.getCount()];
		
		int i=0;
		while (phoneCursor.moveToNext()) {
			retval[i]=phoneCursor.getString(0);
			i++;
		}

		phoneCursor.close();

		return retval;
	}

	public static void displaySmsSender(final Context context, final String phoneNumber, Item item) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.main_reminder_title);
		
		final EditText editMsg = new EditText(context);
		editMsg.setPadding(5,5,5,5);
		editMsg.setText(String.format(context.getString(R.string.main_reminder_text), item.getName()));
		editMsg.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		editMsg.setImeOptions(EditorInfo.IME_ACTION_DONE);
		editMsg.setLines(4);
		editMsg.setGravity(Gravity.TOP);
		builder.setView(editMsg);
		
		builder.setNegativeButton(R.string.main_reminder_cancel, null);
		
		builder.setPositiveButton(R.string.main_reminder_send, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendSms(context, editMsg.getText().toString(),phoneNumber);
			}
		});
		
		builder.show();
		
	}

	protected static void sendSms(Context context, String text, String phoneNumber) {

		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(text);
		for(String part : parts) {
			
			//dummy intents
			PendingIntent sent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
			PendingIntent delivered = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
			
			//send sms
			smsManager.sendTextMessage(phoneNumber, null, part, sent, delivered);
		}
		
	}
}
