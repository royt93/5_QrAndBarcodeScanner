package com.mckimquyen.barcodescanner.feature.barcode.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.isVisible
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.ABarcodeOtpBinding
import com.mckimquyen.barcodescanner.di.otpGenerator
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.orZero
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideRight
import com.mckimquyen.barcodescanner.model.schema.OtpAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit

class ActivityOtp : ActivityBase() {
    private lateinit var binding: ABarcodeOtpBinding


    companion object {
        private const val OTP_KEY = "OTP_KEY"

        fun start(context: Context, opt: OtpAuth) {
            val intent = Intent(context, ActivityOtp::class.java).apply {
                putExtra(OTP_KEY, opt)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()
    private lateinit var otp: OtpAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ABarcodeOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableSecurity()
        supportEdgeToEdge()
        parseOtp()
        handleToolbarBackClicked()
        handleRefreshOtpClicked()
        showOtp()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun enableSecurity() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun parseOtp() {
        otp = intent?.getSerializableExtra(OTP_KEY) as OtpAuth
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleRefreshOtpClicked() {
        binding.buttonRefresh.setOnClickListener {
            refreshOtp()
        }
    }

    private fun refreshOtp() {
        otp = otp.copy(counter = otp.counter.orZero() + 1L)
        showOtp()
    }

    private fun showOtp() {
        when (otp.type) {
            OtpAuth.HOTP_TYPE -> showHotp()
            OtpAuth.TOTP_TYPE -> showTotp()
        }
        binding.textViewPassword.text =
            otpGenerator.generateOTP(otp) ?: getString(R.string.activity_barcode_otp_unable_to_generate_otp)
    }

    private fun showHotp() {
        binding.buttonRefresh.isVisible = true
        binding.textViewCounter.isVisible = true
        binding.textViewCounter.text = getString(R.string.activity_barcode_otp_counter, otp.counter.orZero().toString())
    }

    private fun showTotp() {
        binding.textViewTimer.isVisible = true
        startTimer()
    }

    private fun startTimer() {
        val period = otp.period ?: 30
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val secondsPassed = currentTimeInSeconds % period
        val secondsLeft = period - secondsPassed

        Observable
            .interval(1, TimeUnit.SECONDS)
            .map { it + 1 }
            .take(secondsLeft)
            .map { secondsLeft - it }
            .startWithItem(secondsLeft)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { showOtp() }
            .subscribe(::showTime)
            .addTo(disposable)
    }

    private fun showTime(secondsLeft: Long) {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        binding.textViewTimer.text = getString(R.string.activity_barcode_otp_timer, minutes.toTime(), seconds.toTime())
    }

    private fun Long.toTime(): String {
        return if (this >= 10) {
            this.toString()
        } else {
            "0$this"
        }
    }
}
