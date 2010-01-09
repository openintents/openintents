package org.openintents.shopping;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Status;
import org.openintents.util.ThemeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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

	Typeface mCurrentTypeface = null;

	public int mPriceVisibility;
	public int mTagsVisibility;
	public int mQuantityVisibility;
	public String mTypeface;
	public float mTextSize;
	public boolean mUpperCaseFont;
	public int mTextColor;
	public int mPriceTextColor;
	public int mMarkTextColor;
	public boolean mShowCheckBox;
	public boolean mShowStrikethrough;
	public String mTextSuffixUnchecked;
	public String mTextSuffixChecked;

	NumberFormat mPriceFormatter = new DecimalFormat("0.00");

	int mMode = ShoppingActivity.MODE_IN_SHOP;
	Cursor mCursorItems;
	private View mThemedBackground;
	private long mListId;

	private TextView mTotalTextView;
	private TextView mTotalCheckedTextView;
	
	private Drawable mDefaultDivider;

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
			/*view.findViewById(R.id.quantity).setVisibility(mQuantityVisibility);*/
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
			t.setTypeface(mCurrentTypeface);

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

				if (mShowStrikethrough) {
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

				if (mTextSuffixChecked != null) {
					// very simple
					t.append(mTextSuffixChecked);
				}

			} else {
				// item not bought:
				if (mTextSuffixUnchecked != null) {
					t.append(mTextSuffixUnchecked);
				}
			}

			// we have a check box now.. more visual and gets the point across
			CheckBox c = (CheckBox) view.findViewById(R.id.check);

			Log.i(TAG, "bindview: pos = " + cursor.getPosition());

			// set style for check box
			c.setTag(new Integer(cursor.getPosition()));

			if (mShowCheckBox) {
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
			

			// also check around check box
			LinearLayout l = (LinearLayout) view.findViewById(R.id.check_surround);

			l.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click around: ");
					toggleItemBought(cursorpos);
				}

			});
		}

		public boolean setViewValue(View view, Cursor cursor, int i) {
			int id = view.getId();
			if (id == R.id.name){
				String name = cursor.getString(ShoppingActivity.mStringItemsITEMNAME);
				String quantity = cursor.getString(ShoppingActivity.mStringItemsQUANTITY);
				if (mQuantityVisibility == View.VISIBLE 
						&& !TextUtils.isEmpty(quantity)) {
					name = quantity + " " + name;
				}
				TextView tv = (TextView) view;
				tv.setText(name);
				return true;
			} else if (id == R.id.price) {
				long price = getQuantityPrice(cursor);
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
			} else if (id == R.id.tags) {
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
			}/* else if (id == R.id.quantity) {
				//String quantity = cursor
				//		.getString(ShoppingActivity.mStringItemsQUANTITY);
				TextView tv = (TextView) view;
				//if (mQuantityVisibility == View.VISIBLE
				//		&& !TextUtils.isEmpty(quantity)) {
				//	tv.setVisibility(View.VISIBLE);
				//	tv.setTextColor(mPriceTextColor);
				//	tv.setText(quantity);
				//} else {
					tv.setVisibility(View.GONE);
				//	tv.setText("");
				//}
				return true;
			} */else {
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
		// Remember standard divider
		mDefaultDivider = getDivider();
	}

	public void onResume() {

		// Content observer registered at fillItems()
		// registerContentObserver();
	}

	public void onPause() {
		unregisterContentObserver();
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
				ContainsFull.ITEM_TAGS, ContainsFull.ITEM_PRICE/*,
						ContainsFull.QUANTITY*/ },
				// the view defined in the XML template
				new int[] { R.id.name, /* R.id.image_URI, */R.id.tags,
						R.id.price/*, R.id.quantity*/ });
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
	void setListTheme(String themeName) {
		int size = getSizeFromPrefs();

		// backward compatibility:
		if (themeName == null) {
			setLocalStyle(R.style.ShoppingListDefault, size);
		} else if (themeName.equals("1")) {
			setLocalStyle(R.style.ShoppingListDefault, size);
		} else if (themeName.equals("2")) {
			setLocalStyle(R.style.ShoppingListClassic, size);
		} else if (themeName.equals("3")) {
			setLocalStyle(R.style.ShoppingListAndroid, size);
		} else {
			// New styles:
			boolean ok = setRemoteStyle(themeName, size);
			
			if (!ok) {
				// Some error occured, let's use default style:
				setLocalStyle(R.style.ShoppingListDefault, size);
			}
		}
		
		invalidate();
		if (mCursorItems != null) {
			requery();
		}
	}

	private void setLocalStyle(int style, int size) {
		getContext().setTheme(style);
		
		TypedArray a = getContext().obtainStyledAttributes(
				R.styleable.ShoppingListView);

		mTypeface = a.getString(R.styleable.ShoppingListView_typeface);
		
		if (!TextUtils.isEmpty(mTypeface)) {
			mCurrentTypeface = Typeface.createFromAsset(getContext().getAssets(),
				mTypeface);
		} else {
			mCurrentTypeface = null;
		}
		
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
		
		mShowCheckBox = a.getBoolean(R.styleable.ShoppingListView_showCheckBox, true);
		mShowStrikethrough = a.getBoolean(R.styleable.ShoppingListView_showStrikethrough, false);
		mTextSuffixUnchecked = a.getString(R.styleable.ShoppingListView_textSuffixUnchecked);
		mTextSuffixChecked = a.getString(R.styleable.ShoppingListView_textSuffixChecked);
		
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

		int divider = a.getInteger(R.styleable.ShoppingListView_divider, 0);
		
		a.recycle();
		
		Drawable div = null;
		if (divider > 0) {
			div = getResources().getDrawable(divider);
		} else if (divider < 0) {
			div = null;
		} else {
			div = mDefaultDivider;
		}
		
		setDivider(div);
	}

	private boolean setRemoteStyle(String styleName, int size) {

		PackageManager pm = getContext().getPackageManager();
		
		String packageName = ThemeUtils.getPackageNameFromStyle(styleName);
		
		if (packageName == null) {
			Log.e(TAG, "Invalid style name: " + styleName);
			return false;
		}
		
		Context c = null;
		try {
			c = getContext().createPackageContext(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Name not found");
			return false;
		}
		
		Resources res = c.getResources();
		
		int themeid = res.getIdentifier(styleName, null, null);
		
		int[] attr = ThemeUtils.getAttributeIds(c, ThemeUtils.OpenIntentsThemeAttributes, packageName);
		
		TypedArray a = c.obtainStyledAttributes(themeid, attr);
		
		mTypeface = a.getString(ThemeUtils.ID_typeface);

		if (!TextUtils.isEmpty(mTypeface)) {

			try {
				Log.d(TAG, "Reading typeface: package: " + packageName + ", typeface: " + mTypeface);
				Resources remoteRes = pm.getResourcesForApplication(packageName);
				mCurrentTypeface = Typeface.createFromAsset(remoteRes.getAssets(),
						mTypeface);
				Log.d(TAG, "Result: " + mCurrentTypeface);
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Package not found for Typeface", e);
				mCurrentTypeface = null;
			}
		} else {
			mCurrentTypeface = null;
		}
		
		mUpperCaseFont = a.getBoolean(ThemeUtils.ID_upperCaseFont, true);
		
		mTextColor = a.getColor(ThemeUtils.ID_textColor,
				android.R.color.black);
		
		mPriceTextColor = a.getColor(
				ThemeUtils.ID_priceTextColor,
				android.R.color.black);
		if (size == 1) {
			mTextSize = a
					.getInt(ThemeUtils.ID_textSizeSmall, 10);
		} else if (size == 2) {
			mTextSize = a.getInt(ThemeUtils.ID_textSizeMedium,
					20);
		} else {
			mTextSize = a
					.getInt(ThemeUtils.ID_textSizeLarge, 30);
		}

		mMarkTextColor = a.getColor(ThemeUtils.ID_markTextColor,
				android.R.color.black);
		mShowCheckBox = a.getBoolean(R.styleable.ShoppingListView_showCheckBox, true);
		mShowStrikethrough = a.getBoolean(R.styleable.ShoppingListView_showStrikethrough, false);
		mTextSuffixUnchecked = a.getString(R.styleable.ShoppingListView_textSuffixUnchecked);
		mTextSuffixChecked = a.getString(R.styleable.ShoppingListView_textSuffixChecked);
		
		if (mThemedBackground != null) {
			if (a.getInteger(ThemeUtils.ID_backgroundPadding, -1) >=0){
			   mThemedBackground.setPadding(0,0,0,0);
			} else {
				// 9-patches do the padding automatically
				// todo clear padding 
			}
			try {
				Resources remoteRes = pm.getResourcesForApplication(packageName);
				int resid = a.getResourceId(ThemeUtils.ID_background, 0);
				if (resid != 0) {
					Drawable d = remoteRes.getDrawable(resid);
					mThemedBackground.setBackgroundDrawable(d);
				} else {
					// remove background
					mThemedBackground.setBackgroundResource(0);
				}
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Package not found for Theme background.", e);
			} catch (Resources.NotFoundException e) {
				Log.e(TAG, "Resource not found for Theme background.", e);
			}
		}

		int divider = a.getInteger(ThemeUtils.ID_divider, 0);
		
		a.recycle();
		
		Drawable div = null;
		if (divider > 0) {
			div = getResources().getDrawable(divider);
		} else if (divider < 0) {
			div = null;
		} else {
			div = mDefaultDivider;
		}
		
		setDivider(div);
		
		return true;
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
			long price = getQuantityPrice(mCursorItems);
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
	
	private long getQuantityPrice(Cursor cursor) {
		long price = cursor
				.getLong(ShoppingActivity.mStringItemsITEMPRICE);
		if (price != 0) {
			String quantityString = cursor.getString(ShoppingActivity.mStringItemsQUANTITY);
			if (!TextUtils.isEmpty(quantityString)) {
				try {
					double quantity = Double.parseDouble(quantityString);
					price = (long) (price * quantity);
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
		}
		return price;
	}

}
