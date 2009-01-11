package org.openintents.distribution;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;

public class GetFileManagerFromMarketDialog extends AlertDialog implements OnClickListener {

	private static final String TAG = "GetFromMarketDialog";

    Context mContext;

    public GetFileManagerFromMarketDialog(Context context) {
        super(context);
        mContext = context;

        //setTitle(context.getText(R.string.menu_edit_tags));
        setMessage(mContext.getText(RD.string.filemanager_not_available));
    	setButton(mContext.getText(RD.string.filemanager_get_oi_filemanager), this);
        
    }

    
    @Override
	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON1) {
    		Uri uri = Uri.parse(mContext.getString(RD.string.filemanager_market_uri));
    		
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			GetFromMarket.startSaveActivity(mContext, intent);
    	}
		
	}
}
