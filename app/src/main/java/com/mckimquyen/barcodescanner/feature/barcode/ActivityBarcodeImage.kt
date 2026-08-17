package com.mckimquyen.barcodescanner.feature.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.ABarcodeImageBinding
import com.mckimquyen.barcodescanner.di.barcodeImageGenerator
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.toStringId
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideRight
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.usecase.Logger
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityBarcodeImage : ActivityBase() {
    private lateinit var binding: ABarcodeImageBinding


    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"

        fun start(context: Context, barcode: Barcode) {
            val intent = Intent(context, ActivityBarcodeImage::class.java)
            intent.putExtra(BARCODE_KEY, barcode)
            context.startActivitySlideRight(intent)
        }
    }

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private val barcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }
    private var originalBrightness: Float = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ABarcodeImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        saveOriginalBrightness()
        handleToolbarBackPressed()
        handleToolbarMenuItemClicked()
        showMenu()
        showBarcode()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun saveOriginalBrightness() {
        originalBrightness = window.attributes.screenBrightness
    }

    private fun handleToolbarBackPressed() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuItemClicked() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemIncreaseBrightness -> {
                    increaseBrightnessToMax()
                    binding.toolbar.menu.apply {
                        findItem(R.id.itemIncreaseBrightness).isVisible = false
                        findItem(R.id.itemDecreaseBrightness).isVisible = true
                    }
                }

                R.id.itemDecreaseBrightness -> {
                    restoreOriginalBrightness()
                    binding.toolbar.menu.apply {
                        findItem(R.id.itemDecreaseBrightness).isVisible = false
                        findItem(R.id.itemIncreaseBrightness).isVisible = true
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_barcode_image)
    }

    private fun showBarcode() {
        showBarcodeImage()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeText()
    }

    private fun showBarcodeImage() {
        try {
            val bitmap = barcodeImageGenerator.generateBitmap(
                barcode = barcode,
                width = 2000,
                height = 2000,
                margin = 0,
                codeColor = settings.barcodeContentColor,
                backgroundColor = settings.barcodeBackgroundColor
            )
            binding.imageViewBarcode.setImageBitmap(bitmap)
            binding.imageViewBarcode.setBackgroundColor(settings.barcodeBackgroundColor)
            binding.layoutBarcodeImageBackground.setBackgroundColor(settings.barcodeBackgroundColor)

            if (settings.isDarkTheme.not() || settings.areBarcodeColorsInversed) {
                binding.layoutBarcodeImageBackground.setPadding(
                    /* left = */ 0,
                    /* top = */ 0,
                    /* right = */ 0,
                    /* bottom = */ 0
                )
            }
        } catch (ex: Exception) {
            Logger.log(ex)
            binding.imageViewBarcode.isVisible = false
        }
    }

    private fun showBarcodeDate() {
        binding.textViewDate.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        binding.toolbar.setTitle(format)
    }

    private fun showBarcodeText() {
        binding.textViewBarcodeText.text = barcode.text
    }

    private fun increaseBrightnessToMax() {
        setBrightness(1.0f)
    }

    private fun restoreOriginalBrightness() {
        setBrightness(originalBrightness)
    }

    private fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = brightness
        }
    }
}
