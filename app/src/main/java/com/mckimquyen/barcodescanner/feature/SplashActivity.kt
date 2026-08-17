package com.mckimquyen.barcodescanner.feature

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mckimquyen.barcodescanner.databinding.ActivitySplashBinding
import com.mckimquyen.barcodescanner.feature.tabs.ActivityBottomTabs
import com.roy.sdkadbmob.AdManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var splashStartTime: Long = 0
    private var isNavigating = false
    private var navigationHandler: android.os.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashStartTime = System.currentTimeMillis()
        android.util.Log.i("roy93~", "onCreate Splash")
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Consent (UMP/GDPR) BẮT BUỘC hoàn tất trước khi splash chạy App Open — bỏ bước này = SDK
        // fail-closed, 0 ad. `canRequestAds` không cần check ở đây: initSplashScreen tự biết chặn ad
        // nếu consent chưa đủ, chỉ cần đảm bảo consent đã được request trước.
        AdManager.requestConsentInfoUpdate(this) { canRequestAds ->
            AdManager.initSplashScreen(this) {
                handleAdLoadCompleted()
            }
        }
    }

    private fun handleAdLoadCompleted() {
        if (isNavigating || isFinishing) return

        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - splashStartTime
        val remainingTime = 1000L - elapsedTime // MIN_SPLASH_DURATION = 1000L

        if (remainingTime > 0) {
            navigationHandler = android.os.Handler(android.os.Looper.getMainLooper())
            navigationHandler?.postDelayed({
                goToMain()
            }, remainingTime)
        } else {
            goToMain()
        }
    }

    private fun goToMain() {
        if (isNavigating || isFinishing || isDestroyed) return
        isNavigating = true

        val intent = Intent(this@SplashActivity, ActivityBottomTabs::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        navigationHandler = android.os.Handler(android.os.Looper.getMainLooper())
        navigationHandler?.postDelayed({
            try {
                if (!isFinishing && !isDestroyed) finish()
            } catch (e: Exception) {
            }
        }, 300L) // NAVIGATION_ANIMATION_DURATION
    }

    override fun onBackPressed() {
        // Prevent back button during splash
    }

    override fun onDestroy() {
        navigationHandler?.removeCallbacksAndMessages(null)
        navigationHandler = null
        isNavigating = false
        super.onDestroy()
    }
}
