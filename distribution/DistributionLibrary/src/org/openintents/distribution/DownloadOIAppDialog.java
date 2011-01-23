package org.openintents.distribution;

import android.content.Context;

public class DownloadOIAppDialog extends DownloadAppDialog {

	public static final int OI_FILEMANAGER = 1;
	
	public DownloadOIAppDialog(Context context, int appId) {
		super(context);
		
		switch(appId) {
		case OI_FILEMANAGER:
			set(R.string.oi_distribution_filemanager_not_available,
			R.string.oi_distribution_filemanager,
			R.string.oi_distribution_filemanager_package,
			R.string.oi_distribution_filemanager_website);
			break;
		}
	}
}
