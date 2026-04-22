package com.mckimquyen.barcodescanner.feature

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.databinding.ActivitySplashBinding
import com.mckimquyen.barcodescanner.feature.tabs.ActivityBottomTabs
import com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager
import com.mckimquyen.barcodescanner.sdkadbmob.Logger

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var finishRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i("onCreate")
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdMobManager.initSplashScreen(this, {
            goToMain()
        })
    }

    private fun goToMain() {
        val intent = Intent(this@SplashActivity, ActivityBottomTabs::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // Trì hoãn finish để đợi animation hoàn tất
        finishRunnable = Runnable { finish() }
        window.decorView.postDelayed(finishRunnable, 300) // delay khoảng 300ms (hoặc đúng thời gian của animation)
    }

    override fun onDestroy() {
        finishRunnable?.let { window.decorView.removeCallbacks(it) }
        finishRunnable = null
        super.onDestroy()
    }
}
