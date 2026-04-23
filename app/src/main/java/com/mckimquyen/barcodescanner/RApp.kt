package com.mckimquyen.barcodescanner

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.applovin.sdk.AppLovinSdk
import com.mckimquyen.barcodescanner.di.settings
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.AdSdkConfig
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import com.mckimquyen.barcodescanner.usecase.Logger as UsecaseLogger

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
        val adConfig = AdSdkConfig(
            isEnableAdmob = false,
            isDebug = BuildConfig.DEBUG,
            admobBannerId = "",
            admobInterstitialId = "",
            admobAppOpenId = "",
            applovinBannerId = BuildConfig.BANNER,
            applovinInterstitialId = BuildConfig.INTER,
            applovinAppOpenId = BuildConfig.APPOPEN
        )

        AdManager.setConfig(adConfig)
        AdManager.earlyInit(this)

        if (false) {
            com.google.android.gms.ads.MobileAds.initialize(this) {
                initAdManager(adConfig)
            }
        } else {
            Log.d("RApp", "AppLovin mode, initializing AppLovinSdk")
            val initConfig = com.applovin.sdk.AppLovinSdkInitializationConfiguration.builder(
                BuildConfig.APPLOVIN_SDK_KEY,
                this
            )
                .setMediationProvider(com.applovin.sdk.AppLovinMediationProvider.MAX)
                .build()
            AppLovinSdk.getInstance(this).initialize(initConfig) {
                initAdManager(adConfig)
            }
        }
    }

    private fun initAdManager(adConfig: AdSdkConfig) {
        AdManager.init(this@RApp, adConfig) { success, gaid ->
            Log.d("RApp", "AdManager start success=$success, gaid=$gaid")
            if (success) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    AdManager.registerAppOpenAdLifecycle(this@RApp)
                }
            }
        }
    }

    private fun applyTheme() {
        settings.reapplyTheme()
    }

    private fun handleUnhandledRxJavaErrors() {
        RxJavaPlugins.setErrorHandler { error ->
            UsecaseLogger.log(error)
        }
    }
}
