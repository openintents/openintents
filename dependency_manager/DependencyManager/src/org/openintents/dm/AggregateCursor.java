/**
 * This file is part of the Android DependencyManager project hosted at
 * http://code.google.com/p/android-dependencymanager/
 *
 * Copyright (C) 2009 Jens Finkhaeuser <jens@finkhaeuser.de>
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
 **/

package org.openintents.dm;

import android.database.Cursor;
import android.database.AbstractCursor;
import android.database.CursorWindow;
import android.database.CursorIndexOutOfBoundsException;

import java.util.ArrayList;

import android.util.Log;

/**
 * Merges the results of other cursors into it's own data set.
 *
 * Similar in some ways to MergeCursor. The difference is that the Cursors from
 * which data is taken need not exist before AggregateCursor is created. Data
 * is merged into AggregateCursor, and AggregateCursor's result list stays
 * sorted.
 *
 * (see http://developer.android.com/reference/android/database/MergeCursor.html)
 *
 * This code is based on the MatrixCursor implementation.
 **/
class AggregateCursor extends AbstractCursor
{
  /***************************************************************************
   * Private constants
   **/
  private static final String LTAG = "AggregateCursor";



  /***************************************************************************
   * Private data
   **/
  private final String[] columnNames;
  private Object[] data;
  private int rowCount = 0;
  private final int columnCount;
  private RowComparator comparator;
  private boolean unique = false;


  /***************************************************************************
   * Inner classes
   **/

  /**
   * Abstract base for comparator objects.
   **/
  public static abstract class RowComparator
  {
    /**
     * Given the current row for c1 and c2, return
     * < 0 if c1 is to be sorted before c2
     * = 0 if c1 and c2 can be considered equal
     * > 0 if c1 is to be sorted after c2
     **/
    public abstract int compareCurrentRows(Cursor c1, Cursor c2);
  }



  /***************************************************************************
   * Implementation
   **/

  /**
   * Constructs a new cursor with the given initial capacity.
   *
   * @param columnNames names of the columns, the ordering of which
   *  determines column ordering elsewhere in this cursor
   * @param comparator RowComparator-derived instance used to determine where
   *  to sort newly merged rows.
   * @param uniqe if set, duplicate rows are merged
   * @param initialCapacity in rows
   */
  public AggregateCursor(String[] columnNames, RowComparator comparator,
      boolean unique, int initialCapacity)
  {
    this.columnNames = columnNames;
    this.columnCount = columnNames.length;
    this.comparator = comparator;
    this.unique = unique;

    if (initialCapacity < 1) {
      initialCapacity = 1;
    }

    this.data = new Object[columnCount * initialCapacity];
  }



  /**
   * Constructs a new cursor.
   *
   * @param columnNames names of the columns, the ordering of which
   *  determines column ordering elsewhere in this cursor
   * @param comparator RowComparator-derived instance used to determine where
   *  to sort newly merged rows.
   * @param uniqe if set, duplicate rows are merged
   */
  public AggregateCursor(String[] columnNames, RowComparator comparator,
      boolean unique)
  {
    this(columnNames, comparator, unique, 16);
  }



  /**
   * Constructs a new non-unique cursor.
   *
   * @param columnNames names of the columns, the ordering of which
   *  determines column ordering elsewhere in this cursor
   * @param comparator RowComparator-derived instance used to determine where
   *  to sort newly merged rows.
   */
  public AggregateCursor(String[] columnNames, RowComparator comparator)
  {
    this(columnNames, comparator, false, 16);
  }



