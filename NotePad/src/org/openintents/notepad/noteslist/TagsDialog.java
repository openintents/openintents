package org.openintents.notepad.noteslist;

import org.openintents.intents.CryptoIntents;
import org.openintents.notepad.NotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.crypto.EncryptActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class TagsDialog extends AlertDialog implements OnClickListener {
	private static final String TAG = "TagsDialog";

    private static final String BUNDLE_TAGS = "tags";
    
    Context mContext;
    Uri mUri;
    long mEncrypted;
    
    MultiAutoCompleteTextView mTextView;
    String[] mTagList;
    
    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public TagsDialog(Context context) {
        super(context);
        mContext = context;
        
        setTitle(context.getText(R.string.menu_edit_tags));
        setButton(context.getText(android.R.string.ok), this);
        setButton2(context.getText(android.R.string.cancel), (OnClickListener) null);
        setIcon(R.drawable.ic_menu_edit);
        
        LayoutInflater inflater = 
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_edit_tags, null);
        setView(view);

        mTextView = (MultiAutoCompleteTextView) view.findViewById(R.id.edit);
        mTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mTextView.setThreshold(0);
        mTextView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (mTextView.isPopupShowing()) {
					mTextView.dismissDropDown();
				} else {
					mTextView.showDropDown();
				}
			}
		});
        String[] mTagList = new String[0];
		if (mTagList.length < 1) {
			mTextView.setHint(R.string.tags_hint);
		}
    }
    
    public void setUri(Uri uri) {
    	mUri = uri;
    }
    
    public void setTagList(String[] taglist) {
    	mTagList = taglist;

	    if (taglist != null) {
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
	                android.R.layout.simple_dropdown_item_1line, mTagList);
	        mTextView.setAdapter(adapter);
	    }
    }
    
    public void setTags(String tags) {
    	mTextView.setText(tags);
    }
    
    public void setEncrypted(long encrypted) {
    	mEncrypted = encrypted;
    }
    
    @Override
	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON1) {
    		saveTags();
    	}
		
	}
    
    void saveTags() {
    	if (mTextView == null) {
    		Log.e(TAG, "mTextView is null.");
    		return;
    	}
    	
    	String tags = mTextView.getText().toString();
    	
    	if (mEncrypted == 0) {
    		// Simply store the value
	    	ContentValues values = new ContentValues(1);
	        values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
	        values.put(Notes.TAGS, tags);
	
	        mContext.getContentResolver().update(mUri, values, null, null);
	        mContext.getContentResolver().notifyChange(mUri, null);
    	} else {
    		// Encrypt the tag

    		Intent i = new Intent(mContext, EncryptActivity.class);
    		i.putExtra(NotePadIntents.EXTRA_ACTION, CryptoIntents.ACTION_ENCRYPT);
    		i.putExtra(CryptoIntents.EXTRA_TEXT_ARRAY, EncryptActivity.getCryptoStringArray(null, null, tags));
    		i.putExtra(NotePadIntents.EXTRA_URI, mUri.toString());
    		mContext.startActivity(i);
    	}
    }


	@Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putString(BUNDLE_TAGS, "");
        return state;
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String tags = savedInstanceState.getString(BUNDLE_TAGS);
    }
}
