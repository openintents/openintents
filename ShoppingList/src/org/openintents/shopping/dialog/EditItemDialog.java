package org.openintents.shopping.dialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.Items;
import org.openintents.shopping.PreferenceActivity;
import org.openintents.shopping.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class EditItemDialog extends AlertDialog implements OnClickListener {

	Context mContext;
	Uri mItemUri;
	
	EditText mEditText;
	MultiAutoCompleteTextView mTags;
	EditText mPrice;
	EditText mQuantity;
	
	TextView mPriceLabel;

    String[] mTagList;

	NumberFormat mPriceFormatter = DecimalFormat.getNumberInstance(Locale.ENGLISH);
	
	public EditItemDialog(Context context, Uri itemUri, Uri relationUri) {
		super(context);
		mContext = context;
		mPriceFormatter.setMaximumFractionDigits(2);
		mPriceFormatter.setMinimumFractionDigits(2);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater
				.inflate(R.layout.dialog_edit_item, null);
		setView(view);

		mEditText = (EditText) view.findViewById(R.id.edittext);
		mTags = (MultiAutoCompleteTextView) view.findViewById(R.id.edittags);
		mPrice = (EditText) view.findViewById(R.id.editprice);
		mQuantity= (EditText) view.findViewById(R.id.editquantity);
		mPriceLabel = (TextView) view.findViewById(R.id.labeleditprice);

		final KeyListener kl = PreferenceActivity
			.getCapitalizationKeyListenerFromPrefs(context);
		mEditText.setKeyListener(kl);
		mTags.setKeyListener(kl);
		
		mTags.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		mTags.setThreshold(0);
		mTags.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					toggleTaglistPopup();
				}

			});
	        
		//setIcon(android.R.drawable.ic_menu_edit);
		setTitle(R.string.ask_edit_item);
		
		setItemUri(itemUri);
		setRelationUri(relationUri);
		
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
		
		mQuantity.addTextChangedListener(mTextWatcher);
		mPrice.addTextChangedListener(mTextWatcher);
	}

    public void setTagList(String[] taglist) {
    	mTagList = taglist;

	    if (taglist != null) {
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
	                android.R.layout.simple_dropdown_item_1line, mTagList);
	        mTags.setAdapter(adapter);
	    }
    }
    
	private void toggleTaglistPopup() {
		if (mTags.isPopupShowing()) {
			mTags.dismissDropDown();
		} else {
			mTags.showDropDown();
		}
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			updateQuantityPrice();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
		
	};
	
	void updateQuantityPrice() {
		try {
			double price = Double.parseDouble(mPrice.getText().toString());
			String quantityString = mQuantity.getText().toString();
			if (!TextUtils.isEmpty(quantityString)) {
				double quantity = Double.parseDouble(quantityString);
				price = quantity * price;
				String s = mPriceFormatter.format(price);
				mPriceLabel.setText(mContext.getText(R.string.price) + ": " + s);
				return;
			}
		} catch (NumberFormatException e) {
			// do nothing
		}
		
		// Otherwise show default label:
		mPriceLabel.setText(mContext.getText(R.string.price));
	}
	
	private final String[] mProjection = { 
			Shopping.Items.NAME,
			Shopping.Items.TAGS,
			Shopping.Items.PRICE 
	};
	private final String[] mRelationProjection = { 
			Shopping.Contains.QUANTITY 
	};

	private Uri mRelationUri;
			
	public void setItemUri(Uri itemUri) {
		mItemUri = itemUri;
		
		Cursor c = mContext.getContentResolver().query(mItemUri, 
				mProjection, null, null, null);
		if (c != null && c.moveToFirst()) {
			String text = c.getString(0);
			String tags = c.getString(1);
			long pricecent = c.getLong(2);
			String price = mPriceFormatter.format(pricecent * 0.01d);
			if (pricecent == 0) {
				// Empty field for easier editing
				// (Otherwise "0.00" has to be deleted manually first)
				price = "";
			}
			
			mEditText.setText(text);
			mTags.setText(tags);
			mPrice.setText(price);
		}
		c.close();
	}
	
	public void setRelationUri(Uri relationUri){
		mRelationUri = relationUri;
		Cursor c= mContext.getContentResolver().query(mRelationUri, mRelationProjection, null, null, null);
		if (c != null && c.moveToFirst()){
			String quantity = c.getString(0);
			mQuantity.setText(quantity);
		}
		c.close();		
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
		String quantity = mQuantity.getText().toString();
		
		Long priceLong;
		if (TextUtils.isEmpty(price)) {
			priceLong = 0L;
		} else {
			try {
				priceLong = (long) Math.round(100 * mPriceFormatter.parse(price).doubleValue());
			} catch (ParseException e) {
				priceLong = null;
			}
		}

    	// Remove trailing ","
    	tags = tags.trim();
    	if (tags.endsWith(",")) {
    		tags = tags.substring(0, tags.length() - 1);
    	}
    	tags = tags.trim();
    	
		ContentValues values = new ContentValues();
		values.put(Items.NAME, text);
		values.put(Items.TAGS, tags);
		if (price != null) {
			values.put(Items.PRICE, priceLong);
		}
		mContext.getContentResolver().update(mItemUri, values, null, null);
		mContext.getContentResolver().notifyChange(mItemUri, null);

		values.clear();
		values.put(Contains.QUANTITY, quantity);
		mContext.getContentResolver().update(mRelationUri, values, null, null);
		mContext.getContentResolver().notifyChange(mRelationUri, null);
	}
	
}
