package com.mckimquyen.barcodescanner.feature.common.dlg

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.barcodescanner.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialogFragmentSecurityAlertTest {

    // Mirrors the private URL_KEY constant inside DialogFragmentSecurityAlert.newInstance()
    private val urlArgKey = "URL_KEY"
    private val suspiciousUrl = "https://evil.com/google.com"

    private fun launchWithUrl() {
        // Host container uses the app's real theme; DialogFragmentSecurityAlert applies its
        // own R.style.BottomSheetM3 to the dialog window via getTheme() override.
        launchFragmentInContainer<DialogFragmentSecurityAlert>(
            fragmentArgs = bundleOf(urlArgKey to suspiciousUrl),
            themeResId = R.style.AppTheme,
        )
    }

    @Test
    fun shows_title_message_and_url() {
        launchWithUrl()

        onView(withText(R.string.security_alert_title)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewMessage)).check(matches(withSubstring(suspiciousUrl)))
        onView(withText(R.string.action_go_back_safe)).check(matches(isDisplayed()))
        onView(withText(R.string.action_proceed_anyway)).check(matches(isDisplayed()))
    }

    @Test
    fun clicking_go_back_dismisses_dialog() {
        launchWithUrl()

        onView(withId(R.id.buttonNegative)).perform(click())

        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }

    @Test
    fun clicking_proceed_dismisses_dialog() {
        launchWithUrl()

        onView(withId(R.id.buttonPositive)).perform(click())

        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }
}
