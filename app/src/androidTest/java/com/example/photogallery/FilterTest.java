package com.example.photogallery;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Created by gagan on 2018-09-27.
 */

@RunWith(AndroidJUnit4.class)
public class FilterTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.photogallery", appContext.getPackageName());
    }
    @Rule
    public ActivityTestRule<GalleryActivity> mainAct = new ActivityTestRule<>(GalleryActivity.class);

    @Test
    public void filterTest() {
        onView(withId(R.id.captionText)).perform(replaceText("testString"));
        onView(withId(R.id.saveButton)).perform(click());
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.filterButton)).perform(click());
        onView(withId(R.id.keywordText)).perform(replaceText("testString"));
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.captionText)).check(matches(withText("testString")));
    }
}
