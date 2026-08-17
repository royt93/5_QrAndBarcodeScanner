package com.mckimquyen.barcodescanner.feature.vip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.AVipManagementBinding
import com.mckimquyen.barcodescanner.extension.ext.openBrowserPolicy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideRight
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.AppPreferences
import com.roy.sdkadbmob.NetworkUtils
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * Màn VIP Management — UI mirror trực tiếp từ demo app của chính SDK
 * (royt93/AdmobApplovinWrapper app/src/main/java/com/roy/admobwrapper/vip/ActVipManagement.kt) để
 * nhất quán thiết kế + tái dùng các fix race-condition/UX SDK author đã tự kiểm chứng
 * (ActVipManagementInstrumentedTest/AnimationTest trong repo SDK). Khác biệt cố ý so với bản gốc:
 * dùng token ECDSA (không có nhánh demo sample-token debug), dùng ActivityBase + slide transition
 * sẵn có của app thay vì transition tức thời riêng của demo, dùng applySystemWindowInsets nội bộ.
 */
class ActVipManagement : ActivityBase() {

    private val tag = "roy93~VipScreen"

    private lateinit var binding: AVipManagementBinding
    private lateinit var vipPrefs: VipPrefs

    private var countDownTimer: CountDownTimer? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var crownShimmerAnimator: ObjectAnimator? = null
    private var shimmerAnimator: ValueAnimator? = null
    private var strokePulseAnimator: ValueAnimator? = null
    private var countUpAnimator: ValueAnimator? = null
    private var slideInAnimator: ValueAnimator? = null

    private var activateRunnable: Runnable? = null
    private var verifyingDialog: androidx.appcompat.app.AlertDialog? = null

    private var lastMinute: Int? = null

    /** Mốc hạn VIP TRƯỚC lần activate đang xếp hàng — dùng khi pendingVipTokenResultListener báo kết quả thật. */
    private var expiryBeforeQueuedActivate: Long? = null

    /**
     * Số ngày VIP vừa cấp nhưng CHƯA kịp chúc mừng — dialog+confetti phải đợi ad đóng hẳn (ad vẫn
     * phủ màn hình lúc callback earned fire, vì grantRewardOnEarn=true từ 1.6.1). null = không có
     * gì chờ, 0 = grant thất bại.
     */
    private var pendingVipCelebrationDays: Int? = null

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun start(context: Context) {
            val intent = Intent(context, ActVipManagement::class.java)
            context.startActivitySlideRight(intent)
        }

