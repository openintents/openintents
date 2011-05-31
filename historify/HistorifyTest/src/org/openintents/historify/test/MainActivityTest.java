package org.openintents.historify.test;

import org.openintents.historify.MainActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testActivity() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}

