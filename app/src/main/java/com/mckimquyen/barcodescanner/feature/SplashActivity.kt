package com.mckimquyen.barcodescanner.feature

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.databinding.ActivitySplashBinding
import com.mckimquyen.barcodescanner.feature.tabs.ActivityBottomTabs
import com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("roy93~", "onCreate")
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdMobManager.initSplashScreen(this, {
            goToMain()
        })
    }

    private fun goToMain() {
        val intent = Intent(this@SplashActivity, ActivityBottomTabs::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finishAffinity()
    }
}
