package org.openintents.shopping;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Status;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * View to show a shopping list with its items
 * 
 */
public class ShoppingListView extends ListView {
	private final static String TAG = "ShoppingListView";
	private final static boolean debug = true;

	public static final int MARK_CHECKBOX = 1;
	public static final int MARK_STRIKETHROUGH = 2;
	public static final int MARK_ADDTEXT = 3;

	public Typeface mTypefaceHandwriting;
	public Typeface mTypefaceDigital;

	public int mPriceVisibility;
	public int mTagsVisibility;
	public int mQuantityVisibility;
	public int mTypeface;
	public float mTextSize;
	public boolean mUpperCaseFont;
	public int mTextColor;
	public int mPriceTextColor;
	public int mMarkType;
	public int mMarkTextColor;

	NumberFormat mPriceFormatter = new DecimalFormat("0.00");

	int mMode = ShoppingActivity.MODE_IN_SHOP;
	Cursor mCursorItems;
	private View mThemedBackground;
	private long mListId;

	private TextView mTotalTextView;
	private TextView mTotalCheckedTextView;

	public boolean mClickMeansEdit;

	/**
	 * Extend the SimpleCursorAdapter to strike through items. if STATUS ==
	 * Shopping.Status.BOUGHT
	 */
	public class mSimpleCursorAdapter extends SimpleCursorAdapter implements
			ViewBinder {

		/**
		 * Constructor simply calls super class.
		 * 
		 * @param context
		 *            Context.
		 * @param layout
		 *            Layout.
		 * @param c
		 *            Cursor.
		 * @param from
		 *            Projection from.
		 * @param to
		 *            Projection to.
		 */
		mSimpleCursorAdapter(final Context context, final int layout,
				final Cursor c, final String[] from, final int[] to) {
			super(context, layout, c, from, to);
			super.setViewBinder(this);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			view.findViewById(R.id.price).setVisibility(mPriceVisibility);
			view.findViewById(R.id.tags).setVisibility(mTagsVisibility);
			view.findViewById(R.id.quantity).setVisibility(mQuantityVisibility);
			return view;
		}

		/**
		 * Additionally to the standard bindView, we also check for STATUS, and
		 * strike the item through if BOUGHT.
		 */
		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor) {
			super.bindView(view, context, cursor);

			long status = cursor.getLong(ShoppingActivity.mStringItemsSTATUS);

			TextView t = (TextView) view.findViewById(R.id.name);

			// set style for name view
			// Set font
			if (mTypeface == 1){
				t.setTypeface(null);
			} else if (mTypeface == 2){
				t.setTypeface(mTypefaceHandwriting);				
			} else {
				t.setTypeface(mTypefaceDigital);
			}

			// Set size
			t.setTextSize(mTextSize);

			// Check for upper case:
			if (mUpperCaseFont) {
				// Only upper case should be displayed
				CharSequence cs = t.getText();
				t.setText(cs.toString().toUpperCase());
			}

			t.setTextColor(mTextColor);

			if (status == Shopping.Status.BOUGHT) {
				t.setTextColor(mMarkTextColor);

				if (mMarkType == MARK_STRIKETHROUGH) {
					// We have bought the item,
					// so we strike it through:

					// First convert text to 'spannable'
					t.setText(t.getText(), TextView.BufferType.SPANNABLE);
					Spannable str = (Spannable) t.getText();

					// Strikethrough
					str.setSpan(new StrikethroughSpan(), 0, str.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					// apply color
					// TODO: How to get color from resource?
					// Drawable colorStrikethrough = context
					// .getResources().getDrawable(R.drawable.strikethrough);
					// str.setSpan(new ForegroundColorSpan(0xFF006600), 0,
					// str.setSpan(new ForegroundColorSpan
					// (getResources().getColor(R.color.darkgreen)), 0,
					// str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					// color: 0x33336600
				}

				if (mMarkType == MARK_ADDTEXT) {
					// very simple
					t.append("... OK");
				}

			}

			// we have a check box now.. more visual and gets the point across
			CheckBox c = (CheckBox) view.findViewById(R.id.check);

			Log.i(TAG, "bindview: pos = " + cursor.getPosition());

			// set style for check box
			c.setTag(new Integer(cursor.getPosition()));

			if (mMarkType == MARK_CHECKBOX) {
				c.setVisibility(CheckBox.VISIBLE);
				if ((status == Shopping.Status.BOUGHT && mMode == ShoppingActivity.MODE_IN_SHOP)
						|| (status == Shopping.Status.WANT_TO_BUY)
						&& mMode == ShoppingActivity.MODE_ADD_ITEMS) {
					c.setChecked(true);
				} else {
					c.setChecked(false);
				}
			} else {
				c.setVisibility(CheckBox.GONE);
			}

			/*
			 * t = (TextView) view.findViewById(R.id.quantity); if (t != null &&
			 * TextUtils.isEmpty(t.getText())) { t.setText("1"); }
			 */

			// The parent view knows how to deal with clicks.
			// We just pass the click through.
			// c.setClickable(false);
			final int cursorpos = cursor.getPosition();

			c.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click: ");
					toggleItemBought(cursorpos);
				}

			});
		}

		public boolean setViewValue(View view, Cursor cursor, int i) {
			if (view.getId() == R.id.price) {
				long price = cursor
						.getLong(ShoppingActivity.mStringItemsITEMPRICE);
				TextView tv = (TextView) view;
				if (mPriceVisibility == View.VISIBLE && price != 0) {
					tv.setVisibility(View.VISIBLE);
					String s = mPriceFormatter.format(price * 0.01d);
					tv.setTextColor(mPriceTextColor);
					tv.setText(s);
				} else {
					tv.setVisibility(View.GONE);
					tv.setText("");
				}
				return true;
			} else if (view.getId() == R.id.tags) {
				String tags = cursor
						.getString(ShoppingActivity.mStringItemsITEMTAGS);
				TextView tv = (TextView) view;
				if (mTagsVisibility == View.VISIBLE && !TextUtils.isEmpty(tags)) {
					tv.setVisibility(View.VISIBLE);
					tv.setTextColor(mPriceTextColor);
					tv.setText(tags);
				} else {
					tv.setVisibility(View.GONE);
					tv.setText("");
				}
				return true;
			} else if (view.getId() == R.id.quantity) {
				String quantity = cursor
						.getString(ShoppingActivity.mStringItemsQUANTITY);
				TextView tv = (TextView) view;
				if (mQuantityVisibility == View.VISIBLE
						&& !TextUtils.isEmpty(quantity)) {
					tv.setVisibility(View.VISIBLE);
					tv.setTextColor(mPriceTextColor);
					tv.setText(quantity);
				} else {
					tv.setVisibility(View.GONE);
					tv.setText("");
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setViewBinder(ViewBinder viewBinder) {
			throw new RuntimeException("this adapter implements setViewValue");
		}

	}

	ContentObserver mContentObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			if (mCursorItems != null) {
				try {
					requery();
				} catch (IllegalStateException e) {
					Log.e(TAG, "IllegalStateException ", e);
					// Somehow the logic is not completely right yet...
					mCursorItems = null;
				}
			}

		}

	};

	public ShoppingListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ShoppingListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ShoppingListView(Context context) {
		super(context);
		init();
	}

	private void init() {
		readFonts();
	}

	public void onResume() {

		// Content observer registered at fillItems()
		// registerContentObserver();
	}

	public void onPause() {
		unregisterContentObserver();
	}

	private void readFonts() {
		// Read fonts
		mTypefaceHandwriting = Typeface.createFromAsset(getContext()
				.getAssets(), "fonts/AnkeHand.ttf");
		mTypefaceDigital = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Crysta.ttf");

	}

	Cursor fillItems(long listId) {

		mListId = listId;
		String sortOrder = PreferenceActivity.getSortOrderFromPrefs(this
				.getContext());
		boolean hideBought = PreferenceActivity
				.getHideCheckedItemsFromPrefs(this.getContext());
		String selection;
		if (mMode == ShoppingActivity.MODE_IN_SHOP) {
			if (hideBought) {
				selection = "list_id = ? AND " + Shopping.Contains.STATUS
						+ " == " + Shopping.Status.WANT_TO_BUY;
			} else {
				selection = "list_id = ? AND " + Shopping.Contains.STATUS
						+ " <> " + Shopping.Status.REMOVED_FROM_LIST;
			}
		} else {
			selection = "list_id = ? ";
		}

		// Get a cursor for all items that are contained
		// in currently selected shopping list.
		mCursorItems = getContext().getContentResolver().query(
				ContainsFull.CONTENT_URI, ShoppingActivity.mStringItems,
				selection, new String[] { String.valueOf(listId) }, sortOrder);

		registerContentObserver();

		// Activate the following for a striped list.
		// setupListStripes(mListItems, this);

		if (mCursorItems == null) {
			Log.e(TAG, "missing shopping provider");
			setAdapter(new ArrayAdapter<String>(this.getContext(),
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return mCursorItems;
		}

		int layout_row = R.layout.shopping_item_row;

		int size = getSizeFromPrefs();
		if (size < 3) {
			layout_row = R.layout.shopping_item_row_small;
		}

		mSimpleCursorAdapter adapter = new mSimpleCursorAdapter(this
				.getContext(),
		// Use a template that displays a text view
				layout_row,
				// Give the cursor to the list adapter
				mCursorItems,
				// Map the IMAGE and NAME to...
				new String[] { ContainsFull.ITEM_NAME, /*
														 * ContainsFull.ITEM_IMAGE
														 * ,
														 */
				ContainsFull.ITEM_TAGS, ContainsFull.ITEM_PRICE,
						ContainsFull.QUANTITY },
				// the view defined in the XML template
				new int[] { R.id.name, /* R.id.image_URI, */R.id.tags,
						R.id.price, R.id.quantity });
		setAdapter(adapter);

		updateTotal();

		return mCursorItems;
	}

	/**
	 * 
	 */
	private void registerContentObserver() {
		getContext().getContentResolver().registerContentObserver(
				Shopping.Items.CONTENT_URI, true, mContentObserver);
	}

	private void unregisterContentObserver() {
		getContext().getContentResolver().unregisterContentObserver(
				mContentObserver);
	}

	private int getSizeFromPrefs() {
		int size = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(this.getContext()).getString(
						PreferenceActivity.PREFS_ROWSIZE,
						PreferenceActivity.PREFS_ROWSIZE_DEFAULT));
		return size;
	}

	/**
	 * Set theme according to Id.
	 * 
	 * @param themeId
	 */
	void setListTheme(int themeId) {
		int size = getSizeFromPrefs();

		switch (themeId) {
		case 1:
			getContext().setTheme(R.style.ShoppingListDefault);
			break;
		case 2:
			getContext().setTheme(R.style.ShoppingListClassic);
			break;
		case 3:
			getContext().setTheme(R.style.ShoppingListAndroid);
			break;
		}

		TypedArray a = getContext().obtainStyledAttributes(
				R.styleable.ShoppingListView);

		mTypeface = a.getInteger(R.styleable.ShoppingListView_typeface, 1);
		mUpperCaseFont = a.getBoolean(R.styleable.ShoppingListView_upperCaseFont, true);
		mTextColor = a.getColor(R.styleable.ShoppingListView_textColor,
				android.R.color.black);
		mPriceTextColor = a.getColor(
				R.styleable.ShoppingListView_priceTextColor,
				android.R.color.black);
		if (size == 1) {
			mTextSize = a
					.getInt(R.styleable.ShoppingListView_textSizeSmall, 10);
		} else if (size == 2) {
			mTextSize = a.getInt(R.styleable.ShoppingListView_textSizeMedium,
					20);
		} else {
			mTextSize = a
					.getInt(R.styleable.ShoppingListView_textSizeLarge, 30);
		}

		mMarkTextColor = a.getColor(R.styleable.ShoppingListView_markTextColor,
				android.R.color.black);
		mMarkType = a.getInt(R.styleable.ShoppingListView_markType, 0);

		if (mThemedBackground != null) {
			if (a.getInteger(R.styleable.ShoppingListView_backgroundPadding, -1) >=0){
			   mThemedBackground.setPadding(0,0,0,0);
			} else {
				// 9-patches do the padding automatically
				// todo clear padding 
			}
			mThemedBackground.setBackgroundResource(a.getResourceId(
					R.styleable.ShoppingListView_background, 0));
		}

		mClickMeansEdit = a.getBoolean(
				R.styleable.ShoppingListView_clickMeansEdit, true);

		a.recycle();

		invalidate();
		if (mCursorItems != null) {
			requery();
		}
	}

	public void setThemedBackground(View background) {
		mThemedBackground = background;

	}

	public void toggleItemBought(int position) {
		mCursorItems.moveToPosition(position);

		long oldstatus = mCursorItems
				.getLong(ShoppingActivity.mStringItemsSTATUS);

		// Toggle status:
		// bought -> want_to_buy
		// want_to_buy -> bought
		// removed_from_list -> want_to_buy
		long newstatus = Shopping.Status.WANT_TO_BUY;
		if (oldstatus == Shopping.Status.WANT_TO_BUY) {
			newstatus = Shopping.Status.BOUGHT;
		}

		ContentValues values = new ContentValues();
		values.put(Shopping.Contains.STATUS, newstatus);
		Log.d(TAG, "update row " + mCursorItems.getString(0) + ", newstatus "
				+ newstatus);
		getContext().getContentResolver().update(
				Uri.withAppendedPath(Shopping.Contains.CONTENT_URI,
						mCursorItems.getString(0)), values, null, null);

		requery();

		invalidate();
	}

	public boolean cleanupList() {

		boolean nothingdeleted = true;
		if (false) {
			// by deleteing items

			nothingdeleted = getContext().getContentResolver().delete(
					Shopping.Contains.CONTENT_URI,
					Shopping.Contains.LIST_ID + " = " + mListId + " AND "
							+ Shopping.Contains.STATUS + " = "
							+ Shopping.Status.BOUGHT, null) == 0;

		} else {
			// by changing state
			ContentValues values = new ContentValues();
			values.put(Contains.STATUS, Status.REMOVED_FROM_LIST);
			nothingdeleted = getContext().getContentResolver().update(
					Contains.CONTENT_URI,
					values,
					Shopping.Contains.LIST_ID + " = " + mListId + " AND "
							+ Shopping.Contains.STATUS + " = "
							+ Shopping.Status.BOUGHT, null) == 0;
		}

		requery();

		return !nothingdeleted;

	}

	public void toggleItemRemovedFromList(int pos) {
		mCursorItems.moveToPosition(pos);

		long oldstatus = mCursorItems
				.getLong(ShoppingActivity.mStringItemsSTATUS);

		// Toggle status:
		// bought -> want_to_buy
		// want_to_buy -> removed_from_list
		// removed_from_list -> want_to_buy
		long newstatus = Shopping.Status.WANT_TO_BUY;
		if (oldstatus == Shopping.Status.WANT_TO_BUY) {
			newstatus = Shopping.Status.REMOVED_FROM_LIST;
		}

		ContentValues values = new ContentValues();
		values.put(Shopping.Contains.STATUS, newstatus);
		Log.d(TAG, "update row " + mCursorItems.getString(0) + ", newstatus "
				+ newstatus);
		getContext().getContentResolver().update(
				Uri.withAppendedPath(Shopping.Contains.CONTENT_URI,
						mCursorItems.getString(0)), values, null, null);

		requery();

		// invalidate();

	}

	public void insertNewItem(String newItem) {

		long itemId = Shopping.getItem(getContext(), newItem, null);

		Log.i(TAG, "Insert new item. " + " itemId = " + itemId + ", listId = "
				+ mListId);
		Shopping.addItemToList(getContext(), itemId, mListId,
				Status.WANT_TO_BUY);

		fillItems(mListId);

		// Set the item that we have just selected:
		// Get position of ID:
		mCursorItems.moveToPosition(-1);
		while (mCursorItems.moveToNext()) {
			if (mCursorItems.getLong(ShoppingActivity.mStringItemsITEMID) == itemId) {
				int pos = mCursorItems.getPosition();
				if (pos > 0) {
					// Set selection one before, so that the item is fully
					// visible.
					setSelection(pos - 1);
				} else {
					setSelection(pos);
				}
				break;
			}
		}

	}

	public void requery() {
		mCursorItems.requery();
		updateTotal();
	}

	public void setTotalTextView(TextView tv) {
		mTotalTextView = tv;
	}

	public void setTotalCheckedTextView(TextView tv) {
		mTotalCheckedTextView = tv;
	}

	/**
	 * Update the text fields for "Total:" and "Checked:" with corresponding
	 * price information.
	 */
	public void updateTotal() {
		if (mTotalTextView == null || mTotalCheckedTextView == null) {
			// Most probably in "Add item" mode where no total is displayed
			return;
		}

		if (mPriceVisibility != View.VISIBLE) {
			// If price is not displayed, do not display total
			mTotalTextView.setVisibility(View.GONE);
			mTotalCheckedTextView.setVisibility(View.GONE);
			return;
		}

		mCursorItems.moveToPosition(-1);
		long total = 0;
		long totalchecked = 0;
		while (mCursorItems.moveToNext()) {
			long price = mCursorItems
					.getLong(ShoppingActivity.mStringItemsITEMPRICE);
			total += price;
			if (mCursorItems.getLong(ShoppingActivity.mStringItemsSTATUS) == Shopping.Status.BOUGHT) {
				totalchecked += price;
			}
		}
		Log.d(TAG, "Total: " + total + ", Checked: " + totalchecked);

		mTotalTextView.setTextColor(mPriceTextColor);
		mTotalCheckedTextView.setTextColor(mPriceTextColor);

		if (total != 0) {
			String s = mPriceFormatter.format(total * 0.01d);
			s = getContext().getString(R.string.total, s);
			mTotalTextView.setText(s);
			mTotalTextView.setVisibility(View.VISIBLE);
		} else {
			mTotalTextView.setVisibility(View.GONE);
		}

		if (totalchecked != 0) {
			String s = mPriceFormatter.format(totalchecked * 0.01d);
			s = getContext().getString(R.string.total_checked, s);
			mTotalCheckedTextView.setText(s);
			mTotalCheckedTextView.setVisibility(View.VISIBLE);
		} else {
			mTotalCheckedTextView.setVisibility(View.GONE);
		}
	}

}
