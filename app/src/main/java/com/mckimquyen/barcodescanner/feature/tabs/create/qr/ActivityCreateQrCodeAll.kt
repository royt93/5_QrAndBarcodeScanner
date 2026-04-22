package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.ACreateQrCodeAllBinding
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.create.ActivityCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema

class ActivityCreateQrCodeAll : ActivityBase() {
    private lateinit var binding: ACreateQrCodeAllBinding

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, ActivityCreateQrCodeAll::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ACreateQrCodeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        handleToolbarBackClicked()
        handleButtonsClicked()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleButtonsClicked() {
        binding.buttonText.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.OTHER
            )
        }
        binding.buttonUrl.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.URL
            )
        }
        binding.buttonWifi.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.WIFI
            )
        }
        binding.buttonLocation.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.GEO
            )
        }
        binding.buttonOtp.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.OTP_AUTH
            )
        }
        binding.buttonContactVcard.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.VCARD
            )
        }
        binding.buttonContactMecard.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.MECARD
            )
        }
        binding.buttonEvent.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.VEVENT
            )
        }
        binding.buttonPhone.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.PHONE
            )
        }
        binding.buttonEmail.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.EMAIL
            )
        }
        binding.buttonSms.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.SMS
            )
        }
        binding.buttonMms.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.MMS
            )
        }
        binding.buttonCryptoCurrency.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.CRYPTOCURRENCY
            )
        }
        binding.buttonBookmark.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.BOOKMARK
            )
        }
        binding.buttonApp.setOnClickListener {
            ActivityCreateBarcode.start(
                context = this,
                barcodeFormat = BarcodeFormat.QR_CODE,
                barcodeSchema = BarcodeSchema.APP
            )
        }
    }
}
