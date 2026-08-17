package com.mckimquyen.barcodescanner.feature.vip

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VipPrefsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Start each test from a clean slate — VipPrefs writes to a fixed, well-known prefs file.
        context.getSharedPreferences("vip_screen_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun userRedeemedAtLeastOnce_defaultsToFalse() {
        assertFalse(VipPrefs(context).userRedeemedAtLeastOnce())
    }

    @Test
    fun markUserRedeemed_persistsAcrossInstances() {
        VipPrefs(context).markUserRedeemed()

        // A fresh VipPrefs instance must see the same persisted flag — this is what distinguishes
        // a first-install grace VIP entry from a user-redeemed one in ActVipManagement.bindUi().
        assertTrue(VipPrefs(context).userRedeemedAtLeastOnce())
    }
}
