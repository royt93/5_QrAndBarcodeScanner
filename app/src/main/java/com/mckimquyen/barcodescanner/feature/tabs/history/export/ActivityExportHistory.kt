package com.mckimquyen.barcodescanner.feature.tabs.history.export

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.AExportHistoryBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.di.barcodeSaver
import com.mckimquyen.barcodescanner.di.permissionsHelper
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.ActivityBase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers

class ActivityExportHistory : ActivityBase() {
    private lateinit var binding: AExportHistoryBinding

    private val disposable = CompositeDisposable()

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun start(context: Context) {
            val intent = Intent(context, ActivityExportHistory::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AExportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initToolbar()
        initExportTypeSpinner()
        initFileNameEditText()
        initExportButton()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            exportHistory()
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

    private fun initExportTypeSpinner() {
        binding.spinnerExportAs.adapter = ArrayAdapter.createFromResource(
            this, R.array.activity_export_history_types, R.layout.i_spinner
        ).apply {
            setDropDownViewResource(R.layout.i_spinner_dropdown)
        }
    }

    private fun initFileNameEditText() {
        binding.editTextFileName.addTextChangedListener {
            binding.buttonExport.isEnabled = binding.editTextFileName.isNotBlank()
        }
    }

    private fun initExportButton() {
        binding.buttonExport.setOnClickListener {
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

    private fun exportHistory() {
        val fileName = binding.editTextFileName.textString
        val saveFunc = when (binding.spinnerExportAs.selectedItemPosition) {
            0 -> barcodeSaver::saveBarcodeHistoryAsCsv
            1 -> barcodeSaver::saveBarcodeHistoryAsJson
            else -> return
        }

        showLoading(true)

        barcodeDatabase
            .getAllForExport()
            .flatMapCompletable { barcodes ->
                saveFunc(this, fileName, barcodes)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showHistoryExported()
                },
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

    private fun showHistoryExported() {
        Toast.makeText(this, R.string.activity_export_history_exported, Toast.LENGTH_LONG).show()
        finish()
    }
}
