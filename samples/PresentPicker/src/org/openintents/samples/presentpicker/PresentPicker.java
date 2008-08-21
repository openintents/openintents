/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Part of the code is based on Google's ApiDemos
 * */

package org.openintents.samples.presentpicker;

/*
 * YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-LIB-n.n.n.JAR FILE:
 * 
 * In the Eclipse Package Explorer, right-click on the imported 
 * project PresentPicker, select "Properties", then "Java Build Path" 
 * and tab "Libraries". There "Add External JARs..." and select 
 * lib/openintents-lib-n.n.n.jar. 
 */

import java.util.Random;

import org.openintents.OpenIntents;
import org.openintents.provider.Shopping;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class PresentPicker extends Activity {
	
	private static final int MENU_ABOUT = Menu.FIRST;
	
	private LinearLayout mLayout;
	private LinearLayout.LayoutParams mLayoutParams;
	
	private ProgressDialog mDialog;
    private int mProgress;
    private boolean mCancelled;
    private static final int PROGRESS = 1;
    
    private int mPresentPos;
    private int mPresentNum;
    private Random mMagicOracle; // The source of all present wisdom.
    private int[] mPickList; // Picklist to avoid duplicates.
    private int mPickListLen;
    
    private ImageButton mPersonIB;
    private int mPersonId;
    private AutoCompleteTextView mName;
    private int mMF;
    
    private RadioGroup mRadioMF;
    private RadioButton mRadioM;
    private RadioButton mRadioF;
    
    private ImageButton mCallPerson;
    
    private Spinner mPersonality;
    private Spinner mOccasion;
    
    private EditText mSelection;
    
    // The following are complex Buttons
    private LinearLayout mAddToShoppingList;
    private LinearLayout mViewShoppingList;
	
    private ImageButton mFirstItem; 
    
    private Dialog mAboutDialog;
    
    private TabHost mTabHost;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.presentpicker_main);
        
       OpenIntents.requiresOpenIntents(this);
        
		// Initialize the convenience functions:
		Shopping.mContentResolver = getContentResolver();
		
		Context context = this;
        // Get the Resources object from our context
        Resources res = context.getResources();
        
    
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		
		TabSpec tabspec = mTabHost.newTabSpec("Presentee");
		tabspec.setIndicator("Presentee", res.getDrawable(R.drawable.present001b));
		tabspec.setContent(R.id.content1);
		mTabHost.addTab(tabspec);
		
		tabspec = mTabHost.newTabSpec("Results");
		tabspec.setIndicator("Results", res.getDrawable(android.R.drawable.ic_menu_search));//android.R.drawable.search_icon_default));
		tabspec.setContent(R.id.content2);
		mTabHost.addTab(tabspec);
		
		mTabHost.setCurrentTab(0);
		
		//if (true) return;
		
		/*
        GridView g = (GridView) findViewById(R.id.myGrid);
        g.setAdapter(new ImageAdapter(this));
        */
        
        mLayout = (LinearLayout) findViewById(R.id.results);
        
        mLayoutParams = new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.WRAP_CONTENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT
         );
        mLayoutParams.height = 60;
        mLayoutParams.width = 60;
        
        // Placeholder
        addQuestionButton();
        
        mPersonId = 0;
        mMF = 0;
        
        mPersonIB = (ImageButton) findViewById(R.id.image);
        mPersonIB.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				mPersonId++;
				if (mPersonId >= 3) mPersonId = 0;
				mPersonIB.setImageResource(mPersonIds[mPersonId + mMF]);
			}
        });
        
        //mRadioM = (RadioButton) findViewById(R.id.radio_m);
        //mRadioF = (RadioButton) findViewById(R.id.radio_f);
        mRadioMF = (RadioGroup) findViewById(R.id.radio_mf);
        RadioGroup.OnCheckedChangeListener occl 
        	= new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup rg, int i) {
				Log.i("PP", "radio: " + i);
				if (i == R.id.radio_m) mMF = 0;
				if (i == R.id.radio_f) mMF = 3;
				mPersonIB.setImageResource(mPersonIds[mPersonId + mMF]);
			}
        };
        mRadioMF.setOnCheckedChangeListener(occl);
        mRadioMF.check(R.id.radio_m);
        
        // Initialize our magic oracle:
        mMagicOracle = new Random();
        
        // Add progress handler
        LinearLayout button = (LinearLayout) findViewById(R.id.search);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCancelled = false;
                mProgress = 0;
                OnCancelListener cancelListener = new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mCancelled = true;
                        //TODO: remove before submitting
