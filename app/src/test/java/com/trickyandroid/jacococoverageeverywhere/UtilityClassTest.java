package com.trickyandroid.jacococoverageeverywhere;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UtilityClassTest {

    @Test
    public void testOne() throws Exception {
        SomeUtilityClass utilityClass = new SomeUtilityClass();
        assertEquals(1, utilityClass.getOne());
    }

    @Test
    public void testTwo() throws Exception {
        SomeUtilityClass utilityClass = new SomeUtilityClass();
        assertEquals(2, utilityClass.getTwo());
    }
}