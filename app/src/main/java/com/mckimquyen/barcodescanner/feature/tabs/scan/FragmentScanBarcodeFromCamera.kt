package com.mckimquyen.barcodescanner.feature.tabs.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.FScanBarcodeFromCameraBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.di.barcodeParser
import com.mckimquyen.barcodescanner.di.barcodeSaver
import com.mckimquyen.barcodescanner.di.permissionsHelper
import com.mckimquyen.barcodescanner.di.scannerCameraHelper
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.equalTo
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.extension.vibrateOnce
import com.mckimquyen.barcodescanner.extension.vibrator
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentConfirmBarcode
import com.mckimquyen.barcodescanner.feature.tabs.scan.file.ActivityScanBarcodeFromFile
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.usecase.SupportedBarcodeFormats
import com.mckimquyen.barcodescanner.usecase.save
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FragmentScanBarcodeFromCamera : Fragment(), DialogFragmentConfirmBarcode.Listener {
    private var _binding: FScanBarcodeFromCameraBinding? = null
    private val binding get() = _binding!!


    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
        private const val TAG = "roy93~"
    }

    private val vibrationPattern = arrayOf<Long>(0, 350).toLongArray()
    private val disposable = CompositeDisposable()
    private var maxZoom: Int = 0
    private val zoomStep = 5
    private var codeScanner: CodeScanner? = null
    private var toast: Toast? = null
    private var lastResult: Barcode? = null
    
    private var isBatchScanMode = false
    private val batchList = mutableListOf<Barcode>()
    private val batchAdapter = BatchAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FScanBarcodeFromCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: initializing scan fragment")
        supportEdgeToEdge()
        setDarkStatusBar()
        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        initBatchScanButton()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            initZoomSeekBar()
            codeScanner?.startPreview()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            initZoomSeekBar()
            codeScanner?.startPreview()
        }
    }

    override fun onBarcodeConfirmed(barcode: Barcode) {
        handleConfirmedBarcode(barcode)
    }

    override fun onBarcodeDeclined() {
        restartPreview()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setLightStatusBar()
        disposable.clear()
        codeScanner?.releaseResources()
        codeScanner = null
        _binding = null
    }

    private fun supportEdgeToEdge() {
        // Apply status bar inset to the entire top controls row
        binding.layoutTopControls.applySystemWindowInsets(applyTop = true)
    }

    private fun setDarkStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireActivity(), binding.scannerView).apply {
            camera = if (settings.isBackCamera) {
                CodeScanner.CAMERA_BACK
            } else {
                CodeScanner.CAMERA_FRONT
            }
            autoFocusMode = if (settings.simpleAutoFocus) {
                AutoFocusMode.SAFE
            } else {
                AutoFocusMode.CONTINUOUS
            }
            formats = SupportedBarcodeFormats.FORMATS.filter(settings::isFormatSelected)
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = settings.flash
            isTouchFocusEnabled = false
            decodeCallback = DecodeCallback(::handleScannedBarcode)
            errorCallback = ErrorCallback(::showError)
        }
    }

    private fun initZoomSeekBar() {
        scannerCameraHelper.getCameraParameters(settings.isBackCamera)?.apply {
            this@FragmentScanBarcodeFromCamera.maxZoom = maxZoom
            binding.seekBarZoom.max = maxZoom
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun initFlashButton() {
        binding.layoutFlashContainer.setOnClickListener {
            toggleFlash()
        }
        binding.imageViewFlash.isActivated = settings.flash
    }

    private fun updateBatchScanVisual() {
        if (isBatchScanMode) {
            // ON: blue pill background + white tint on icon
            binding.layoutBatchScanContainer.setBackgroundResource(R.drawable.bg_batch_scan_active)
            binding.imageViewBatchScan.setColorFilter(
                android.graphics.Color.parseColor("#00B1FF"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.textViewBatchScan.setTextColor(android.graphics.Color.parseColor("#00B1FF"))
        } else {
            // OFF: transparent + white (default)
            binding.layoutBatchScanContainer.setBackgroundResource(android.R.color.transparent)
            binding.imageViewBatchScan.clearColorFilter()
            binding.textViewBatchScan.setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun initBatchScanButton() {
        binding.layoutBatchScanContainer.setOnClickListener {
            isBatchScanMode = !isBatchScanMode
            updateBatchScanVisual()
            Log.d(TAG, "Batch Scan toggle: isBatchScanMode=$isBatchScanMode")

            if (isBatchScanMode) {
                if (binding.recyclerViewBatch.adapter == null) {
                    binding.recyclerViewBatch.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(requireContext())
                    binding.recyclerViewBatch.adapter = batchAdapter
                    // Fix 2: disable RecyclerView touch interception so card-level click works
                    binding.recyclerViewBatch.isClickable = false
                    binding.recyclerViewBatch.isFocusable = false
                    binding.recyclerViewBatch.isNestedScrollingEnabled = false
                }
                // Show helper dialog; panel will appear only after first successful scan
                val dialog = DialogFragmentScanHelper()
                dialog.onDismissCallback = {
                    Log.d(TAG, "BatchHelper dialog dismissed — waiting for first scan to reveal panel")
                }
                dialog.show(childFragmentManager, "BatchHelper")
            } else {
                Log.d(TAG, "Batch Scan OFF → hiding panel, clearing list")
                binding.layoutBatchListPanel.isVisible = false
            }
        }

        // Export on button click
        binding.buttonExportCsv.setOnClickListener { triggerExport() }
        // Card click exports (works because RecyclerView is non-clickable)
        binding.layoutBatchListPanel.setOnClickListener { triggerExport() }
    }

    private fun triggerExport() {
        if (batchList.isEmpty()) return
        val snapshot = batchList.toList() // take a snapshot before clearing
        val fileName = "Batch_Scan_${System.currentTimeMillis()}"
        val exportList = snapshot.map { com.mckimquyen.barcodescanner.model.ExportBarcode(it.date, it.format, it.text) }
        Log.d(TAG, "triggerExport: saving ${snapshot.size} items to database + CSV")

        // Step 1: save all batch items to the barcode database so they appear in History
        io.reactivex.rxjava3.core.Observable.fromIterable(snapshot)
            .flatMapSingle { barcode ->
                barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                    .doOnSuccess { id -> Log.d(TAG, "triggerExport: saved '${barcode.text}' to DB id=$id") }
                    .onErrorReturnItem(-1L)
            }
            .toList()
            .flatMap {
                // Step 2: export to CSV after all DB saves are done
                Log.d(TAG, "triggerExport: DB saves complete, exporting CSV")
                (requireActivity() as AppCompatActivity).barcodeSaver
                    .saveBarcodeHistoryAsCsv(requireContext(), fileName, exportList)
                    .andThen(io.reactivex.rxjava3.core.Single.just(snapshot.size))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ count ->
                batchList.clear()
                batchAdapter.notifyDataSetChanged()
                binding.layoutBatchListPanel.isVisible = false
                Log.d(TAG, "triggerExport success: DB + CSV done, panel hidden. count=$count")
                com.mckimquyen.barcodescanner.feature.tabs.scan.ActivityBatchExportResult.start(requireContext(), count, fileName)
            }, { error: Throwable ->
                Log.e(TAG, "triggerExport error: ${error.message}", error)
                showError(error)
            })
            .addTo(disposable)
    }

    private fun handleScanFromFileClicked() {
        binding.layoutScanFromFileContainer.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun handleZoomChanged() {
        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean,
            ) {
                if (fromUser) {
                    codeScanner?.zoom = progress
                }
            }
        })
    }

    private fun handleDecreaseZoomClicked() {
        binding.buttonDecreaseZoom.setOnClickListener {
            decreaseZoom()
        }
    }

    private fun handleIncreaseZoomClicked() {
        binding.buttonIncreaseZoom.setOnClickListener {
            increaseZoom()
        }
    }

    private fun decreaseZoom() {
        codeScanner?.apply {
            if (zoom > zoomStep) {
                zoom -= zoomStep
            } else {
                zoom = 0
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun increaseZoom() {
        codeScanner?.apply {
            if (zoom < maxZoom - zoomStep) {
                zoom += zoomStep
            } else {
                zoom = maxZoom
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun handleScannedBarcode(result: Result) {
        if (requireActivity().intent?.action == ZXING_SCAN_INTENT_ACTION) {
            vibrateIfNeeded()
            finishWithResult(result)
            return
        }

        if (settings.continuousScanning && result.equalTo(lastResult)) {
            restartPreviewWithDelay(false)
            return
        }

        vibrateIfNeeded()

        val barcode = barcodeParser.parseResult(result)

        when {
            settings.confirmScansManually -> showScanConfirmationDialog(barcode)
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun handleConfirmedBarcode(barcode: Barcode) {
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            activity?.apply {
                runOnUiThread {
                    applicationContext.vibrator?.vibrateOnce(vibrationPattern)
                }
            }
        }
    }

    private fun showScanConfirmationDialog(barcode: Barcode) {
        val dialog = DialogFragmentConfirmBarcode.newInstance(barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun saveScannedBarcode(barcode: Barcode) {
        if (isBatchScanMode) {
            activity?.runOnUiThread {
                val isDuplicate = settings.doNotSaveDuplicates && batchList.any { it.text == barcode.text && it.format == barcode.format }
                if (!isDuplicate) {
                    batchList.add(0, barcode)
                    batchAdapter.notifyItemInserted(0)
                    binding.recyclerViewBatch.scrollToPosition(0)
                    Log.d(TAG, "saveScannedBarcode [BATCH]: added '${barcode.text}', total=${batchList.size}")
                    // Show panel on very first successful scan
                    if (!binding.layoutBatchListPanel.isVisible) {
                        binding.layoutBatchListPanel.isVisible = true
                        Log.d(TAG, "saveScannedBarcode [BATCH]: revealing panel on first scan")
                    }
                } else {
                    Log.d(TAG, "saveScannedBarcode [BATCH]: duplicate skipped '${barcode.text}'")
                }
            }
            restartPreviewWithDelay(false)
            return
        }

        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    lastResult = barcode
                    when (settings.continuousScanning) {
                        true -> restartPreviewWithDelay(true)
                        else -> navigateToBarcodeScreen(barcode.copy(id = id))
                    }
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun restartPreviewWithDelay(showMessage: Boolean) {
        Completable
            .timer(CONTINUOUS_SCANNING_PREVIEW_DELAY, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (showMessage) {
                    showToast(R.string.fragment_scan_barcode_from_camera_barcode_saved)
                }
                restartPreview()
            }
            .addTo(disposable)
    }

    private fun restartPreview() {
        activity?.runOnUiThread {
            codeScanner?.startPreview()
        }
    }

    private fun toggleFlash() {
        binding.imageViewFlash.isActivated = binding.imageViewFlash.isActivated.not()
        codeScanner?.isFlashEnabled = codeScanner?.isFlashEnabled?.not() ?: false
    }

    private fun showToast(stringId: Int) {
        toast?.cancel()
        toast = Toast.makeText(requireActivity(), stringId, Toast.LENGTH_SHORT).apply {
            show()
        }
    }

    private fun requestPermissions() {
        permissionsHelper.requestNotGrantedPermissions(
            requireActivity() as AppCompatActivity,
            PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    private fun areAllPermissionsGranted(): Boolean {
        return permissionsHelper.areAllPermissionsGranted(requireActivity(), PERMISSIONS)
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return permissionsHelper.areAllPermissionsGranted(grantResults)
    }

    private fun navigateToScanFromFileScreen() {
        ActivityScanBarcodeFromFile.start(requireActivity())
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        ActivityBarcode.start(requireActivity(), barcode)
    }

    private fun finishWithResult(result: Result) {
        val intent = Intent()
            .putExtra("SCAN_RESULT", result.text)
            .putExtra("SCAN_RESULT_FORMAT", result.barcodeFormat.toString())

        if (result.rawBytes?.isNotEmpty() == true) {
            intent.putExtra("SCAN_RESULT_BYTES", result.rawBytes)
        }

        result.resultMetadata?.let { metadata ->
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_ORIENTATION", it.toString())
            }

            metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL]?.let {
                intent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL", it.toString())
            }

            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_UPC_EAN_EXTENSION", it.toString())
            }

            metadata[ResultMetadataType.BYTE_SEGMENTS]?.let {
                var i = 0
                @Suppress("UNCHECKED_CAST")
                for (seg in it as Iterable<ByteArray>) {
                    intent.putExtra("SCAN_RESULT_BYTE_SEGMENTS_$i", seg)
                    ++i
                }
            }
        }

        requireActivity().apply {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    inner class BatchAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<BatchAdapter.BatchViewHolder>() {
        inner class BatchViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val text: android.widget.TextView = view.findViewById(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatchViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return BatchViewHolder(view)
        }
        override fun onBindViewHolder(holder: BatchViewHolder, position: Int) {
            holder.text.text = batchList[position].text
            holder.text.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.default_text_color))
        }
        override fun getItemCount() = batchList.size
    }
}
