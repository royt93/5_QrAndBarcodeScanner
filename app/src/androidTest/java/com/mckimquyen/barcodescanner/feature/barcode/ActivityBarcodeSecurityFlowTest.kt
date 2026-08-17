package com.mckimquyen.barcodescanner.feature.barcode

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end coverage for the Smart Security Shield URL-open flow:
 * safe domains open directly, unsafe domains are gated by DialogFragmentSecurityAlert.
 */
@RunWith(AndroidJUnit4::class)
class ActivityBarcodeSecurityFlowTest {

    private var scenario: ActivityScenario<ActivityBarcode>? = null

    @Before
    fun setUp() {
        Intents.init()
        // Prevent the test from actually switching to a real browser app.
        intending(not(isInternal())).respondWith(
            android.app.Instrumentation.ActivityResult(0, null)
        )
    }

    @After
    fun tearDown() {
        scenario?.close()
        Intents.release()
    }

    private fun launchWithUrl(url: String): ActivityScenario<ActivityBarcode> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val barcode = Barcode(
            text = url,
            formattedText = url,
            format = BarcodeFormat.QR_CODE,
            schema = BarcodeSchema.URL,
            date = 0L,
        )
        val intent = Intent(context, ActivityBarcode::class.java).apply {
            putExtra("BARCODE_KEY", barcode)
            putExtra("IS_CREATED", false)
        }
        return ActivityScenario.launch<ActivityBarcode>(intent).also { scenario = it }
    }

    @Test
    fun safeDomain_opensDirectly_withoutSecurityDialog() {
        val url = "https://www.google.com/search?q=test"
        launchWithUrl(url)

        onView(withId(R.id.buttonOpenLink)).perform(click())

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))))
        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }

    @Test
    fun unsafeDomain_showsSecurityDialog_andProceedOpensLink() {
        val url = "https://evil.com/google.com"
        launchWithUrl(url)

        onView(withId(R.id.buttonOpenLink)).perform(click())
        onView(withText(R.string.security_alert_title)).check(matches(isDisplayed()))

        onView(withId(R.id.buttonPositive)).perform(click())

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))))
        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }

    @Test
    fun unsafeDomain_cancelNeverOpensLink() {
        val url = "https://google.com.evil.net"
        launchWithUrl(url)

        onView(withId(R.id.buttonOpenLink)).perform(click())
        onView(withText(R.string.security_alert_title)).check(matches(isDisplayed()))

        onView(withId(R.id.buttonNegative)).perform(click())

        onView(withText(R.string.security_alert_title)).check(doesNotExist())
        intended(
            allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))),
            Intents.times(0),
        )
    }

    @Test
    fun vnBankDomain_opensDirectly_withoutSecurityDialog() {
        val url = "https://www.vietcombank.com.vn/login"
        launchWithUrl(url)

        onView(withId(R.id.buttonOpenLink)).perform(click())

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))))
        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }

    // Regression guard: textViewBarcodeText used to have android:autoLink="all", which let the
    // system Linkify/URLSpan fire Intent.ACTION_VIEW directly, completely bypassing the
    // UrlSafetyChecker gate that buttonOpenLink already went through.
    @Test
    fun unsafeDomain_tappingBarcodeText_alsoShowsSecurityDialog() {
        val url = "https://evil.com/google.com"
        launchWithUrl(url)

        onView(withId(R.id.textViewBarcodeText)).perform(click())

        onView(withText(R.string.security_alert_title)).check(matches(isDisplayed()))
        intended(
            allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))),
            Intents.times(0),
        )
    }

    @Test
    fun safeDomain_tappingBarcodeText_opensDirectly_withoutSecurityDialog() {
        val url = "https://www.google.com/search?q=test"
        launchWithUrl(url)

        onView(withId(R.id.textViewBarcodeText)).perform(click())

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Uri.parse(url))))
        onView(withText(R.string.security_alert_title)).check(doesNotExist())
    }
}
