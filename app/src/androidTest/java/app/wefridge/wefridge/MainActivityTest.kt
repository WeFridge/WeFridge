package app.wefridge.wefridge


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.firebase.auth.FirebaseAuth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf

import app.wefridge.wefridge.UITestUtils.Companion.signInUser
import app.wefridge.wefridge.application.MainActivity

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
        val auth = FirebaseAuth.getInstance()
        auth.currentUser ?: signInUser("test@example.com","12345678")

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
}
