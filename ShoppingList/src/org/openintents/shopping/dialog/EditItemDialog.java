package org.openintents.shopping.dialog;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Items;
import org.openintents.shopping.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditItemDialog extends AlertDialog implements OnClickListener {

	Context mContext;
	Uri mItemUri;
	
	EditText mEditText;
	EditText mTags;
	EditText mPrice;
	
	public EditItemDialog(Context context, Uri itemUri) {
		super(context);
		mContext = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater
				.inflate(R.layout.dialog_edit_item, null);
		setView(view);

		mEditText = (EditText) view.findViewById(R.id.edittext);
		mTags = (EditText) view.findViewById(R.id.edittags);
		mPrice = (EditText) view.findViewById(R.id.editprice);
		
		setIcon(android.R.drawable.ic_menu_edit);
		setTitle(R.string.ask_edit_item);
		
		setItemUri(itemUri);
		
		setButton(context.getText(R.string.ok), this);
		setButton2(context.getText(R.string.cancel), this);
		
		
		/*
		setButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {

						dialog.dismiss();
						doTextEntryDialogAction(mTextEntryMenu,
								(Dialog) dialog);

					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {

						dialog.cancel();
					}
				}).create();
				*/
	}
	
	String[] mProjection = { 
			Shopping.Items.NAME,
			Shopping.Items.TAGS,
			Shopping.Items.PRICE 
	};
			
	public void setItemUri(Uri itemUri) {
		mItemUri = itemUri;
		
		Cursor c = mContext.getContentResolver().query(mItemUri, 
				mProjection, null, null, null);
		if (c != null && c.moveToFirst()) {
			String text = c.getString(0);
			String tags = c.getString(1);
			String price = String.valueOf(c.getLong(2) * 0.01d);
			
			mEditText.setText(text);
			mTags.setText(tags);
			mPrice.setText(price);
		}
	}

	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON1) {
    		editItem();
    	}
		
	}
	
	void editItem() {
		String text = mEditText.getText().toString();
		String tags = mTags.getText().toString();
		String price = mPrice.getText().toString();
		Long priceLong;
		if (TextUtils.isEmpty(price)) {
			priceLong = 0L;
		} else {
			try {
				priceLong = (long) (100 * Double.parseDouble(price));
			} catch (NumberFormatException e) {
				priceLong = null;
			}
		}
		
		ContentValues values = new ContentValues();
		values.put(Items.NAME, text);
		values.put(Items.TAGS, tags);
		if (price != null) {
			values.put(Items.PRICE, priceLong);
		}
		mContext.getContentResolver().update(mItemUri, values, null, null);
		mContext.getContentResolver().notifyChange(mItemUri, null);

	}
	
}
