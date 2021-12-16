package app.wefridge.wefridge

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import junit.framework.Assert.fail
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import androidx.annotation.NonNull

import com.google.firebase.auth.FirebaseUser




class UITestUtils {

    companion object {
        fun signInUser(email: String, password: String) {
            val signInWithEmailButton = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.email_button),
                    ViewMatchers.withText("Sign in with email"),
                    childAtPosition(
                        Matchers.allOf(
                            ViewMatchers.withId(R.id.btn_holder),
                            childAtPosition(
                                ViewMatchers.withId(R.id.container),
                                0
                            )
                        ),
                        0
                    )
                )
            )
            signInWithEmailButton.perform(ViewActions.scrollTo(), ViewActions.click())

            val emailInputField = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.email)
                )
            )
            emailInputField.perform(
                ViewActions.scrollTo(),
                ViewActions.replaceText(email),
                ViewActions.closeSoftKeyboard()
            )

            val emailNextButton = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.button_next), ViewMatchers.withText("Next"),
                )
            )
            emailNextButton.perform(ViewActions.scrollTo(), ViewActions.click())

            val passwordInputField = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.password),
                )
            )
            passwordInputField.perform(
                ViewActions.scrollTo(),
                ViewActions.replaceText(password),
                ViewActions.closeSoftKeyboard()
            )

            val signInButton = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.button_done), ViewMatchers.withText("Sign in"),
                    childAtPosition(
                        childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")),
                            0
                        ),
                        4
                    )
                )
            )
            signInButton.perform(ViewActions.scrollTo(), ViewActions.click())
        }

        fun logOutUser() {
            val settingsButton = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.SettingsFragment),
                    ViewMatchers.withContentDescription("Settings"),
                    childAtPosition(
                        childAtPosition(
                            ViewMatchers.withId(R.id.bottom_nav),
                            0
                        ),
                        2
                    ),
                    ViewMatchers.isDisplayed()
                )
            )
            settingsButton.perform(ViewActions.click())

            val logOutButton = Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.logout), ViewMatchers.withText("Logout"),
                    childAtPosition(
                        childAtPosition(
                            ViewMatchers.withId(R.id.nav_host_fragment_content_main),
                            0
                        ),
                        2
                    ),
                    ViewMatchers.isDisplayed()
                )
            )
            logOutButton.perform(ViewActions.click())
        }

        fun createTestUser(email: String, password: String, activity: Activity) : FirebaseUser? {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task: Task<AuthResult> ->
                    if (!task.isSuccessful) {
                        fail("Could not create test user.")
                    } else {
                        auth.currentUser!!
                    }
                }
            return null
        }

        fun destroyTestUser(user: FirebaseUser) {
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("UITestUtils", "User account deleted.")
                    }
                }
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
}
