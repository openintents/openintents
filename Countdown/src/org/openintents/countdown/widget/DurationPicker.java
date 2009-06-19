/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.openintents.countdown.widget;

import org.openintents.com.android.internal.widget.NumberPicker;
import org.openintents.countdown.R;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * A view for selecting the a duration using hour, minute, second.
 *
 */
public class DurationPicker extends FrameLayout {
    
    /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    private static final OnDurationChangedListener NO_OP_CHANGE_LISTENER = new OnDurationChangedListener() {
        public void onDurationChanged(DurationPicker view, int day, int hourOfDay, int minute, int second) {
        }
    };
    
    // state
    private int mCurrentDay = 0; // 0-99999 (300 years)
    private int mCurrentHour = 0; // 0-23
    private int mCurrentMinute = 0; // 0-59
    private int mCurrentSecond = 0; // 0-59

    // ui components
    private final Button mShowDay;
    private final NumberPicker mDayPicker;
    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;
    private final NumberPicker mSecondPicker;
    
    // callbacks
    private OnDurationChangedListener mOnDurationChangedListener;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnDurationChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hour The current hour.
         * @param minute The current minute.
         * @param second The current second.
         */
        void onDurationChanged(DurationPicker view, int day, int hour, int minute, int second);
    }

    public DurationPicker(Context context) {
        this(context, null);
    }
    
    public DurationPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DurationPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.duration_picker,
            this, // we are the parent
            true);

        mShowDay = (Button) findViewById(R.id.showday);
        mShowDay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDayPicker();
			}
        	
        });
        
        // day
        mDayPicker = (NumberPicker) findViewById(R.id.day);
        mDayPicker.setRange(0, 99999);
        mDayPicker.setSpeed(100);
        //mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mDayPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentDay = newVal;
                onDurationChanged();
            }
        });
        
        // hour
        mHourPicker = (NumberPicker) findViewById(R.id.hour);
        mHourPicker.setRange(0, 23);
        mHourPicker.setSpeed(100);
        //mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mHourPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentHour = newVal;
                onDurationChanged();
            }
        });

        // digits of minute
        mMinutePicker = (NumberPicker) findViewById(R.id.minute);
        mMinutePicker.setRange(0, 59);
        mMinutePicker.setSpeed(100);
        mMinutePicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mMinutePicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentMinute = newVal;
                onDurationChanged();
            }
        });
        

        // digits of second
        mSecondPicker = (NumberPicker) findViewById(R.id.second);
        mSecondPicker.setRange(0, 59);
        mSecondPicker.setSpeed(100);
        mSecondPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mSecondPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentSecond = newVal;
                onDurationChanged();
            }
        });

        setOnDurationChangedListener(NO_OP_CHANGE_LISTENER);
        
        
        if (!isEnabled()) {
            setEnabled(false);
        }
    }
    
    public void showDayPicker() {
    	mShowDay.setVisibility(View.GONE);
    	mDayPicker.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mDayPicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mSecondPicker.setEnabled(enabled);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mDay;
        private final int mHour;
        private final int mMinute;
        private final int mSecond;

        private SavedState(Parcelable superState, int day, int hour, int minute, int second) {
            super(superState);
            mDay = day;
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }
        
        private SavedState(Parcel in) {
            super(in);
            mDay = in.readInt();
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getDay() {
            return mDay;
        }
        
        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }
        
        public int getSecond() {
            return mSecond;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mDay);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentDay, mCurrentHour, mCurrentMinute, mCurrentSecond);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentDay(ss.getDay());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnDurationChangedListener(OnDurationChangedListener onDurationChangedListener) {
        mOnDurationChangedListener = onDurationChangedListener;
    }

    /**
     * @return The current day (0-99999).
     */
    public Integer getCurrentDay() {
        return mCurrentDay;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentDay(Integer currentDay) {
        this.mCurrentDay = currentDay;
        updateDayDisplay();
    }
    
    /**
     * @return The current hour (0-23).
     */
    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour;
        updateHourDisplay();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    /**
     * @return The current second.
     */
    public Integer getCurrentSecond() {
        return mCurrentSecond;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        this.mCurrentSecond = currentSecond;
        updateSecondDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline(); 
    }

    /**
     * Set the state of the spinners appropriate to the current day.
     */
    private void updateDayDisplay() {
        int currentDay = mCurrentDay;
        mDayPicker.setCurrent(currentDay);
        if (mCurrentDay > 0 && mDayPicker.getVisibility() == View.GONE) {
        	showDayPicker();
        }
        onDurationChanged();
    }
    
    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourDisplay() {
        int currentHour = mCurrentHour;
        mHourPicker.setCurrent(currentHour);
        onDurationChanged();
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay() {
        mMinutePicker.setCurrent(mCurrentMinute);
        onDurationChanged();
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateSecondDisplay() {
        mSecondPicker.setCurrent(mCurrentSecond);
        onDurationChanged();
    }

    private void onDurationChanged() {
        mOnDurationChangedListener.onDurationChanged(this, getCurrentDay(), 
        		getCurrentHour(), 
        		getCurrentMinute(), getCurrentSecond());
    }
    
    /**
     * Returns the duration in milliseconds.
     * @return
     */
    public long getDuration() {

        // The text views may still have focus so clear theirs focus which will
        // trigger the on focus changed and any typed values to be pulled.
    	mDayPicker.clearFocus();
    	mHourPicker.clearFocus();
    	mMinutePicker.clearFocus();
    	mSecondPicker.clearFocus();

		long duration = 1000 * (((((mCurrentDay * 24l + mCurrentHour) * 60) + mCurrentMinute) * 60) + mCurrentSecond);
		
		return duration;
    }
    
    /**
     * Sets the duration in milliseconds.
     * @return
     */
    public void setDuration(long duration) {
    	long seconds = (long) (duration / 1000);
    	long minutes = seconds / 60;
    	seconds = seconds % 60;
    	long hours = minutes / 60;
    	minutes = minutes % 60;
    	long days = hours / 24;
    	hours = hours % 24;

    	setCurrentDay((int)days);
    	setCurrentHour((int)hours);
    	setCurrentMinute((int)minutes);
    	setCurrentSecond((int)seconds);
    	
    }

}

