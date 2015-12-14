package com.trickyandroid.jacococoverageeverywhere;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UtilityClassTest {
    SomeUtilityClass utilityClass;

    @BeforeClass
    public void setUp() {
        this.utilityClass = new SomeUtilityClass();
    }

    @Test
    public void testOne() throws Exception {
        assertEquals(1, utilityClass.getOne());
    }

    @Test
    public void testTwo() throws Exception {
        assertEquals(2, utilityClass.getTwo());
    }
}