package com.mckimquyen.barcodescanner

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.feature.SplashActivity
import com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager
import com.mckimquyen.barcodescanner.sdkadbmob.AppLifecycleListener
import com.mckimquyen.barcodescanner.usecase.Logger
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java

//TODO roy93~ finger print
//TODO roy93~ why you see ad
//TODO roy93~ join beta tester
//TODO roy93~ khi scan image no ko work doi voi hinh anh do chinh app nay tao ra
//TODO roy93~ splash screen

//done mckimquyen
//admob
//font scale
//120hz
//review in app
//ad applovin
//github
//policy
//rate app, share app, more app
//build version
//change pkg name manifest
//leak canary
//proguard
//double to exit app
//ui switch
//change icon launcher
//keystore

class RApp : MultiDexApplication() {

    override fun onCreate() {
        handleUnhandledRxJavaErrors()
        applyTheme()
        super.onCreate()
//        this.setupApplovinAd()
        setupAdmob()
    }

    private fun setupAdmob() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@RApp) {}
            AdMobManager.init(this@RApp) { success, gaidCurrent ->
                Log.d("roy93~", "AdMobManager init success $success, gaidCurrent $gaidCurrent")
            }
        }
//        registerActivityLifecycleCallbacks(
//            AppLifecycleListener(
//                { isForeground, activity ->
//                    if (isForeground) {
//                        Log.d("roy93~", "App moved to Foreground")
//                        Log.d("roy93~", "activity.localClassName ${activity.localClassName}")
//                        Log.d(
//                            "roy93~",
//                            "SplashActivity::class.java.simpleName ${SplashActivity::class.java.simpleName}"
//                        )
//                        if (activity.localClassName == SplashActivity::class.java.simpleName) {
//                            //do nothing
//                        } else {
////                            AdMobManager.showAppOpenAd(activity)
//                        }
//                    } else {
//                        Log.d("roy93~", "App moved to Background")
//                    }
//                }, { activity ->
//                    Log.d("roy93~", "callbackActivityCreated ${activity.localClassName}")
//                    if (activity.localClassName == SplashActivity::class.java.simpleName) {
//                        //do nothing
//                    } else {
////                        AdMobManager.loadAppOpenAd(
////                            context = this,
////                            adUnitId = BuildConfig.ADMOB_APP_OPEN_ID,
////                            onAdLoaded = {},
////                        )
//                    }
//                }
//            )
//        )
    }

    private fun applyTheme() {
        settings.reapplyTheme()
    }

    private fun handleUnhandledRxJavaErrors() {
        RxJavaPlugins.setErrorHandler { error ->
            Logger.log(error)
        }
    }
}