//                        Log.v("ProgressBarTest", "Canceled the progress bar.");
                    }
                };

                mDialog = ProgressDialog.show(PresentPicker.this,
                        null, "Searching for suitable presents...", false,
                        true, cancelListener);

                // Set up the magical oracle to find the
                // best presents:
                mPresentNum = 2 + mMagicOracle.nextInt(4); // max 5
                //mPresentNum = 5;
                mPresentPos = 0;
                mPickListLen = mPresentIds.length;
                mPickList = new int[mPickListLen];
                for (int i=0; i<mPickList.length; i++)
                	mPickList[i] = i;
                
                // for screenshot:
                // mPickList = new int[] {5, 3, 1, 7, 6, 2, 0, 9};
                // mPickListLen = mPickList.length;
                // mPresentNum = 5;
                
                mTabHost.setCurrentTab(1);
        		
                removeButtons();
                addQuestionButton();
                mHandler.sendMessage(mHandler.obtainMessage(PROGRESS));
            }
        });
        
        // Autocomplete contact list entry:
        ContentResolver content = getContentResolver();
        Cursor cursor = content.query(Contacts.People.CONTENT_URI,
                PEOPLE_PROJECTION, null, null, Contacts.People.DEFAULT_SORT_ORDER);
        ContactListAdapter adapter =
                new ContactListAdapter(this, cursor);

        mName = (AutoCompleteTextView)
                findViewById(R.id.name);
        mName.setAdapter(adapter);

        /*
        // Call the person:
        mCallPerson = (ImageButton) findViewById(R.id.callperson);
        mCallPerson.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Get ID of person
				// TODO: Get ID of person in AutoComplete field.
				
				// Call that person
				// TODO: Call that person via intent.
			}
        	
        });
        */
        
        // Set personality list:
        mPersonality = (Spinner) findViewById(R.id.personality);
        ArrayAdapter<String> arrayadapter
        	= new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, mPersonalityStrings);
        arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPersonality.setAdapter(arrayadapter);
        
        // Set occasion list:
        mOccasion = (Spinner) findViewById(R.id.occasion);
        arrayadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mOccasionStrings);
        arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOccasion.setAdapter(arrayadapter);
        
        // Set edit text:
        mSelection = (EditText) findViewById(R.id.selection);
        
        // Add to shopping list button:
        mAddToShoppingList = (LinearLayout) findViewById(R.id.button_add);
        mAddToShoppingList.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// insert item in shopping list #0:
				String newItem = mSelection.getText().toString();
				String newName = mName.getText().toString();
				
				if (newName.compareTo("") != 0) {
					newItem = newItem + " for " + mName.getText().toString();
				}
				
				// Only add if there is something to add
				if (newItem.compareTo("") != 0) {
					// First create the new item:
					long itemId = Shopping.getItem(newItem);
					
					// Now put this into the default shopping list
					long listId = Shopping.getDefaultList();
					long id = Shopping.addItemToList(itemId, listId);
					int toastId;
					if (id > 0){
						toastId = R.string.item_added_to_shopping;
					} else {
						toastId = R.string.item_not_added_to_shopping;
					}
					Toast.makeText(PresentPicker.this, toastId, Toast.LENGTH_SHORT).show();
					
					// That was it!
				}
			}
        	
        });
        
        // View the shopping list by calling the activity:
        mViewShoppingList = (LinearLayout) findViewById(R.id.button_view);
        mViewShoppingList.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Shopping.Lists.CONTENT_URI);
				startActivity(intent);
			}
        	
        });
        
        // Initial focus shall go to the name field:
