package com.trickyandroid.jacococoverageeverywhere;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UtilityClassTest {
    @Test
    public void testOne() throws Exception {
        assertEquals(1, SomeUtilityClass.getOne());
    }

    @Test
    public void testTwo() throws Exception {
        assertEquals(2, SomeUtilityClass.getTwo());
    }
}