package app.wefridge.wefridge


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Test requires device language to be English to match strings
    // Test requires a user account with credentials "test@example.com/12345678" to exist
    @Test
    fun mainActivityTest() {
        val signInWithEmailButton = onView(
            allOf(
                withId(R.id.email_button), withText("Sign in with email"),
                childAtPosition(
                    allOf(
                        withId(R.id.btn_holder),
                        childAtPosition(
                            withId(R.id.container),
                            0
                        )
                    ),
                    0
                )
            )
        )
        signInWithEmailButton.perform(scrollTo(), click())

        val emailInputField = onView(
            allOf(
                withId(R.id.email)
            )
        )
        emailInputField.perform(scrollTo(), replaceText("test@example.com"), closeSoftKeyboard())

        val emailNextButton = onView(
            allOf(
                withId(R.id.button_next), withText("Next"),
            )
        )
        emailNextButton.perform(scrollTo(), click())

        val passwordInputField = onView(
            allOf(
                withId(R.id.password),
            )
        )
        passwordInputField.perform(scrollTo(), replaceText("12345678"), closeSoftKeyboard())

        val signInButton = onView(
            allOf(
                withId(R.id.button_done), withText("Sign in"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    4
                )
            )
        )
        signInButton.perform(scrollTo(), click())

        val fab = onView(
            allOf(
                withId(R.id.fab),
                withParent(withParent(withId(R.id.nav_host_fragment_content_main))),
                isDisplayed()
            )
        )
        fab.check(matches(isDisplayed()))

        // Test that pantry, nearby items, and settings fragment are in the bottom navigation
        val pantryFragment = onView(
            allOf(
                withId(R.id.PantryFragment), withContentDescription("Pantry"),
                withParent(withParent(withId(R.id.bottom_nav))),
                isDisplayed()
            )
        )
        pantryFragment.check(matches(isDisplayed()))

        val nearbyItemsFragment = onView(
            allOf(
                withId(R.id.NearbyItemsFragment), withContentDescription("Nearby"),
                withParent(withParent(withId(R.id.bottom_nav))),
                isDisplayed()
            )
        )
        nearbyItemsFragment.check(matches(isDisplayed()))

        val settingsFragment = onView(
            allOf(
                withId(R.id.SettingsFragment), withContentDescription("Settings"),
                withParent(withParent(withId(R.id.bottom_nav))),
                isDisplayed()
            )
        )
        settingsFragment.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
