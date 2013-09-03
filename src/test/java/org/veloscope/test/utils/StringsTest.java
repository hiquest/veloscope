package org.veloscope.test.utils;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.veloscope.utils.Strings;

@RunWith(JUnit4.class)
public class StringsTest {
    private static String [] NOT_EMPTY_STRINGS = { "hello" };
    private static String [] EMPTY_STRINGS = { "", " ", "\n\n", null };

    @Test
    public void testEmpty() {
        for (String s: EMPTY_STRINGS) {
            Assert.assertTrue(Strings.empty(s));
        }

        for (String s: NOT_EMPTY_STRINGS) {
            Assert.assertFalse(Strings.empty(s));
        }
    }

    @Test
    public void testNotEmpty() {
        for (String s: EMPTY_STRINGS) {
            Assert.assertFalse("String " + s + " must get checked as empty!", Strings.notEmpty(s));
        }

        for (String s: NOT_EMPTY_STRINGS) {
            Assert.assertTrue("String " + s + " must get checked as non empty!", Strings.notEmpty(s));
        }
    }

    @Test
    public void testCapitalize() {
        Assert.assertNull("Null must give a null", Strings.capitalize(null));
        Assert.assertEquals(" ", Strings.capitalize(" "));
        Assert.assertEquals("A", Strings.capitalize("a"));
        Assert.assertEquals("Getme", Strings.capitalize("getme"));
    }
}
