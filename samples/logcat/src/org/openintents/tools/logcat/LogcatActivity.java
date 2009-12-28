package org.openintents.tools.logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class LogcatActivity extends Activity
{
	public static final String TAG = "LogcatActivity";

	protected ScrollView mScrollView;
	protected LinearLayout mLines;
	protected Process mLogcatProc;

	protected LogcatProcessor mLogcatter;
	protected LogcatContext mContext = new LogcatContext(5);

	public static final int MAX_LINES = 250;
	
	private static final int MENU_SEND_LOG = Menu.FIRST;
	private static final int MENU_SAVE_LOG = Menu.FIRST + 1;
	
//	private static final int MSG_ERROR = 0;
	private static final int MSG_NEWLINE = 1;
	private static final int MSG_SHOW_DIALOG = 2;
	
	private static final int DIALOG_SEND_LOGCAT = 1;
	
	private String lastErrorPackage = null;
    private String pid = null;
	private String crashedApplicationName = null;
	
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
//			case MSG_ERROR:
//				handleMessageError(msg);
//				break;
			case MSG_NEWLINE:
				handleMessageNewline(msg);
				break;
			case MSG_SHOW_DIALOG:
				showDialog(DIALOG_SEND_LOGCAT);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	/* TODO: Scrolling needs a lot of work.  Might not be worth it, though. */
	private void handleMessageNewline(Message msg)
	{
		String line = (String)msg.obj;
		
		checkForError(line);
		
		final boolean autoscroll = 
		  (mScrollView.getScrollY() + mScrollView.getHeight() >= mLines.getBottom()) ? true : false;
			
		TextView lineView = new TextView(LogcatActivity.this);
		lineView.setTypeface(Typeface.MONOSPACE);
		lineView.setText(new LoglineFormattedString(line));

		mContext.addLine(line);
		mLines.addView(lineView, new LayoutParams(LayoutParams.FILL_PARENT,
		  LayoutParams.WRAP_CONTENT));

		if (mLines.getChildCount() > MAX_LINES)
			mLines.removeViewAt(0);

		/* Lame trick so our code runs after mLines#onMeasure.  Ideally we
		 * should use a custom widget here. */
		mScrollView.post(new Runnable() {
			public void run()
			{
				if (autoscroll == true)
				{
					mScrollView.scrollTo(0,
					  mLines.getBottom() - mScrollView.getHeight());
				}
			}
		});    				
	}
	
	@Override
	public void onCreate(Bundle icicle)
	{
	    super.onCreate(icicle);
	    setContentView(R.layout.main);
	
	    mScrollView = (ScrollView)findViewById(R.id.scrollView);
	    mLines = (LinearLayout)findViewById(R.id.lines);
	    
	    super.onCreate(icicle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_SEND_LOG, 0, "Send Log").setIcon(android.R.drawable.ic_menu_send);
		menu.add(0, MENU_SAVE_LOG, 0, "Save Log").setIcon(android.R.drawable.ic_menu_save);

		return true;
	}
	
	private String makeLogFilename()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-kkmmss");
		String name = "/sdcard/logcat-" + fmt.format(new Date()) + ".log";
		
		try {
			File f = new File(name);
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return name;
	}

	private String saveCurrentLog()
	  throws IOException
	{
		String filename = makeLogFilename();
		
		BufferedWriter writer = null;

		try
		{
			writer = new BufferedWriter(new FileWriter(filename), 2048);

			int n = mLines.getChildCount();

			for (int i = 0; i < n; i++)
			{
				TextView lineView = (TextView)mLines.getChildAt(i);
				String line = lineView.getText().toString();

				writer.write(line, 0, line.length());
				writer.newLine();
			}
		}
		finally
		{
			if (writer != null)
				try { writer.close(); } catch (IOException e) {}	
		}
		
		return filename;
	}
	
	private void menuSaveCurrentLog()
	{
		try
		{
			String filename = saveCurrentLog();
			
			Toast.makeText(this, "Wrote " + filename,
			  Toast.LENGTH_LONG).show();
		}
		catch (IOException e)
		{
			Toast.makeText(this, "Failed writing log: " + e.toString(),
			  Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case MENU_SEND_LOG:
			sendCurrentInfo();
			return true;
		case MENU_SAVE_LOG:
			menuSaveCurrentLog();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart()
	{
		mLogcatter = new LogcatProcessor()
		{
			public void onError(final String msg, Throwable e)
			{
				runOnUiThread(new Runnable() {
					public void run()
					{
						Toast.makeText(LogcatActivity.this, msg,
						  Toast.LENGTH_LONG).show();
					}
				});
			}
	
			public void onNewline(String line)
			{
				Message msg = mHandler.obtainMessage(MSG_NEWLINE);
				msg.obj = line;
				mHandler.sendMessage(msg);
			}
		};
	
		mLogcatter.start();
	
		super.onStart();
	}
	
	@Override
	public void onStop()
	{
		mLogcatter.stopCatter();
		mLogcatter = null;
	
		super.onStop();
	}
	
	/**
	 * Simple class to help keep context across multiple logcat invocations 
	 * (between onStop and onStart).  Similar to the way that the unidiff
	 * patch format works to keep context.
	 */
	private static class LogcatContext
	{
		int mPrec;
		int mPos;
		String[] mLastLines;
		int mLastLineCount;
	
		public LogcatContext(int precision)
		{
			mPrec = precision;
			mPos = mLastLineCount = 0;
			mLastLines = new String[precision * 2];
		}
	
		public void addLine(String line)
		{
			/* TODO */
		}
	}
	
	/* 
	 * Format a logcat line of the form:
	 * 
	 *   L/tag(????): Message
	 */
	private static class LoglineFormattedString extends SpannableString
	{
		public static final HashMap<Character, Integer> LABEL_COLOR_MAP; 
		
		public LoglineFormattedString(String line)
		{
			super(line);
			
			try
			{
				/* We expect at least "L/f:" */
				if (line.length() <= 4)
					throw new RuntimeException();
				
				/* And the log-level label must be exactly 1 character. */
				if (line.charAt(1) != '/')
					throw new RuntimeException();
				
				Integer labelColor = LABEL_COLOR_MAP.get(line.charAt(0));
				
				if (labelColor == null)
					labelColor = LABEL_COLOR_MAP.get('E');

				setSpan(new ForegroundColorSpan(labelColor), 0, 1, 0);
				setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);

				int leftIdx;

				if ((leftIdx = line.indexOf(':', 2)) >= 0)
				{
					setSpan(new ForegroundColorSpan(labelColor), 2, leftIdx, 0);
					setSpan(new StyleSpan(Typeface.ITALIC), 2, leftIdx, 0);
				}
			}
			catch (RuntimeException e) /* Lazy FormatException */
			{
				setSpan(new ForegroundColorSpan(0xffddaacc), 0, length(), 0);
			}
		}
		
		static
		{
			LABEL_COLOR_MAP = new HashMap<Character, Integer>();
			LABEL_COLOR_MAP.put('D', 0xff9999ff);
			LABEL_COLOR_MAP.put('V', 0xffcccccc);
			LABEL_COLOR_MAP.put('I', 0xffeeeeee);
			LABEL_COLOR_MAP.put('E', 0xffff9999);
			LABEL_COLOR_MAP.put('W', 0xffffff99);
		}
	}

	void sendCurrentInfo() {
		
		String mSendText = "";
		mSendText = "Dear OpenIntents support team,\n\n";
		if (!TextUtils.isEmpty(crashedApplicationName)) {
			mSendText += "The application " + crashedApplicationName + " crashed.\n\n";
		}
		mSendText += "Please find below the system log ";
		mSendText += "together with device-specific information.\n\n";
		mSendText += "<insert additional comments>\n\n";
		mSendText += "Best,\n<insert your name if you want>\n\n";
		mSendText += "-----\nDevice information:\n";
		mSendText += getBuildInfo();
		mSendText += "\n";
		mSendText += "-----\nLogcat output:\n";
		mSendText += getLogcatOutput();
		mSendText += "\n";
		
		// Send
		mSendText += "-----\n";
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		//sendIntent.setData(Uri.parse("mailto:support@openintents.org"));
		sendIntent.putExtra(Intent.EXTRA_TEXT, mSendText);
		if (!TextUtils.isEmpty(crashedApplicationName)) {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Logcat for " + crashedApplicationName + " (" + Build.MODEL + ")");
		} else {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Logcat (" + Build.MODEL + ")");
		}
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@openintents.org"});
		sendIntent.setType("message/rfc822");
		startActivity(Intent.createChooser(sendIntent, "Send message:"));
		
		lastErrorPackage = null;
		pid = null;
		crashedApplicationName = null;
	}
	
    static String getBuildInfo() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Model: ");
    	sb.append(Build.MODEL);
    	sb.append("\nBrand: ");
    	sb.append(Build.BRAND);
    	sb.append("\nProduct: ");
    	sb.append(Build.PRODUCT);
    	sb.append("\nDevice: ");
    	sb.append(Build.DEVICE);
    	sb.append("\nBoard: ");
    	sb.append(Build.BOARD);
    	sb.append("\nDisplay: ");
    	sb.append(Build.DISPLAY);
    	sb.append("\nID: ");
    	sb.append(Build.ID);
    	sb.append("\nVersion: ");
    	sb.append(Build.VERSION.RELEASE);
    	sb.append("\nVersion incremental: ");
    	sb.append(Build.VERSION.INCREMENTAL);
    	sb.append("\nSDK version: ");
    	sb.append(Build.VERSION.SDK);
    	//sb.append("\nFingerprint: ");
    	//sb.append(Build.FINGERPRINT);
    	//sb.append("\nHost: ");
    	//sb.append(Build.HOST);
    	//sb.append("\nTags: ");
    	//sb.append(Build.TAGS);
    	//sb.append("\nTime: ");
    	//sb.append(Build.TIME);
    	//sb.append("\nType: ");
    	//sb.append(Build.TYPE);
    	//sb.append("\nUser: ");
    	//sb.append(Build.USER);
    	return sb.toString();
    }
    
    String getLogcatOutput() {
    	StringBuffer sb = new StringBuffer();
    	
		int n = mLines.getChildCount();

		for (int i = 0; i < n; i++)
		{
			TextView lineView = (TextView)mLines.getChildAt(i);
			String line = lineView.getText().toString();
			sb.append(line);
			sb.append("\n");
		}
		
		return sb.toString();
    }
    
    /**
     * Check whether line contains an error message:
     * @param line
     */
    void checkForError(String line) {
    	// First we look for a line that may look like this:
    	// E/Wall    ( 8765): at org.openintents.test.crash.TestCrashActivity(TestCrashActivity.java:50)
    	if (line.charAt(0) == 'E') {

    		// Extract pid, which is contained in brackets:
    		int x1 = line.indexOf("(");
    		int x2 = line.indexOf(")");
    		
        	// Here we only extract the pid ('8765' in the example above).
    		pid = line.substring(x1 + 1, x2).trim();
    	}
    	
    	// Then we look for another line that may look like this:
    	// I/ActivityManager(   74): Process org.openintents.test.crash (pid 8765) has died.
    	if (pid != null && line.endsWith(" has died.") && line.contains(pid)) {
    		// Retrieve package name from process that has died with same pid as obtained above in
    		// an error message:
    		String l = line.substring(line.indexOf(':'));
    		String s = l.substring("Process ".length() + 2, l.length() - " has died.".length());
    		
    		// Extract package name before pid in bracket starts:
    		lastErrorPackage = s.substring(0, s.indexOf("(") - 1);
    		
    		// Delete any previous messages:
    		mHandler.removeMessages(MSG_SHOW_DIALOG);
    		
    		// Show dialog after a delay (if no other error message is found in the mean-time)
    		mHandler.sendEmptyMessageDelayed(MSG_SHOW_DIALOG, 1000);
    	}
    	
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SEND_LOGCAT:
			
    		PackageManager pm = getPackageManager();
    		ApplicationInfo info = null;
    		try {
    			info = pm.getApplicationInfo(lastErrorPackage, 0);
    		} catch (NameNotFoundException e) {
    		}
			
    		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("Send error message?")
			.setPositiveButton(
					"Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							sendCurrentInfo();
						}
					})
			.setNegativeButton("No", null
					);
    		
    		
    		if (info != null) {
    			crashedApplicationName = pm.getApplicationLabel(info).toString();
    			builder.setMessage("Application " + 
    					crashedApplicationName + " crashed. Would you like to send "
    	    			+ "the system log to support?");
    			builder.setIcon(pm.getApplicationIcon(info));
    		} else {
    			builder.setMessage("An application crashed. Would you like to send "
    	    			+ "the system log to support?");
    			builder.setIcon(android.R.drawable.ic_dialog_alert);
    		}
    		 
    		return builder.create();
		}
		return super.onCreateDialog(id);
	}
    
    
}
