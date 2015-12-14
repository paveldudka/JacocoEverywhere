package com.trickyandroid.jacococoverageeverywhere;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkImportantTextPresent() {
        //that text view is super important
        Espresso.onView(ViewMatchers.withId(R.id.important_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}