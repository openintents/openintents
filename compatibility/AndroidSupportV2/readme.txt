Compatibility Libraries for Android.


This SDK component contains static libraries providing access to newer APIs

on older platforms. To use those libraries, simply copy them as static libraries

into your project.


"v2" provides support for using new APIs on Android API 2 (1.1) and above.


The code is based on "Android Compatibility package, revision 3".

The following modifications have been introduced:
* android.support.v2.os.Build.VERSION has been added, to support integer SDK_INT
  on all platform versions (native support starts on Android API 4 - 1.6)
* SDK_INT has been replaced by the v2 compatible version in:
  - app.FragmentActivity
  - app.FragmentManager
  - os.ParcelableCompat
  - view.MenuCompat
  - view.MotionEventCompat
  - view.VelocityTrackerCompat
  - view.ViewConfigurationCompat

All modifications are prepended by the original line, commented out as "//v4".
