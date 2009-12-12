package org.openintents.tools.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class LogcatProcessor extends Thread
{
	/* TODO: Support logcat filtering. */
	public static final String[] LOGCAT_CMD = new String[] { "logcat" };
	private static final int BUFFER_SIZE = 1024;

	private int mLines = 0;
	protected Process mLogcatProc = null;

	public void run()
	{
		try
		{
			mLogcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);
		}
		catch (IOException e)
		{
			onError("Can't start " + LOGCAT_CMD[0], e);
			return;
		}
		
		BufferedReader reader = null;

		try
		{
			reader =
			  new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()),
			    BUFFER_SIZE);

			String line;
			
			while ((line = reader.readLine()) != null)
			{
				onNewline(line);
				mLines++;
			}
			
		}
		catch (IOException e)
		{
			onError("Error reading from process " + LOGCAT_CMD[0], e);
		}
		finally
		{
			if (reader != null)
				try { reader.close(); } catch (IOException e) {}
				
			stopCatter();
		}
	}

	public void stopCatter()
	{
		if (mLogcatProc == null)
			return;
		
		mLogcatProc.destroy();
		mLogcatProc = null;
	}
	
	public int getLineCount()
	{
		return mLines;
	}

	public abstract void onError(String msg, Throwable e);
	public abstract void onNewline(String line);
}
