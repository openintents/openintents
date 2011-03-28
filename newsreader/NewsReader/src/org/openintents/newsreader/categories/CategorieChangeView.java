package org.openintents.newsreader.categories;

/*
/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.openintents.newsreader.R;
import org.openintents.provider.News;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;


public class CategorieChangeView extends LinearLayout{

	Handler mHandler;

	public static final String TAG = "CategorieChangeView";
//		public CategorieChangeView(Context context){super(context);}
	

	private OnCategorieChangeListener mCatChangeListener=null;
	private Context context;
	//private Button bInstance;
	private LinearLayout mPreviousButton;
	private LinearLayout mNextButton;
	
	Spinner mSpinner;

	Cursor mCursor;

	private String currentCategorie;

	public CategorieChangeView(Activity context){
		super(context);
		this.context = context;
		this.setOrientation(HORIZONTAL);

		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout internalLayout= (LinearLayout)inflater.
			inflate(R.layout.newsreader_categoriechangeview,null);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
		);

		addView(internalLayout, lp);

		mSpinner=(Spinner)internalLayout.findViewById(R.id.categoriechangeview_selector);
				
		//Uri uri = News.Categories.CONTENT_URI.buildUpon().appendQueryParameter(Categories.QUERY_USED_ONLY, "Y").build();
		Uri uri = News.Categories.CONTENT_URI;
		mCursor=context.managedQuery(uri ,News.Categories.PROJECTION_MAP,null,null,null);
		Log.d(TAG,"total categories>"+mCursor.getCount());
		mCursor.registerDataSetObserver(new CatObserver());
		initList();

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView parent,View view, int position, long id){
				String catname=(String)parent.getAdapter().getItem(position);
				CategorieChangeView.this.fireEvent(catname);
			}
			public void onNothingSelected(AdapterView parent){}
		});

	}

	/** set the current cat from outside. this fires a onCategorieChangeEvent.*/
	public void setCurrentCategorie(String newCat){
		//Log.d(TAG,"categorie from outside>"+newCat+"<");
		if (newCat!=null && ! newCat.equals(""))
		{
		
			int p=((ArrayAdapter)mSpinner.getAdapter()).getPosition(newCat);
			mSpinner.setSelection(p,false);//no animation
			//Log.d(TAG,"spinner state is now >"+mSpinner.getSelectedItemId()+"<< adapter thinks >>"+p+"<<");
		}
	///	fireEvent(newCat);
	}


	public void initList(){

		if (mCursor!=null)
		{
			mCursor.moveToFirst();
			final int c=mCursor.getCount();
			final int cIndex=mCursor.getColumnIndexOrThrow(
				News.Categories.NAME
				);

			ArrayAdapter aa=new ArrayAdapter(
				context,
				android.R.layout.simple_spinner_item
				);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			aa.add(context.getString(R.string.all));
			while (!mCursor.isAfterLast())
			{
				aa.add(mCursor.getString(cIndex));
				mCursor.moveToNext();
			}
			mSpinner.setAdapter(aa);
		
		}

	}


	private void fireEvent(String s){
		Log.d(TAG,"fireEvent, s>"+s);
		if (this.mCatChangeListener!=null)
		{
			this.mCatChangeListener.onCategorieChange(s);
		}
	}

	public void setOnCategorieChangeListener(OnCategorieChangeListener catChangeListener){
		this.mCatChangeListener=catChangeListener;
	}

	public interface OnCategorieChangeListener{
		public static final int DIRECTION_PREV=100;
		public static final int DIRECTION_NEXT=101;

		public void onCategorieChange(String newCat);
	};


	private class CatObserver extends DataSetObserver
	{
		public void onChanged(){
			Log.v("CatObserver", "changed");			
			CategorieChangeView.this.initList();
		}

		public void onInvalidated(){
			Log.v("CatObserver", "invalidated");			
		}
	};



}/*eoc*/