//        mName.requestFocus();
    }
    
    // Handle the process of searching for suitable present:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESS && !mCancelled) {
                mProgress += mMagicOracle.nextInt(200);
            	
            	// for screenshot:
            	// mProgress += 5;
                
                if (mProgress / 10000. * mPresentNum > mPresentPos)
                {
                	if (mPresentPos == 0) {
                		 // first time only
                		removeButtons();
                		mFirstItem = null;
                	}
                	int inspiration = mMagicOracle.nextInt(mPickListLen);
                	// inspiration = 0; // for screenshot
                	addPresentButton(mPickList[inspiration]);
                	// remove this item from the possible choices of the oracle:
                	mPickListLen--;
                	mPickList[inspiration] = mPickList[mPickListLen];
                	mPresentPos++;
                }
                if (mProgress > 10000) {
                	// we are done
                    mProgress = 0;
                    // Set focus to new element:
                    mFirstItem.requestFocus();
                    mDialog.cancel();
                    return;
                }
                mDialog.setProgress(mProgress);
                sendMessageDelayed(obtainMessage(PROGRESS), 50);
            }
        }
    };

    private void addPresentButton(int i)
    {
        ImageButton ib = new ImageButton(PresentPicker.this);
        ib.setImageResource(mPresentIds[i]);
        ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ib.setLayoutParams(mLayoutParams);
        ib.setId(i); // set id, so that we know later who we are
        ib.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				int id = v.getId();
				mSelection.setText(mPresentStrings[id]);
			}
        });
        ib.setOnFocusChangeListener(new OnFocusChangeListener() {

			/* (non-Javadoc)
			 * @see android.view.View.OnFocusChangeListener#onFocusChanged(android.view.View, boolean)
			 */		
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					int id = v.getId();
					mSelection.setText(mPresentStrings[id]);
				}
				
			}
        	
        });
        
        if (mFirstItem == null) {
        	mFirstItem = ib;
        }
        mLayout.addView(ib);
    }

    
    private void addQuestionButton()
    {
    	// Add a button:
        Button bb = new Button(PresentPicker.this);
        bb.setLayoutParams(mLayoutParams);
        bb.setText("?");
        bb.setTextSize(42);
        bb.setTextColor(0xffaa00aa);
        bb.setPadding(0, 0, 0, 0);
        bb.setTypeface(Typeface.DEFAULT_BOLD);
        mLayout.addView(bb);
    }
    
    private void removeButtons() {
    	mLayout.removeAllViews();
    }

    // The menu

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		
		// Standard menu
		menu.add(0, MENU_ABOUT, 0, R.string.about)
			.setIcon(R.drawable.about001a)
			.setShortcut('0', 'a');
		
		return true;
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.Menu.Item)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAboutBox();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    /**
     * Show the "About" box.
     */
    private void showAboutBox() {
    	mAboutDialog = new Dialog(PresentPicker.this);
		
    	mAboutDialog.setContentView(R.layout.presentpicker_about);
		
    	mAboutDialog.setTitle(getString(R.string.about_title));
				
    	// Open a web page upon clicking on FastIcon
		((ImageButton) mAboutDialog.findViewById(R.id.fasticon))
			.setOnClickListener(new OnClickListener() {
				public void onClick(final View v) {
					Intent i = new Intent(Intent.ACTION_VIEW, 
						Uri.parse("http://www.fasticon.com/commercial_license.html"));
					startActivity(i);
				}
			});
		
		// OK button
		Button bOk = (Button) mAboutDialog.findViewById(R.id.ok);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				
				mAboutDialog.dismiss();
			}
		});
		
		mAboutDialog.show();
	
    }

    
    // from API-demos AutoComplete4.java
    
    // XXX compiler bug in javac 1.5.0_07-164, we need to implement Filterable
    // to make compilation work
    public static class ContactListAdapter
            extends CursorAdapter implements Filterable {
        public ContactListAdapter(Context context, Cursor c) {
            super(context, c);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
        	final LayoutInflater inflater = LayoutInflater.from(mContext);
            final TextView view = (TextView) inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false);
            view.setText(cursor.getString(5));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(5));
        }

        @Override
        public String convertToString(Cursor cursor) {
            return cursor.getString(5);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (mFilterQueryProvider != null) {
                return mFilterQueryProvider.runQuery(constraint);
            }

            StringBuilder buffer = null;
            String[] args = null;
            if (constraint != null) {
                buffer = new StringBuilder();
                buffer.append("UPPER(");
                buffer.append(Contacts.ContactMethods.NAME);
                buffer.append(") GLOB ?");
                args = new String[] { constraint.toString().toUpperCase() + "*" };
            }

            return mContent.query(Contacts.People.CONTENT_URI, PEOPLE_PROJECTION,
                    buffer == null ? null : buffer.toString(), args,
                    Contacts.People.DEFAULT_SORT_ORDER);
        }

        private ContentResolver mContent;        
    }

    private static final String[] PEOPLE_PROJECTION = new String[] {
        Contacts.People._ID,
        Contacts.People.PRIMARY_PHONE_ID,
        Contacts.People.TYPE,
        Contacts.People.NUMBER,
        Contacts.People.LABEL,
        Contacts.People.NAME
    };
	
	private Integer[] mPresentIds = {
	        R.drawable.ball,
	        R.drawable.bike,
	        R.drawable.book,
	        R.drawable.car,
	        R.drawable.cat,
	        R.drawable.cd,
	        R.drawable.cup,
	        R.drawable.dog,
	        R.drawable.flashlight,
	        R.drawable.flower,
	        R.drawable.ice_cream,
	        R.drawable.joystick,
	        R.drawable.picture,
	        R.drawable.roller,
	        R.drawable.skate,
	        R.drawable.toy};
	
	private String[] mPresentStrings = {
			"football",
			"bike",
			"book",
			"car (equipment)",
			"cat (pet or doll)",
			"CD (new music)",
			"cup (with name)",
			"dog (pet or doll)",
			"flashlight",
			"flower",
			"ice cream (invitation)",
			"console game",
			"camera (equipment)",
			"roller skates",
			"skate board",
			"toy"
	};
	
	private Integer[] mPersonIds = {
			R.drawable.boy_1,
			R.drawable.boy_3,
			R.drawable.boy_6,
			R.drawable.girl_1,
			R.drawable.girl_3,
			R.drawable.girl_5};
	
	private String[] mPersonalityStrings = {
			"cheerful",
			"funny",
			"happy",
			"energetic",
			"carefree",
			"sweet",
			"confident",
			"earnest",
			"stylish",
			"innocent",
			"passionate",
			"lively",
			"sentimental",
			"romantic",
			"earthy",
			"whimsical",
			"intimate",
			"elegant",
			"sensual",
			"humorous",
			"quirky",
			"mannered",
			"joyous"
		};
	
		private String[] mOccasionStrings = {
			"birthday",
			"anniversary",
			"congratulations",
			"graduation",
			"new baby",
			"engagement",
			"new job / promotion",
			"housewarming",
			"christmas",
			"easter",
			"wedding",
			"thank you",
			"valentine's day",
			"halloween",
			"get well",
			"sympathy",
			"thanksgiving",
			"mother's day",
			"father's day",
			"new year"			
		};

}