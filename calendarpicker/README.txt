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
