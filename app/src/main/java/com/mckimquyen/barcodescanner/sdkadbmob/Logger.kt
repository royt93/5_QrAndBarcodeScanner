package com.mckimquyen.barcodescanner.sdkadbmob

import android.util.Log
import com.mckimquyen.barcodescanner.BuildConfig

object Logger {
    fun i(s: String) {
        if (BuildConfig.DEBUG) {
            Log.d("roy93~", s)
        }
    }
}