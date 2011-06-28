package org.openintents.historify.test;

import junit.framework.TestCase;

import org.openintents.historify.MainActivity;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Contact.Comparator;

import android.test.ActivityInstrumentationTestCase2;

public class ContactTest extends TestCase {

    

    public void testCompare1() {
    	Comparator c = new Contact.Comparator();
    	assertEquals(0, c.compare(new Contact("A", "Bob"), new Contact("B", "Bob")));
    	assertEquals(0, c.compare(new Contact("A", "Bob"), new Contact("A", "Bob")));
    }

    public void testCompare2() {
    	Comparator c = new Contact.Comparator();
    	assertTrue(c.compare(new Contact("A", "Bob"), new Contact("A", "Alice")) > 0);
    	assertTrue(c.compare(new Contact("A", "Bob"), new Contact("A", "bob"))<0);
    	assertTrue(c.compare(new Contact("A", "bob"), new Contact("A", "bob"))<0);
    	
    }
}