  /**
   * Merges data from the given cursor into this cursor. Not safe for concurrent
   * use. IllegalArgumentException if columNames and columnCount do not match.
   **/
  public void merge(Cursor other)
  {
    if (null == other || 0 >= other.getCount()) {
      return;
    }

    if (columnCount != other.getColumnCount()) {
      throw new IllegalArgumentException("Cannot update AggregateCursor "
          + "from a cursor with different column count.");
    }
    for (int i = 0 ; i < columnCount ; ++i) {
      int otherIndex = other.getColumnIndex(columnNames[i]);
      if (-1 == otherIndex) {
        throw new IllegalArgumentException("Invalid cursor to update from; "
            + "expected column '" + columnNames[i] + "' not found!");
      }
    }

    // Expand capacity to fit the other cursor's data as well.
    int newCapacity = rowCount + other.getCount();
    ensureCapacity(newCapacity * columnCount);

    // Store current position, so we can restore the cursor position at the end
    // of this function.
    int current = getPosition();

    // Append other's rows.
    other.moveToFirst();
    for ( ; !other.isAfterLast() ; other.moveToNext()) {
      int idx = insertIndex(other, 0, rowCount);
      if (-1 == idx) {
        // Only happens when unique is set. The current row is a duplicate we
        // need to ignore.
        continue;
      }


      // If we insert *before* current, then we must increment current for it
      // to point at the same row again.
      if (idx <= current) {
        ++current;
      }

      // First we need to move everything from idx back one row.
      int shiftRows = rowCount - idx;
      System.arraycopy(data, idx * columnCount, data, (idx + 1) * columnCount,
          shiftRows * columnCount);

      // Next, insert the new row at idx.
      ++rowCount;
      for (int i = 0 ; i < columnCount ; ++i) {
        // We're retrieving every column as a string. Our own getters then
        // convert to the desired type.
        data[(idx * columnCount) + i] = other.getString(other.getColumnIndex(columnNames[i]));
      }
    }

    // Restore current
    moveToPosition(current);
  }



  /**
   * Helper function for merge(). Uses quicksort-like algorithm for determining
   * where to insert the row cursor currently points at. May return -1 if the
   * new row is a duplicate and the unique flag is set. Thanks to the fact that
   * data is sorted, that'll avoid duplicates nicely.
   **/
  private int insertIndex(Cursor cursor, int left, int right)
  {
    if (right <= left) {
      return left;
    }

    int pivot = (left + right) / 2;

    moveToPosition(pivot);
    int cres = comparator.compareCurrentRows(this, cursor);

    if (cres < 0) {
      return insertIndex(cursor, pivot + 1, right);
    }
    else if (cres > 0) {
      return insertIndex(cursor, left, pivot);
    }

    if (unique) {
      // Discard duplicates
      return -1;
    }
    else {
      // Keep duplicates
      return pivot;
    }
  }



  /**
   * Adds a new row to the end and returns a builder for that row. Not safe
   * for concurrent use.
   *
   * @return builder which can be used to set the column values for the new
   *  row
   */
  public RowBuilder newRow()
  {
    rowCount++;
    int endIndex = rowCount * columnCount;
    ensureCapacity(endIndex);
    int start = endIndex - columnCount;
    return new RowBuilder(start, endIndex);
  }



  /**
   * Adds a new row to the end with the given column values. Not safe
   * for concurrent use.
   *
   * @throws IllegalArgumentException if {@code columnValues.length !=
   *  columnNames.length}
   * @param columnValues in the same order as the the column names specified
   *  at cursor construction time
   */
  public void addRow(Object[] columnValues)
  {
    if (columnValues.length != columnCount) {
      throw new IllegalArgumentException("columnNames.length = "
          + columnCount + ", columnValues.length = "
          + columnValues.length);
    }

    int start = rowCount++ * columnCount;
    ensureCapacity(start + columnCount);
    System.arraycopy(columnValues, 0, data, start, columnCount);
  }



  /**
   * Adds a new row to the end with the given column values. Not safe
   * for concurrent use.
   *
   * @throws IllegalArgumentException if {@code columnValues.size() !=
   *  columnNames.length}
   * @param columnValues in the same order as the the column names specified
   *  at cursor construction time
   */
  public void addRow(Iterable<?> columnValues)
  {
    int start = rowCount * columnCount;
    int end = start + columnCount;
    ensureCapacity(end);

    if (columnValues instanceof ArrayList<?>) {
      addRow((ArrayList<?>) columnValues, start);
      return;
    }

    int current = start;
    Object[] localData = data;
    for (Object columnValue : columnValues) {
      if (current == end) {
        // TODO: null out row?
        throw new IllegalArgumentException(
            "columnValues.size() > columnNames.length");
      }
      localData[current++] = columnValue;
    }

    if (current != end) {
        // TODO: null out row?
        throw new IllegalArgumentException(
            "columnValues.size() < columnNames.length");
    }

    // Increase row count here in case we encounter an exception.
    rowCount++;
  }