        /**
         * 3 mốc trên timeline ngang [grantedAt --- expiresAt]: start=grantedAt (0%), end=expiresAt
         * (100%), current=now (vị trí fill). Bar RỖNG lúc vừa kích hoạt, ĐẦY DẦN đến lúc hết hạn.
         * `internal` + companion (không phụ thuộc instance) để unit test trực tiếp không cần
         * Android runtime — xem ComputeElapsedProgressTest.
         */
        @androidx.annotation.VisibleForTesting
        internal fun computeElapsedProgress(grantedAtMs: Long, expiresAtMs: Long, nowMs: Long): Int {
            val total = expiresAtMs - grantedAtMs
            if (total <= 0L) return 100
            val elapsed = nowMs - grantedAtMs
            return ((elapsed.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = AVipManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vipPrefs = VipPrefs(this)

        supportEdgeToEdge()

        // BẮT BUỘC gọi setCurrentActivity(this) TRƯỚC setPendingVipTokenResultListener — nếu không,
        // AdManager gán owner của listener theo currentActivity HIỆN TẠI, mà tại thời điểm onCreate()
        // này chạy, ActivityLifecycleCallbacks tự-track CHƯA kịp cập nhật currentActivity thành chính
        // Activity này (chỉ fire SAU KHI onCreate() trả về, ở onResume). Owner khi đó vẫn trỏ Activity
        // TRƯỚC — Activity đó destroy (điều hướng bình thường) sẽ tự xoá NHẦM listener của màn này,
        // token dán ngay lúc mở màn có thể bị cấp VIP thầm lặng không ai biết.
        AdManager.setCurrentActivity(this)
        AdManager.setPendingVipTokenResultListener { activated ->
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                val before = expiryBeforeQueuedActivate
                expiryBeforeQueuedActivate = null
                binding.btnActivate.isEnabled = binding.etVipKey.text?.isNotEmpty() == true
                when {
                    activated && before != null -> {
                        val baseline = maxOf(before, System.currentTimeMillis())
                        val addedDays = ceil(
                            (AdManager.getVipByKeyExpiry() - baseline).toDouble() / 86_400_000.0
                        ).toInt().coerceAtLeast(0)
                        vipPrefs.markUserRedeemed()
                        if (addedDays > 0) showActivationSuccess(addedDays) else showTokenNoEffectDialog()
                    }

                    activated -> showActivationSuccess(0)
                    else -> showActivationFailed()
                }
            }
        }

        setupToolbar()
        setupClickListeners()
        setupInputListeners()
        bindUi()
        triggerSlideInAnimation()
    }

    override fun onResume() {
        super.onResume()
        startLoopAnimations()
        bindUi()
        // (C9-04) Ad vừa đóng → giờ mới an toàn bật dialog/confetti. bindUi() chạy trước để header
        // hiện trạng thái VIP mới trước khi dialog phủ lên.
        flushPendingVipCelebration()
        AdManager.loadRewarded(this)
    }

    override fun onPause() {
        super.onPause()
        cancelLoopAnimations()
    }

    override fun onDestroy() {
        // Closure của listener giữ `this` — không gỡ thì AdManager (singleton, sống suốt process)
        // giữ Activity đã destroy, rò rỉ.
        AdManager.setPendingVipTokenResultListener(null)
        activateRunnable?.let { binding.root.removeCallbacks(it) }
        activateRunnable = null
        verifyingDialog?.dismiss()
        verifyingDialog = null
        countDownTimer?.cancel(); countDownTimer = null
        cancelLoopAnimations()
        slideInAnimator?.cancel(); slideInAnimator = null
        countUpAnimator?.cancel(); countUpAnimator = null
        super.onDestroy()
    }

    private fun supportEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutVipRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            binding.contentContainer.setPadding(
                binding.contentContainer.paddingLeft,
                binding.contentContainer.paddingTop,
                binding.contentContainer.paddingRight,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnActivate.setOnClickListener {
            val inputToken = binding.etVipKey.text?.toString()?.trim() ?: ""
            if (inputToken.isEmpty()) {
                binding.tilVipKey.error = getString(R.string.vip_redeem_hint)
                return@setOnClickListener
            }
            binding.tilVipKey.error = null

            verifyingDialog?.dismiss()
            val progressDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.vip_verifying_title)
                .setView(R.layout.v_progress_dialog)
                .setCancelable(false)
                .create()
            verifyingDialog = progressDialog
            progressDialog.show()

            activateRunnable?.let { binding.root.removeCallbacks(it) }
            activateRunnable = Runnable {
                if (isFinishing || isDestroyed) return@Runnable
                progressDialog.dismiss()
                verifyingDialog = null
                // activateVipByToken() dùng SharedPreferences commit() đồng bộ — chạy ngoài main
                // thread để tránh StrictMode violation/jank.
                val expiryBeforeActivate = AdManager.getVipByKeyExpiry()
                // Lưu mốc NGAY tại đây, trên main thread, TRƯỚC khi thread nền chạy — pendingVipToken
                // listener có thể fire sớm hơn nếu lưu muộn, gây race đọc `null`.
                expiryBeforeQueuedActivate = expiryBeforeActivate
                binding.btnActivate.isEnabled = false
                Thread {
                    val success = AdManager.activateVipByToken(this, inputToken)
                    runOnUiThread {
                        if (isFinishing || isDestroyed) return@runOnUiThread
                        if (success) {
                            expiryBeforeQueuedActivate = null
                            binding.btnActivate.isEnabled = binding.etVipKey.text?.isNotEmpty() == true
                            vipPrefs.markUserRedeemed()
                            val addedDays = ceil(
                                (AdManager.getVipByKeyExpiry() - maxOf(expiryBeforeActivate, System.currentTimeMillis())).toDouble() / 86_400_000.0
                            ).toInt().coerceAtLeast(0)
                            if (addedDays > 0) showActivationSuccess(addedDays) else showTokenNoEffectDialog()
                        } else if (AdManager.isVipTokenPending) {
                            // Token HỢP LỆ nhưng SDK chưa init xong nên bị xếp hàng — `false` ở đây
                            // KHÔNG có nghĩa là từ chối. Kết quả thật tới qua listener đăng ký ở onCreate.
                            showTokenQueuedDialog()
                        } else {
                            expiryBeforeQueuedActivate = null
                            binding.btnActivate.isEnabled = binding.etVipKey.text?.isNotEmpty() == true
                            showActivationFailed()
                        }
                    }
                }.start()
                activateRunnable = null
            }
            binding.root.postDelayed(activateRunnable!!, 1000)
        }

        binding.btnRevokeVip.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.vip_revoke_all_confirm_title)
                .setMessage(R.string.vip_revoke_all_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    AdManager.clearVipByKey()
                    bindUi()
                    infoDialog(R.string.vip_revoked_message)
                }
                .show()
        }

        binding.btnWatchAd.setOnClickListener {
            if (!NetworkUtils.isDeviceConnected(this)) {
                showNoAdDialog()
                return@setOnClickListener
            }
            AdManager.showRewarded(this) { earned ->
                if (isFinishing || isDestroyed) return@showRewarded
                if (earned) {
                    grantVipFromAd()
                } else {
                    // Fallback interstitial — monetization-only, TUYỆT ĐỐI KHÔNG cấp reward (policy A-15).
                    AdManager.showInterstitial(this) {
                        if (isFinishing || isDestroyed) return@showInterstitial
                        showNoRewardDialog()
                    }
                }
            }
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            try {
                openBrowserPolicy()
            } catch (e: Exception) {
                Log.w(tag, "open privacy policy failed: ${e.message}")
                infoDialog(R.string.vip_open_link_error)
            }
        }
    }

    private fun setupInputListeners() {
        val initialKey = binding.etVipKey.text?.toString()?.trim() ?: ""
        binding.btnActivate.isEnabled = initialKey.isNotEmpty()

        binding.etVipKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString()?.trim() ?: ""
                binding.btnActivate.isEnabled = input.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun grantVipFromAd() {
        // Cấp VIP từ nguồn tin cậy nội bộ (user vừa xem xong rewarded ad) qua API chuyên dụng —
        // KHÔNG dùng activateVipByKey(vipKeySecret) (nhánh legacy plaintext default TẮT, fail im lặng).
        val success = AdManager.grantVipDays(this, 3)
        if (success) vipPrefs.markUserRedeemed()
        pendingVipCelebrationDays = if (success) 3 else 0
    }

    private fun flushPendingVipCelebration() {
        val days = pendingVipCelebrationDays ?: return
        pendingVipCelebrationDays = null
        if (days > 0) showActivationSuccess(days) else showActivationFailed()
    }

    // ---------------------------------------------------------------------
    // UI state
    // ---------------------------------------------------------------------

    private fun bindUi() {
        val isVip = AdManager.isVipByKeyActive()
        val expiryMs = AdManager.getVipByKeyExpiry()
        // Nguồn sự thật là SDK (ghi từ MỌI grant path: token/grantVipDays/legacy-key), không phải
        // pref riêng của UI.
        val grantedAtMs = AdManager.getVipGrantedAtMs()

        if (isVip && expiryMs > System.currentTimeMillis()) {
            binding.layoutStatusHeaderBackground.setBackgroundResource(R.drawable.bg_vip_status_header_active)
            binding.cardStatusHeader.strokeWidth = resources.getDimensionPixelSize(R.dimen.vip_gold_stroke_width)
            binding.cardStatusHeader.strokeColor = 0xFFD4AF37.toInt()
            binding.imgCrown.imageTintList = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
            binding.imgCrownGlow.visibility = View.VISIBLE
            binding.tvVipBadge.visibility = View.VISIBLE
            binding.tvStatusTitle.setTextColor(0xFFFFFFFF.toInt())
            binding.tvStatusEyebrow.setTextColor(0xFFFFE87A.toInt())
            binding.tvStatusEyebrow.alpha = 1.0f
            binding.tvStatusTitle.text = getString(R.string.vip_active)

            val formattedExpiry = DATE_FORMAT.format(Date(expiryMs))
            binding.tvStatusSubtitle.text = getString(R.string.vip_until, formattedExpiry)
            binding.tvStatusSubtitle.visibility = View.VISIBLE

            binding.progressVip.visibility = View.VISIBLE
            binding.tvCountdown.visibility = View.VISIBLE
            binding.cardVipDetails.visibility = View.VISIBLE

            binding.btnWatchAd.isEnabled = false

            val isFirstInstallGrace = AppPreferences.getInstance(this).isAddVIPMemberFirstInitSuccess() &&
                !vipPrefs.userRedeemedAtLeastOnce()

            if (isFirstInstallGrace) {
                binding.tvActiveVipLabel.text = getString(R.string.vip_entry_first_install)
            } else {
                val durationDays = ceil((expiryMs - grantedAtMs).toDouble() / 86_400_000.0).toInt().coerceAtLeast(0)
                binding.tvActiveVipLabel.text =
                    resources.getQuantityString(R.plurals.vip_entry_redeemed, durationDays, durationDays)
            }

            val formattedGranted = DATE_FORMAT.format(Date(grantedAtMs))
            binding.tvActivatedAt.text = getString(R.string.vip_activated_at, formattedGranted)
            binding.tvExpiresAt.text = getString(R.string.vip_expires_at, formattedExpiry)

            startCountdown(grantedAtMs, expiryMs)
        } else {
            binding.layoutStatusHeaderBackground.setBackgroundResource(R.drawable.bg_vip_status_header_free)
            strokePulseAnimator?.cancel(); strokePulseAnimator = null
            binding.cardStatusHeader.strokeWidth = 0
            binding.imgCrown.imageTintList = android.content.res.ColorStateList.valueOf(0xFF9B6200.toInt())
            binding.imgCrownGlow.visibility = View.GONE
            binding.tvVipBadge.visibility = View.GONE
            binding.tvStatusTitle.setTextColor(0xFF5C3500.toInt())
            binding.tvStatusEyebrow.setTextColor(0xFF7A4800.toInt())
            binding.tvStatusEyebrow.alpha = 0.9f
            binding.tvStatusTitle.text = getString(R.string.vip_free_user)
            binding.tvStatusSubtitle.visibility = View.GONE
            binding.progressVip.visibility = View.GONE
            binding.tvCountdown.visibility = View.GONE
            binding.cardVipDetails.visibility = View.GONE

            binding.btnWatchAd.isEnabled = true

            countDownTimer?.cancel(); countDownTimer = null
        }
    }

    private fun startCountdown(grantedAtMs: Long, expiryMs: Long) {
        countDownTimer?.cancel()
        val remainingMs = expiryMs - System.currentTimeMillis()
        if (remainingMs <= 0) {
            bindUi()
            return
        }

        countDownTimer = object : CountDownTimer(remainingMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val now = System.currentTimeMillis()
                binding.progressVip.setProgressCompat(computeElapsedProgress(grantedAtMs, expiryMs, now), true)

                val days = millisUntilFinished / (24 * 3600 * 1000)
                val hours = (millisUntilFinished % (24 * 3600 * 1000)) / (3600 * 1000)
                val minutes = (millisUntilFinished % (3600 * 1000)) / (60 * 1000)
                val seconds = (millisUntilFinished % (60 * 1000)) / 1000

                binding.tvCountdown.text = getString(
                    R.string.vip_remaining, days.toInt(), hours.toInt(), minutes.toInt(), seconds.toInt()
                )

                val currentMin = minutes.toInt()
                if (lastMinute != null && lastMinute != currentMin) {
                    triggerCountUpAnimation()
                }
                lastMinute = currentMin
            }

            override fun onFinish() {
                bindUi()
            }
        }.start()
    }

    // ---------------------------------------------------------------------
    // Animation (5 bắt buộc theo spec Step 10.4)
    // ---------------------------------------------------------------------

    // #1 + #3: pulse/rotate khi free, breathing crown + gold shimmer + stroke pulse khi VIP active
    private fun startLoopAnimations() {
        cancelLoopAnimations()

        if (AdManager.isVipByKeyActive()) {
            crownShimmerAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.imgCrown,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.15f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.15f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.80f, 1.0f),
            ).apply {
                duration = 2200L
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            strokePulseAnimator = ValueAnimator.ofArgb(0xFFA07000.toInt(), 0xFFFFE040.toInt()).apply {
                duration = 2200L
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { va ->
                    if (!isDestroyed && !isFinishing) {
                        binding.cardStatusHeader.strokeColor = va.animatedValue as Int
                    }
                }
                start()
            }

            binding.viewShimmer.visibility = View.VISIBLE
            binding.viewShimmer.cornerRadiusPx = 28f * resources.displayMetrics.density
            shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 3500L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = android.view.animation.LinearInterpolator()
                startDelay = 700L
                addUpdateListener { va ->
                    if (!isDestroyed && !isFinishing) {
                        binding.viewShimmer.shimmerProgress = va.animatedValue as Float
                    }
                }
                start()
            }
        } else {
            crownShimmerAnimator = ObjectAnimator.ofFloat(binding.imgCrown, View.ROTATION, -8f, 8f).apply {
                duration = 2000L
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.btnWatchAd,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.04f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.04f),
            ).apply {
                duration = 1200L
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun cancelLoopAnimations() {
        pulseAnimator?.cancel(); pulseAnimator = null
        crownShimmerAnimator?.cancel(); crownShimmerAnimator = null
        shimmerAnimator?.cancel(); shimmerAnimator = null
        strokePulseAnimator?.cancel(); strokePulseAnimator = null
        binding.viewShimmer.visibility = View.INVISIBLE
        binding.viewShimmer.shimmerProgress = 0f
    }

    // #2: slide-in từ dưới khi mở màn hình
    private fun triggerSlideInAnimation() {
        slideInAnimator?.cancel()

        binding.cardStatusHeader.translationY = 400f
        binding.cardStatusHeader.alpha = 0f
        binding.cardActivation.translationY = 600f
        binding.cardActivation.alpha = 0f
        binding.btnWatchAd.translationY = 800f
        binding.btnWatchAd.alpha = 0f

        slideInAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900L
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float

                val f1 = (fraction / 0.5f).coerceIn(0f, 1f)
                binding.cardStatusHeader.translationY = (1f - f1) * 400f
                binding.cardStatusHeader.alpha = f1

                val f2 = ((fraction - 0.2f) / 0.6f).coerceIn(0f, 1f)
                binding.cardActivation.translationY = (1f - f2) * 600f
                binding.cardActivation.alpha = f2

                val f3 = ((fraction - 0.4f) / 0.6f).coerceIn(0f, 1f)
                binding.btnWatchAd.translationY = (1f - f3) * 800f
                binding.btnWatchAd.alpha = f3
            }
            start()
        }
    }

    // #4: count-up bounce khi countdown đổi phút
    private fun triggerCountUpAnimation() {
        countUpAnimator?.cancel()
        countUpAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            addUpdateListener { animator ->
                val scale = 1f + (animator.animatedFraction * 0.1f)
                binding.tvCountdown.scaleX = scale
                binding.tvCountdown.scaleY = scale
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.tvCountdown.scaleX = 1.0f
                    binding.tvCountdown.scaleY = 1.0f
                }
            })
            start()
        }
    }

    // #5: confetti + haptic khi activate thành công
    private fun triggerSuccessEffect() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            angle = 270,
            spread = 360,
            colors = listOf(0xFFD700, 0xFFC200, 0xFFE87A, 0xFFFFFF, 0xFF9F00, 0xFFF5CC),
            position = Position.Relative(0.5, 0.3),
            emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(150),
        )
        binding.viewKonfetti.start(party)
        performHaptic()
    }

    private fun performHaptic() {
        // API >= R (30) đã có performHapticFeedback. minSdk=24 nên nhánh else luôn < R nghĩa là cũng
        // luôn < S (31) — không cần nhánh VibratorManager (chỉ tồn tại từ S) ở đây.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Dialogs
    // ---------------------------------------------------------------------

    private fun showActivationSuccess(days: Int) {
        triggerSuccessEffect()
        binding.etVipKey.text?.clear()
        bindUi()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_success_title)
            .setMessage(resources.getQuantityString(R.plurals.vip_success_message, days, days))
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showActivationFailed() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_failed_title)
            .setMessage(R.string.vip_failed_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    /**
     * Token đang xếp hàng chờ SDK init xong — CHƯA có kết quả thật. Không có nút "Thử lại" tự bấm
     * gửi lại token — SDK đã tự động retry ngay khi init() xong; gửi lại thủ công có thể dính
     * "replay rejected" cho một token đã thành công ngầm. Kết quả thật tới qua listener ở onCreate.
     */
    private fun showTokenQueuedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_queued_title)
            .setMessage(R.string.vip_queued_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showTokenNoEffectDialog() {
        binding.etVipKey.text?.clear()
        bindUi()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_token_no_effect_title)
            .setMessage(R.string.vip_token_no_effect_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun infoDialog(messageRes: Int) {
        if (isFinishing || isDestroyed) return
        MaterialAlertDialogBuilder(this)
            .setMessage(messageRes)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showNoAdDialog() {
        if (isFinishing || isDestroyed) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_no_ad_title)
            .setMessage(R.string.vip_no_ad_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showNoRewardDialog() {
        if (isFinishing || isDestroyed) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_no_reward_title)
            .setMessage(R.string.vip_no_reward_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
