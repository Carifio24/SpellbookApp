package dnd.jon.spellbook;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import dnd.jon.spellbook.databinding.ActivityMainBinding;

@RunWith(AndroidJUnit4.class)
public class BasicInterfaceTest {

    @Before
    public void setup() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final File filesDir = context.getFilesDir();
        SpellbookUtils.deleteDirectory(filesDir);
    }

    @Test
    public void initialInterfaceTest() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            // Check that the character creation screen is visible, and create a new test character
            onView(withId(R.id.character_creation)).check(matches(isDisplayed()));
            onView(withId(R.id.creation_edit_text)).perform(typeText("Test"), closeSoftKeyboard());
            onView(withId(R.id.create_button)).perform(click());

            // Check that the character creation screen closes
            onView(withId(R.id.character_creation)).check(doesNotExist());
        }
    }
}
