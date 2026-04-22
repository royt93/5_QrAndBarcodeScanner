package com.mckimquyen.barcodescanner.feature.barcode.save

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.ASaveBarcodeAsTextBinding
import com.mckimquyen.barcodescanner.di.barcodeSaver
import com.mckimquyen.barcodescanner.di.permissionsHelper
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.model.Barcode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ActivitySaveBarcodeAsText : ActivityBase() {
    private lateinit var binding: ASaveBarcodeAsTextBinding


    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        private const val BARCODE_KEY = "BARCODE_KEY"

        fun start(context: Context, barcode: Barcode) {
            val intent = Intent(context, ActivitySaveBarcodeAsText::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
            }
            context.startActivity(intent)
        }
    }

    private val barcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ASaveBarcodeAsTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initToolbar()
        initFormatSpinner()
        initSaveButton()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            saveBarcode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initFormatSpinner() {
        binding.spinnerSaveAs.adapter = ArrayAdapter.createFromResource(
            this, R.array.activity_save_barcode_as_text_formats, R.layout.i_spinner
        ).apply {
            setDropDownViewResource(R.layout.i_spinner_dropdown)
        }
    }

    private fun initSaveButton() {
        binding.buttonSave.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        permissionsHelper.requestPermissions(
            activity = this,
            permissions = PERMISSIONS,
            requestCode = REQUEST_PERMISSIONS_CODE
        )
    }

    private fun saveBarcode() {
        val saveFunc = when (binding.spinnerSaveAs.selectedItemPosition) {
            0 -> barcodeSaver::saveBarcodeAsCsv
            1 -> barcodeSaver::saveBarcodeAsJson
            else -> return
        }

        showLoading(true)

        saveFunc(this, barcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showBarcodeSaved() },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.scrollView.isVisible = isLoading.not()
    }

    private fun showBarcodeSaved() {
        Toast.makeText(this, R.string.activity_save_barcode_as_text_file_name_saved, Toast.LENGTH_LONG).show()
        finish()
    }
}
