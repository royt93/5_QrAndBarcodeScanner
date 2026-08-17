package com.mckimquyen.barcodescanner.feature.vip

import android.content.Context

/**
 * Chỉ lưu 1 thứ SDK không thể tự biết: user đã tự tay redeem token VIP hay chưa (phân biệt với
 * grace entry cài đặt mới). `grantedAtMs`/`expiry` KHÔNG lưu ở đây — lib đã expose qua
 * AdManager.getVipGrantedAtMs()/getVipByKeyExpiry(), tự lưu lại sẽ lệch khi VIP cấp qua đường
 * khác (vd watch-ad -> grantVipDays) mà VipPrefs app-side không biết.
 */
class VipPrefs(context: Context) {
    private val sp = context.getSharedPreferences("vip_screen_prefs", Context.MODE_PRIVATE)

    fun markUserRedeemed() = sp.edit().putBoolean(KEY_USER_REDEEMED, true).apply()

    fun userRedeemedAtLeastOnce(): Boolean = sp.getBoolean(KEY_USER_REDEEMED, false)

    companion object {
        private const val KEY_USER_REDEEMED = "user_redeemed_once"
    }
}
