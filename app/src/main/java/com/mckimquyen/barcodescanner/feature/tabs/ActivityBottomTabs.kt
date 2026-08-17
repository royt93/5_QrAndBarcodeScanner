package com.mckimquyen.barcodescanner.feature.tabs

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.ABottomTabsBinding
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.rateAppInApp
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentCreateBarcode
import com.mckimquyen.barcodescanner.feature.tabs.history.FragmentBarcodeHistory
import com.mckimquyen.barcodescanner.feature.tabs.scan.FragmentScanBarcodeFromCamera
import com.mckimquyen.barcodescanner.feature.tabs.setting.SettingsFragment
import com.roy.sdkadbmob.AdManager

class ActivityBottomTabs : ActivityBase(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ABottomTabsBinding


    companion object {
        private const val ACTION_CREATE_BARCODE = "${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"
        private const val ACTION_HISTORY = "${BuildConfig.APPLICATION_ID}.HISTORY"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ABottomTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportEdgeToEdge()
        initBottomNavigationView()

        if (savedInstanceState == null) {
            showInitialFragment()
        }
        val bannerContainer = findViewById<FrameLayout>(R.id.bannerContainer)
        val tvLabelAd = findViewById<TextView>(R.id.tvLabelAd)
        // autoManageLifecycle=true (mặc định) — SDK tự hook resume/pause/destroy qua
        // ActivityLifecycleCallbacks, KHÔNG tự gọi bannerResume/bannerPause/bannerDestroy nữa.
        AdManager.loadBanner(
            context = this,
            container = bannerContainer,
            tvLabelAd = tvLabelAd,
        )
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == binding.bottomNavigationView.selectedItemId) {
            return false
        }
        showFragment(item.itemId)
        rateAppInApp(BuildConfig.DEBUG)
        return true
    }

    /** Called when ActivityBottomTabs is brought to foreground via FLAG_ACTIVITY_CLEAR_TOP.
     *  e.g. when View History is tapped in ActivityBatchExportResult */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("roy93~", "ActivityBottomTabs.onNewIntent: action=${intent?.action}")
        when (intent?.action) {
            ACTION_HISTORY -> {
                Log.d("roy93~", "ActivityBottomTabs.onNewIntent: switching to History tab")
                binding.bottomNavigationView.selectedItemId = R.id.itemHistory
            }

            ACTION_CREATE_BARCODE -> {
                Log.d("roy93~", "ActivityBottomTabs.onNewIntent: switching to Create tab")
                binding.bottomNavigationView.selectedItemId = R.id.itemCreate
            }

            else -> Log.d("roy93~", "ActivityBottomTabs.onNewIntent: no matching action, ignoring")
        }
    }

    private var doubleBackToExitPressedOnce = false

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.bottomNavigationView.selectedItemId == R.id.itemScan) {
//            super.onBackPressed()
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        } else {
            binding.bottomNavigationView.selectedItemId = R.id.itemScan
        }
    }

    private fun supportEdgeToEdge() {
        binding.bottomNavigationView.applySystemWindowInsets(applyBottom = true)
    }

    private fun initBottomNavigationView() {
        binding.bottomNavigationView.apply {
            setOnItemSelectedListener(this@ActivityBottomTabs)
        }
    }

    private fun showInitialFragment() {
        when (intent?.action) {
            ACTION_CREATE_BARCODE -> binding.bottomNavigationView.selectedItemId = R.id.itemCreate
            ACTION_HISTORY -> binding.bottomNavigationView.selectedItemId = R.id.itemHistory
            else -> showFragment(R.id.itemScan)
        }
    }

    private fun showFragment(bottomItemId: Int) {
        val fragment = when (bottomItemId) {
            R.id.itemScan -> FragmentScanBarcodeFromCamera()
            R.id.itemCreate -> FragmentCreateBarcode()
            R.id.itemHistory -> FragmentBarcodeHistory()
            R.id.itemSettings -> SettingsFragment()
            else -> null
        }
        fragment?.apply(::replaceFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutFragmentContainer, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}
