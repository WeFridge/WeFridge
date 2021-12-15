package app.wefridge.wefridge


import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Test requires device language to be English to match strings
    @Test
    fun loginScreenTest() {
        val imageView = onView(
            allOf(
                withId(R.id.logo), withContentDescription("App logo"),
                withParent(
                    allOf(
                        withId(R.id.root),
                        withParent(withId(android.R.id.content))
                    )
                ),
                isDisplayed()
            )
        )
        imageView.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withText("WeFridge"),
                withParent(
                    allOf(
                        withId(R.id.action_bar),
                        withParent(withId(R.id.action_bar_container))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("WeFridge")))

        val button = onView(
            allOf(
                withId(R.id.email_button), withText("Sign in with email"),
                withParent(
                    allOf(
                        withId(R.id.btn_holder),
                        withParent(withId(R.id.container))
                    )
                ),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))
    }
}
