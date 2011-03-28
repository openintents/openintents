package org.openintents.newsreader;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class Application extends android.app.Application {
	static {
		Calendar c= GregorianCalendar.getInstance();
		c.set(2009, 2, 1);
		deadline = c.getTimeInMillis();
	}

	public final static long deadline;
	
	public static void checkDeadline(final Activity context){
		if (System.currentTimeMillis() > Application.deadline){
			Builder b = new AlertDialog.Builder(context).
			setTitle(R.string.timeout).setMessage(R.string.timeout_long).
			setPositiveButton(R.string.ok, new OnClickListener(){

				public void onClick(DialogInterface arg0, int arg1) {
					context.finish();
					
				}
				
			});
			b.show();			
		}
	}
}