  /** Optimization for {@link ArrayList}. */
  private void addRow(ArrayList<?> columnValues, int start)
  {
    int size = columnValues.size();
    if (size != columnCount) {
      throw new IllegalArgumentException("columnNames.length = "
          + columnCount + ", columnValues.size() = " + size);
    }

    rowCount++;
    Object[] localData = data;
    for (int i = 0; i < size; i++) {
      localData[start + i] = columnValues.get(i);
    }
  }



  /** Ensures that this cursor has enough capacity. */
  private void ensureCapacity(int size)
  {
    if (size > data.length) {
      Object[] oldData = this.data;
      int newSize = data.length * 2;
      if (newSize < size) {
        newSize = size;
      }
      this.data = new Object[newSize];
      System.arraycopy(oldData, 0, this.data, 0, oldData.length);
    }
  }



  /**
   * Builds a row, starting from the left-most column and adding one column
   * value at a time. Follows the same ordering as the column names specified
   * at cursor construction time.
   */
  public class RowBuilder
  {
    private int index;
    private final int endIndex;

    RowBuilder(int index, int endIndex) {
      this.index = index;
      this.endIndex = endIndex;
    }

    /**
     * Sets the next column value in this row.
     *
     * @throws CursorIndexOutOfBoundsException if you try to add too many
     *  values
     * @return this builder to support chaining
     */
    public RowBuilder add(Object columnValue) {
      if (index == endIndex) {
        throw new CursorIndexOutOfBoundsException(
            "No more columns left.");
      }

      data[index++] = columnValue;
      return this;
    }
  }



  public int getCount()
  {
    return rowCount;
  }



  public String[] getColumnNames()
  {
    return columnNames;
  }



  /**
   * Gets value at the given column for the current row.
   */
  private Object get(int column) {
    if (column < 0 || column >= columnCount) {
      throw new CursorIndexOutOfBoundsException("Requested column: "
          + column + ", # of columns: " +  columnCount);
    }
    if (mPos < 0) {
      throw new CursorIndexOutOfBoundsException("Before first row.");
    }
    if (mPos >= rowCount) {
      throw new CursorIndexOutOfBoundsException("After last row.");
    }
    return data[mPos * columnCount + column];
  }



  public byte[] getBlob(int column)
  {
    Object value = get(column);
    if (null == value) {
      return null;
    }

    return String.valueOf(value).getBytes();
  }



  public String getString(int column)
  {
    Object value = get(column);
    if (null == value) {
      return null;
    }
    return String.valueOf(value);
  }



  public short getShort(int column)
  {
    Object value = get(column);
    return (value instanceof String)
      ? Short.valueOf((String) value)
      : ((Number) value).shortValue();
  }



  public int getInt(int column)
  {
    Object value = get(column);
    return (value instanceof String)
      ? Integer.valueOf((String) value)
      : ((Number) value).intValue();
  }



  public long getLong(int column)
  {
    Object value = get(column);
    return (value instanceof String)
      ? Long.valueOf((String) value)
      : ((Number) value).longValue();
  }



  public float getFloat(int column)
  {
    Object value = get(column);
    return (value instanceof String)
      ? Float.valueOf((String) value)
      : ((Number) value).floatValue();
  }



  public double getDouble(int column)
  {
    Object value = get(column);
    return (value instanceof String)
      ? Double.valueOf((String) value)
      : ((Number) value).doubleValue();
  }



  public boolean isNull(int column) {
    return get(column) == null;
  }
}
