package com.example.photogallery;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Created by gagan on 2018-09-27.
 */

@RunWith(AndroidJUnit4.class)
public class FilterTest {

    String testString;
    String startDateStr;
    String endDateStr;
    long startDateLong;
    long endDateLong;
    String topLat;
    String topLong;
    String btmLat;
    String btmLong;

    @Before
    public void setVars(){
        testString = "testString";
        startDateStr = "20180925";
        endDateStr = "20180930";
        startDateLong = 1537833600000l;
        endDateLong = 1538265600000l;
        topLat = "50";
        topLong = "-125";
        btmLat = "45";
        btmLong = "-120";
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.photogallery", appContext.getPackageName());
    }

    @Rule
    public ActivityTestRule<GalleryActivity> mainAct = new ActivityTestRule<>(GalleryActivity.class);

    @Test
    public void keywordSearchTest() {
        onView(withId(R.id.captionText)).perform(replaceText(testString));
        onView(withId(R.id.saveButton)).perform(click());
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.filterButton)).perform(click());
        onView(withId(R.id.keywordText)).perform(replaceText(testString));
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.captionText)).check(matches(withText(testString)));
    }

    @Test
    public void dateSearchTest() throws ParseException {
        onView(withId(R.id.filterButton)).perform(click());
        onView(withId(R.id.startDateText)).perform(replaceText(startDateStr));
        onView(withId(R.id.endDateText)).perform(replaceText(endDateStr));
        onView(withId(R.id.searchButton)).perform(click());
        DateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date pictureDate = sdf.parse(getText(withId(R.id.timeText)));
        Date startDate = new Date(startDateLong);
        Date endDate = new Date(endDateLong);
        assertTrue(pictureDate.after(startDate) && pictureDate.before(endDate));
    }

    @Test
    public void geoSearchTest() {
        onView(withId(R.id.filterButton)).perform(click());
        onView(withId(R.id.topLatText)).perform(replaceText(topLat));
        onView(withId(R.id.topLongText)).perform(replaceText(topLong));
        onView(withId(R.id.btmLatText)).perform(replaceText(btmLat));
        onView(withId(R.id.btmLongText)).perform(replaceText(btmLong));
        onView(withId(R.id.searchButton)).perform(click());
        String[] coords = getText(withId(R.id.geoText)).split("\\n");
        float lat = Float.parseFloat(coords[0]);
        float lon = Float.parseFloat(coords[1]);
        assertTrue((Math.abs(lat) <= Math.abs(Integer.parseInt(topLat)) && Math.abs(lat) >= Math.abs(Integer.parseInt(btmLat))) &&
                (Math.abs(lon) <= Math.abs(Integer.parseInt(topLong)) && Math.abs(lon) >= Math.abs(Integer.parseInt(btmLong))));
    }

    String getText(final Matcher<View> matcher) {
        final String[] stringHolder = { null };
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView temp = (TextView)view; //Save, because of check in getConstraints()
                stringHolder[0] = temp.getText().toString();
            }
        });
        return stringHolder[0];
    }
}
