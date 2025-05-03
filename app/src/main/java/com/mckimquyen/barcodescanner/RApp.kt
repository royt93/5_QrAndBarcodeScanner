package com.mckimquyen.barcodescanner

import androidx.multidex.MultiDexApplication
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.usecase.Logger
import io.reactivex.plugins.RxJavaPlugins

//TODO roy93~ finger print
//TODO roy93~ why you see ad
//TODO roy93~ join beta tester
//TODO roy93~ khi scan image no ko work doi voi hinh anh do chinh app nay tao ra
//TODO roy93~ splash screen

//done mckimquyen
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
        //TODO roy93~ admob setup
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
