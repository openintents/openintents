Icon obtained from Open Clip Art Library:
http://www.openclipart.org/detail/34669



NOTE:
The Calendar Picker Demo requires the following file to be present:
./CalendarPickerDemo/src/org/openintents/calendarpicker/contract/IntentConstants.java
This file should be copied from
./CalendarPicker/src/org/openintents/calendarpicker/contract/IntentConstants.java

The latter is the "canonical" version of this file, although both may be stored in SVN.
All changes made to the canonical version should be manually propagated to its dependent copy.

Normally, an svn:extern might be used for this, but the OpenIntents project opts not to use them
due to the extra time they add to downloads.



NOTE:
ContentProviders must implement "selection" (i.e. WHERE clauses) for epoch ranges for event
selection to work properly, and it must support an ascending sort order.  This is trivial
when your backend is SQLite, but could be complicated if you decide to use a MatrixCursor for
some reason.

If you supply your data through Intent extras instead of with an SQLite-backed ContentProvider,
the event selection activity won't work quite properly.



NOTE:
The minimum version of the Calendar Picker is set to 4 (Android 1.6), but the minimum version of
the Calendar Picker Demo is set to 7 (Android 2.1).  This is because the "authority" portion of
the CalendarProvider URI changed in Android 2.0.  The data URI is selected in the Demo, then
passed on to the Picker.  The new (Android 2.0) URI is apparently still valid when accessed from
the Android 1.6 Picker, so long as it originates from an Android 2.0+ application.
However, experimentation will show that if the Demo is set to Android 1.6, the CalendarProvider URI
will be invalid.  I have defined a different constant (in IntentConstants.java) for the "authority"
of the URI that works with Android 1.6.

Both the Picker and the Demo require the READ_CALENDAR permission.
