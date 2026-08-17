package com.mckimquyen.barcodescanner.feature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.rotationHelper
import com.mckimquyen.barcodescanner.usecase.LocaleHelper
import java.util.Calendar

abstract class ActivityBase : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        rotationHelper.lockCurrentOrientationIfNeeded(this)
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply language setting - Read directly from SharedPreferences to avoid initialization issues
        val sharedPreferences = newBase.getSharedPreferences("SHARED_PREFERENCES_NAME", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("LANGUAGE", "system") ?: "system"
        android.util.Log.i("roy93~", "attachBaseContext - Language: $language")
        val localeContext = LocaleHelper.setLocale(newBase, language)

        // Apply font scale override
        val override = Configuration(localeContext.resources.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
        super.attachBaseContext(localeContext)
    }

    // Override to customize the finish() transition (default: slide-out-right)
    open val exitTransition: Pair<Int, Int> = R.anim.slide_in_left to R.anim.slide_out_right

    override fun finish() {
        super.finish()
        val (enterAnim, exitAnim) = exitTransition
        overridePendingTransition(enterAnim, exitAnim)
    }

    override fun onResume() {
        super.onResume()
        com.roy.sdkadbmob.AdManager.setCurrentActivity(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableAdaptiveRefreshRate()
        }
    }

    private fun enableAdaptiveRefreshRate() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display // Sử dụng API mới
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay // Fallback cho API thấp hơn
        }

        if (display != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val supportedModes = display.supportedModes
                val highestRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }
                if (highestRefreshRateMode != null) {
                    window.attributes = window.attributes.apply {
                        preferredDisplayModeId = highestRefreshRateMode.modeId
                    }
                    println("Adaptive refresh rate applied: ${highestRefreshRateMode.refreshRate} Hz")
                }
            }
        }
    }
}

fun Context.startActivitySlideRight(intent: Intent) {
    startActivity(intent)
    (this as? Activity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Context.startActivitySlideUp(intent: Intent) {
    startActivity(intent)
    // enterAnim=0: old screen stays in place (modal style); new screen slides up over it
    (this as? Activity)?.overridePendingTransition(R.anim.slide_in_up, 0)
}

//rateAppInApp(BuildConfig.DEBUG)
fun Activity.rateAppInApp(forceRateInApp: Boolean = false) {
    //import gradle app
//    implementation("com.google.android.play:review:2.0.2")
//    implementation("com.google.android.play:review-ktx:2.0.2")

    val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val lastReviewTime = sharedPreferences.getLong("last_review_time", 0L)
//    android.util.Log.i("roy93~", "requestReview lastReviewTime $lastReviewTime")
    val currentTime = Calendar.getInstance().timeInMillis
    val daysSinceLastReview = (currentTime - lastReviewTime) / (1000 * 60 * 60 * 24)
//    android.util.Log.i("roy93~", "requestReview forceRateInApp $forceRateInApp")
//    android.util.Log.i("roy93~", "requestReview daysSinceLastReview $daysSinceLastReview")
    if (daysSinceLastReview >= 7 || forceRateInApp) {
//    if (daysSinceLastReview >= 7) {
        val reviewManager = ReviewManagerFactory.create(this)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    val reviewInfo: ReviewInfo = task.result
                    reviewManager.launchReviewFlow(this, reviewInfo)
                    sharedPreferences.edit().putLong("last_review_time", currentTime).apply()
//                    android.util.Log.i("roy93~", "requestReview result ${task.result}")
//                    android.util.Log.i("roy93~", "requestReview isSuccessful ${task.isSuccessful}")
//                    android.util.Log.i("roy93~", "requestReview isCanceled ${task.isCanceled}")
//                    android.util.Log.i("roy93~", "requestReview isComplete ${task.isComplete}")
//                    android.util.Log.i("roy93~", "requestReview exception ${task.exception}")
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException?)?.errorCode
//                    Log.e("roy93~", "requestReview error $reviewErrorCode")
                }
            } catch (e: Exception) {
//                Log.e("roy93~", "rateAppInApp e $e")
            }
        }
    }
}
