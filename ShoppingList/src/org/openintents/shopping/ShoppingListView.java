package org.openintents.shopping;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Status;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
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

public class ShoppingListView extends ListView {

	public static final int MARK_CHECKBOX = 1;
	public static final int MARK_STRIKETHROUGH = 2;
	public static final int MARK_ADDTEXT = 3;

	public Typeface mTypefaceHandwriting;
	public Typeface mTypefaceDigital;

	public int mPriceVisiblity;
	public int mTagsVisiblity;
	public Typeface mTypeface;
	public float mTextSize;
	public boolean mUpperCaseFont;
	public int mTextColor;
	public int mMarkType;
	public int mMarkTextColor;

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
			view.findViewById(R.id.price).setVisibility(mPriceVisiblity);
			view.findViewById(R.id.tags).setVisibility(mTagsVisiblity);
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

			TextView t = (TextView) view.findViewById(R.id.name);
			// we have a check box now.. more visual and gets the point across
			CheckBox c = (CheckBox) view.findViewById(R.id.check);

			Log.i(TAG, "bindview: pos = " + cursor.getPosition());

			c.setTag(new Integer(cursor.getPosition()));

			// Set font
			t.setTypeface(mTypeface);

			// Set size
			t.setTextSize(mTextSize);

			// Check for upper case:
			if (mUpperCaseFont) {
				// Only upper case should be displayed
				CharSequence cs = t.getText();
				t.setText(cs.toString().toUpperCase());
			}

			t.setTextColor(mTextColor);

			long status = cursor.getLong(ShoppingActivity.mStringItemsSTATUS);
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

			t = (TextView) view.findViewById(R.id.quantity);
			if (t != null && TextUtils.isEmpty(t.getText())) {
				t.setText("1");
			}

			// The parent view knows how to deal with clicks.
			// We just pass the click through.
			c.setClickable(false);
		}

		public boolean setViewValue(View view, Cursor cursor, int i) {
			if (view.getId() == R.id.price) {
				long price = cursor
						.getLong(ShoppingActivity.mStringItemsITEMPRICE);
				((TextView) view).setText(String.valueOf(price * 0.01d));
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

	private static final String TAG = "ShoppingListView";
	int mMode = ShoppingActivity.MODE_IN_SHOP;
	private Cursor mCursorItems;
	private View mThemedBackground;
	private long mListId;

	public ShoppingListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		readFonts();
	}

	public ShoppingListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		readFonts();
	}

	public ShoppingListView(Context context) {
		super(context);
		readFonts();
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
				new String[] { ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
						ContainsFull.ITEM_TAGS, ContainsFull.ITEM_PRICE,
						ContainsFull.QUANTITY },
				// the view defined in the XML template
				new int[] { R.id.name, R.id.image_URI, R.id.tags, R.id.price,
						R.id.quantity });
		setAdapter(adapter);
		return mCursorItems;
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
			mTypeface = null;
			mUpperCaseFont = false;
			mTextColor = 0xffffffff; // white

			if (size == 1) {
				mTextSize = 18;
			} else if (size == 2) {
				mTextSize = 23;
			} else {
				mTextSize = 28;
			}

			mMarkTextColor = 0xffcccccc; // white gray
			mMarkType = MARK_CHECKBOX;

			if (mThemedBackground != null) {
				mThemedBackground.setPadding(0, 0, 0, 0);
				mThemedBackground.setBackgroundDrawable(null);
			}

			break;
		case 2:
			mTypeface = mTypefaceHandwriting;
			mUpperCaseFont = false;
			mTextColor = 0xff000000; // black
			if (size == 1) {
				mTextSize = 15;
			} else if (size == 2) {
				mTextSize = 20;
			} else {
				mTextSize = 25;
			}

			mMarkTextColor = 0xff008800; // dark green
			mMarkType = MARK_STRIKETHROUGH;

			if (mThemedBackground != null) {
				// 9-patch drawable defines padding by itself
				mThemedBackground
						.setBackgroundResource(R.drawable.shoppinglist01d);
			}

			break;
		case 3:
			mTypeface = mTypefaceDigital;

			// Digital only supports upper case fonts.
			mUpperCaseFont = true;
			mTextColor = 0xffff0000; // red
			if (size == 1) {
				mTextSize = 21;
			} else if (size == 2) {
				mTextSize = 26;
			} else {
				mTextSize = 31;
			}

			mMarkTextColor = 0xff00ff00; // light green
			mMarkType = MARK_ADDTEXT;

			if (mThemedBackground != null) {
				mThemedBackground.setPadding(0, 0, 0, 0);
				mThemedBackground
						.setBackgroundResource(R.drawable.theme_android);
			}

			break;
		}

		invalidate();
		if (mCursorItems != null) {
			mCursorItems.requery();
		}
	}

	public void setThemedBackground(View background) {
		mThemedBackground = background;

	}

	public void toggleItemBought(int position) {
		mCursorItems.moveToPosition(position);

		long oldstatus = mCursorItems.getLong(ShoppingActivity.mStringItemsSTATUS);

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
		Log.d(TAG, "update row " + mCursorItems.getString(0) + ", newstatus " + newstatus);
		getContext().getContentResolver().update(
				Uri.withAppendedPath(Shopping.Contains.CONTENT_URI,
						mCursorItems.getString(0)), values, null, null);

		mCursorItems.requery();

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

		mCursorItems.requery();
		
		return !nothingdeleted;
		
	}

	public void toggleItemRemovedFromList(int pos) {
		mCursorItems.moveToPosition(pos);

		long oldstatus = mCursorItems.getLong(ShoppingActivity.mStringItemsSTATUS);

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
		Log.d(TAG, "update row " + mCursorItems.getString(0) + ", newstatus " + newstatus);
		getContext().getContentResolver().update(
				Uri.withAppendedPath(Shopping.Contains.CONTENT_URI,
						mCursorItems.getString(0)), values, null, null);

		mCursorItems.requery();

		//invalidate();

		
	}

	public void insertNewItem(String newItem) {		

		long itemId = Shopping.getItem(getContext(), newItem, null);

		Log.i(TAG, "Insert new item. " + " itemId = " + itemId
				+ ", listId = " + mListId);
		Shopping.addItemToList(getContext(), itemId, mListId, Status.WANT_TO_BUY);

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

}
