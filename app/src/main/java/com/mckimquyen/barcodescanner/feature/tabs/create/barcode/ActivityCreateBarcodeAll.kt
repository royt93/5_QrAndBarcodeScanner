package com.mckimquyen.barcodescanner.feature.tabs.create.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.ACreateBarcodeAllBinding
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideRight
import com.mckimquyen.barcodescanner.feature.tabs.create.ActivityCreateBarcode

class ActivityCreateBarcodeAll : ActivityBase() {
    private lateinit var binding: ACreateBarcodeAllBinding

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, ActivityCreateBarcodeAll::class.java)
            context.startActivitySlideRight(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ACreateBarcodeAllBinding.inflate(layoutInflater)
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
        binding.buttonDataMatrix.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.DATA_MATRIX)
        }
        binding.buttonAztec.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.AZTEC)
        }
        binding.buttonPdf417.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.PDF_417)
        }
        binding.buttonCodabar.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.CODABAR)
        }
        binding.buttonCode39.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.CODE_39)
        }
        binding.buttonCode93.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.CODE_93)
        }
        binding.buttonCode128.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.CODE_128)
        }
        binding.buttonEan8.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.EAN_8)
        }
        binding.buttonEan13.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.EAN_13)
        }
        binding.buttonItf14.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.ITF)
        }
        binding.buttonUpcA.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.UPC_A)
        }
        binding.buttonUpcE.setOnClickListener {
            ActivityCreateBarcode.start(context = this, barcodeFormat = BarcodeFormat.UPC_E)
        }
    }
}
