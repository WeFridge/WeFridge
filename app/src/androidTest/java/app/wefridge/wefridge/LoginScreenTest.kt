package app.wefridge.wefridge


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Test requires device language to be English to match strings
    @Test
    fun loginScreenTest() {
        val appLogo = onView(
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
        appLogo.check(matches(isDisplayed()))

        val titleText = onView(
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
        titleText.check(matches(withText("WeFridge")))

        val emailSignInButton = onView(
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
        emailSignInButton.check(matches(isDisplayed()))
    }
}
