package org.openintents.tools.logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
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
	
	private static final int MENU_SAVE_LOG = Menu.FIRST;
	
//	private static final int MSG_ERROR = 0;
	private static final int MSG_NEWLINE = 1;
	
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
			default:
				super.handleMessage(msg);
			}
		}
	};

	/* TODO: Scrolling needs a lot of work.  Might not be worth it, though. */
	private void handleMessageNewline(Message msg)
	{
		String line = (String)msg.obj;
		
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
}
