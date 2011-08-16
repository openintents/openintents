/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.data.aggregation;

import java.util.ArrayList;

import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.uri.ContentUris;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;

/**
 * 
 * This class is for wrapping the aggregated source cursors to provide a
 * virtual, merged view of the data where rows are sorted by the value of a
 * given field.
 * 
 * @author berke.andras
 * 
 */
public class MergedCursor extends AbstractCursor {

	/**
	 * Builds a MergedCursor instance and sets is attributes.
	 */
	public static class Builder {

		private Context context;
		private String orderColumn;
		private ArrayList<Cursor> cursors;
		private ArrayList<EventSource> sources;

		public Builder(Context context, String orderColumn) {
			this.context = context;
			this.orderColumn = orderColumn;
			this.cursors = new ArrayList<Cursor>();
			this.sources = new ArrayList<EventSource>();
		}

		public Builder add(Cursor cursor, EventSource source) {
			cursors.add(cursor);
			sources.add(source);
			return this;
		}

		public MergedCursor build() {

			Cursor[] cursorsArray = new Cursor[cursors.size()];
			cursors.toArray(cursorsArray);
			EventSource[] sourcesArray = new EventSource[sources.size()];
			sources.toArray(sourcesArray);
			return new MergedCursor(context, cursorsArray, sourcesArray,
					orderColumn);
		}
	}

	/**
	 * This class represents the position in the logical merged table. It
	 * implements the algorithm of row merging.
	 */
	private static class MergerPosition {

		private MergedCursor mergedCursor;

		private int mergerPosition = -1;

		private Cursor actCursor = null;
		private int actCursorIndex = -1;

		public MergerPosition(MergedCursor mergedCursor) {
			this.mergedCursor = mergedCursor;
		}

		public Cursor getActCursor() {
			return actCursor;
		}

		public int getActCursorIndex() {
			return actCursorIndex;
		}

		public boolean moveToPosition(int newPosition) {

			int neededSteps = newPosition - mergerPosition;

			if (neededSteps == 0)
				return true;

			if (neededSteps < 0) { // move backwards
				while (neededSteps < 0 && move(-1, false)) {
					neededSteps++;
					mergerPosition--;
				}
			} else { // move forward
				while (neededSteps > 0 && move(1, true)) {
					neededSteps--;
					mergerPosition++;
				}
			}

			return mergerPosition == newPosition;
		}

		/**
		 * Moving one row in the merged table.
		 * 
		 * @param dir
		 *            Signed integer indicating the direction of the move
		 * @param asc
		 *            Boolean indicating whether the rows of merged table should
		 *            be arranged in ascending order.
		 * @return true if moving in the merged table was successful.
		 */
		private boolean move(int dir, boolean asc) {

			Cursor[] cursors = mergedCursor.getCursors();

			int selectedIndex = -1;
			long selectedValue = asc ? Long.MIN_VALUE : Long.MAX_VALUE;

			// decide which of the aggregated cursors position will be moved
			for (int i = 0; i < cursors.length; i++) {
				Cursor c = cursors[i];

				boolean canStep = false;
				if (dir > 0) {
					// step forward: all cursors step one position
					canStep = c.moveToNext();
				} else {
					// step backwards: all cursors stay at current pos except
					// the one that holds the element at the actual pos
					canStep = i == actCursorIndex ? c.moveToPrevious() : !c
							.isBeforeFirst();
				}

				if (canStep) {
					// this cursor got more content
					// check the value of the field that the merged table
					// ordered by
					int orderByColumnIndex = mergedCursor
							.getCursorOrderByColumnIndex(i);
					long orderByColumnValue = c.getLong(orderByColumnIndex);
					if ((asc && orderByColumnValue > selectedValue)
							|| (!asc && orderByColumnValue < selectedValue)) {
						selectedIndex = i;
						selectedValue = orderByColumnValue;
					}
				}
			}

			// step back all cursors except the actually selected
			if (dir > 0) {
				for (int i = 0; i < cursors.length; i++)
					cursors[i].moveToPrevious();
			}
			if (selectedIndex > -1) {
				Cursor c = cursors[selectedIndex];
				actCursor = c;
				actCursorIndex = selectedIndex;

				if (dir > 0)
					c.moveToNext();

				return true;
			}

			return false;
		}
	}

	private Cursor[] mCursors;
	private int[] mCursorOrderByColumnIndex;
	private EventSource[] mSources;
	private int mCount;

	private MergerPosition mMergerPosition;

	private SourceContentObserver[] mSourceObservers;

	private MergedCursor(Context context, Cursor[] cursors,
			EventSource[] sources, String orderColumn) {

		mCursors = cursors;
		mCursorOrderByColumnIndex = new int[mCursors.length];
		mSources = sources;
		mSourceObservers = new SourceContentObserver[mCursors.length];

		int count = 0;
		for (int i = 0; i < cursors.length; i++) {
			Cursor c = cursors[i];
			mCursorOrderByColumnIndex[i] = c.getColumnIndex(orderColumn);
			count += c.getCount();

			mSourceObservers[i] = new SourceContentObserver(context, sources[i]);
			c.registerContentObserver(mSourceObservers[i]);
		}
		mCount = count;

		mMergerPosition = new MergerPosition(this);

		setNotificationUri(context.getContentResolver(),
				ContentUris.MergedEvents);
	}

	public void release() {
		for (int i = 0; i < mCursors.length; i++) {
			Cursor c = mCursors[i];
			if (!c.isClosed())
				c.unregisterContentObserver(mSourceObservers[i]);
			c.close();
			mSourceObservers[i] = null;
		}

	}

	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// STANDARD CURSOR METHODS
	// ---------------------------------------------------------------------------------

	@Override
	public int getCount() {
		return mCount;
	}

	private Cursor[] getCursors() {
		return mCursors;
	}

	private int getCursorOrderByColumnIndex(int index) {
		return mCursorOrderByColumnIndex[index];
	}

	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		return mMergerPosition.moveToPosition(newPosition);
	}

	public EventSource getSource() {
		return mSources[mMergerPosition.getActCursorIndex()];
	}

	@Override
	public String[] getColumnNames() {
		return mMergerPosition.getActCursor().getColumnNames();
	}

	@Override
	public double getDouble(int column) {
		return mMergerPosition.getActCursor().getDouble(column);
	}

	@Override
	public float getFloat(int column) {
		return mMergerPosition.getActCursor().getFloat(column);
	}

	@Override
	public int getInt(int column) {
		return mMergerPosition.getActCursor().getInt(column);
	}

	@Override
	public long getLong(int column) {
		return mMergerPosition.getActCursor().getLong(column);
	}

	@Override
	public short getShort(int column) {
		return mMergerPosition.getActCursor().getShort(column);
	}

	@Override
	public String getString(int column) {
		return mMergerPosition.getActCursor().getString(column);
	}

	@Override
	public boolean isNull(int column) {
		return mMergerPosition.getActCursor().isNull(column);
	}

}
