package com.mckimquyen.barcodescanner

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.ext.URL_POLICY_NOTION
import com.mckimquyen.barcodescanner.feature.SplashActivity
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.AdSafetyLimits
import com.roy.sdkadbmob.AdSdkConfig
import com.roy.sdkadbmob.ErrorReporter
import com.roy.sdkadbmob.PaidEventListener
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
        setupAds()
    }

    private fun setupAds() {
        // Provider: AdMob (BuildConfig.IS_ENABLE_ADMOB=true) — AppLovin vẫn set đủ ID để giữ song song
        // làm phương án dự phòng/so sánh doanh thu (quyết định 2026-08-17, xem doc/AD.MD mục 5).
        val adConfig = AdSdkConfig(
            isEnableAdmob = BuildConfig.IS_ENABLE_ADMOB,
            isDebug = BuildConfig.DEBUG,

            admobBannerId = BuildConfig.ADMOB_BANNER_ID,
            admobInterstitialId = BuildConfig.ADMOB_INTERSTITIAL_ID,
            admobAppOpenId = BuildConfig.ADMOB_APP_OPEN_ID,
            admobRewardedId = BuildConfig.ADMOB_REWARDED_ID,

            applovinSdkKey = BuildConfig.APPLOVIN_SDK_KEY,
            applovinBannerId = BuildConfig.APPLOVIN_BANNER_ID,
            applovinInterstitialId = BuildConfig.APPLOVIN_INTERSTITIAL_ID,
            applovinAppOpenId = BuildConfig.APPLOVIN_APP_OPEN_ID,
            applovinRewardedId = BuildConfig.APPLOVIN_REWARD_ID,
            applovinPrivacyPolicyUrl = URL_POLICY_NOTION,
            // applovinHasUserConsent: giữ null — flavor gms dùng UMP ở SplashActivity quyết định qua
            // requestConsentInfoUpdate(), KHÔNG set cứng ở đây.

            vipKeySecret = BuildConfig.VIP_KEY_SECRET,
            vipTokenPublicKey = BuildConfig.VIP_TOKEN_PUBLIC_KEY,

            appOpenExcludedActivities = listOf(SplashActivity::class.java),

            safety = if (BuildConfig.DEBUG) AdSafetyLimits.TEST else AdSafetyLimits.UTILITY,
        )

        AdManager.setConfig(adConfig)

        // 💰 paidEventListener PHẢI set ở Application.onCreate (KHÔNG set trong Activity) — SDK tự xoá
        // listener khi Activity "chủ sở hữu" lúc set bị destroy.
        AdManager.paidEventListener = PaidEventListener { adType, valueMicros, currency, precision, adSource ->
            Log.d("AdsRevenue", "$adType $valueMicros $currency $precision $adSource")
        }
        AdManager.errorReporter = ErrorReporter { throwable, _ ->
            UsecaseLogger.log(throwable)
        }
        // setPendingVipTokenResultListener KHÔNG set ở đây — SDK gán "chủ sở hữu" listener theo
        // currentActivity, mà ở Application.onCreate() chưa có Activity nào cả. ActVipManagement tự
        // đăng ký/gỡ listener này trong onCreate()/onDestroy() của chính nó (xem class đó).

        AdManager.initialize(this) { success, gaid ->
            Log.d("RApp", "AdManager initialize success=$success, gaid=$gaid")
            if (BuildConfig.DEBUG) {
                // ⚠️ THAY bằng GAID thật của máy dev/QA trước khi tự click ad test — xem doc/AD.MD mục 6.
                // AdManager.setTestDeviceIds("GAID_MAY_DEV", "GAID_MAY_QA")
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
