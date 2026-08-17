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
        }

        // Test Device cho build release (ID thật, luôn cần lưới an toàn code-level — debug đã dùng
        // Google test ad unit ID nên không cần list này). setTestDeviceIds() gọi thẳng
        // RequestConfiguration.Builder().setTestDeviceIds() gốc của Google — API này chỉ nhận đúng
        // HASH (dòng logcat "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(\"...\"))
        // to get test ads on this device" khi chạy app thật trên máy đó), KHÔNG phải GAID dạng UUID
        // thô như AdMob Console "Test devices" hiển thị (Console dùng cho Ad Inspector từ xa — KHÔNG
        // đảm bảo gắn nhãn Test Ad cho request, đã verify thực tế 2026-08-17: máy có GAID trong
        // Console list vẫn nhận được ad thật cho tới khi thêm đúng hash này qua code).
        //
        // ⚠️ CHỈ dòng hash bên dưới đã VERIFY THẬT hoạt động (Pixel 7 Pro, test release 2026-08-17).
        // 29 dòng GAID thô còn lại (copy từ AdMob Console Test devices, team dùng chung nhiều
        // project) CHƯA CHẮC có tác dụng cho lưới an toàn Test Ad — giữ lại làm tham chiếu/để dễ
        // thêm hash đúng khi có, KHÔNG được coi là các máy đó đã an toàn. Muốn 1 máy nào an toàn
        // thật: cầm đúng máy đó chạy app release này 1 lần, copy dòng hash trong logcat (tag "Ads")
        // dán đè/thêm vào đây.
        AdManager.setTestDeviceIds(
            "AB9BC2BCC2E8EC070391DCDD728275A6", // hash Pixel 7 Pro — ĐÃ VERIFY hoạt động thật
            "ace2a4fc-bec9-4271-a744-9f10b25f86f9", // chưa verify (raw GAID, có thể không tác dụng)
            "6db38c1e-03cf-43f5-bcc7-61e474e16a74",
            "04b5ae54-8943-472a-bd6c-27774e9d4bf2",
            "34986618-096D-4B6B-86CD-45E30E904686",
            "40f8e222-cf7a-4fac-9913-6809c4c58817",
            "c09b2f04-e145-490c-96f9-dab620074104",
            "932099db-d381-4b52-98dc-5b96ba8b4ff4",
            "be39dfe0-67f5-4da4-afb3-8407cd481df4", // pixel 7 pro (raw GAID)
            "2acac903-41cc-4f0b-b4b6-7aefc70fe81e",
            "460d3f5c-bbe2-46fc-841a-6381e3c93864",
            "66e652de-79ef-4889-8074-9b482fd81b5a",
            "a1339bd1-8ea5-47cd-969e-4b5721b576b7",
            "49606ad7-5cee-43b4-9af7-8aa274644737",
            "6cf051f8-83f5-43b7-8c1a-1d20ae1f8d93",
            "9b6499f2-d4de-4b9e-afdf-ac2a2b127fb1",
            "261f772c-6a10-499c-b896-4157d9ab6a25",
            "c003454a-62dd-48da-b9ed-56a65c816c51",
            "6fbb207d-341d-470d-bb0a-dddd79522b32",
            "4258512b-457a-4191-8d50-942ec8e444aa",
            "f5a36a2f-5add-4315-a171-0f8dddab78c7",
            "4ed22dd8-e8fb-442e-a75e-081a3d977957",
            "938bee9d-13ca-441c-b858-9a3be0353e12",
            "8f6ccdc1-08fd-4611-abdf-f48bdadb5581",
            "46259467-0ac4-49c4-a3a2-7d3db3ce4bda",
            "adaa42e7-9cc6-4a8a-9c90-d4d87842b12c",
            "1b7c3e3f-c709-4e85-b26f-dd74c4df2ed7",
            "c228aa08-bedd-4e6e-adf6-ae5e95bcddae",
            "da10cb05-5458-42df-ba86-630732356b35",
            "9ad0127d-04be-4b6c-937a-ca3ed7f650b9",
            "3f2f21d2-85eb-451b-a1a5-003668ba6345",
        )
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
