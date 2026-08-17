package com.mckimquyen.barcodescanner.feature.tabs.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideUp
import com.mckimquyen.barcodescanner.feature.tabs.ActivityBottomTabs
import java.io.File

class ActivityBatchExportResult : ActivityBase() {

    companion object {
        private const val TAG = "roy93~"
        private const val EXTRA_ITEM_COUNT = "EXTRA_ITEM_COUNT"
        private const val EXTRA_FILE_NAME = "EXTRA_FILE_NAME"

        fun start(context: Context, itemCount: Int, fileName: String) {
            Log.d(TAG, "ActivityBatchExportResult.start: itemCount=$itemCount, fileName=$fileName")
            val intent = Intent(context, ActivityBatchExportResult::class.java).apply {
                putExtra(EXTRA_ITEM_COUNT, itemCount)
                putExtra(EXTRA_FILE_NAME, fileName)
            }
            context.startActivitySlideUp(intent)
        }
    }

    override val exitTransition = 0 to R.anim.slide_out_down

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_batch_export_result)

        val itemCount = intent.getIntExtra(EXTRA_ITEM_COUNT, 0)
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        Log.d(TAG, "ActivityBatchExportResult.onCreate: itemCount=$itemCount, fileName=$fileName")

        // Toolbar with back button + localized title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.activity_batch_export_title)
        }
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "ActivityBatchExportResult: back button pressed")
            finish()
        }

        // Views
        val imageViewSuccess = findViewById<ImageView>(R.id.imageViewSuccess)
        val layoutContent = findViewById<LinearLayout>(R.id.layoutContent)
        val textViewDetail = findViewById<TextView>(R.id.textViewDetail)
        val textViewPath = findViewById<TextView>(R.id.textViewPath)
        val buttonOpenFile = findViewById<Button>(R.id.buttonOpenFile)
        val buttonContinueScanning = findViewById<Button>(R.id.buttonContinueScanning)
        val buttonBackHome = findViewById<Button>(R.id.buttonBackHome)

        val csvPath = "Downloads/${fileName}.csv"
        textViewDetail.text = getString(R.string.activity_batch_export_success_detail, itemCount)
        textViewPath.text = csvPath
        Log.d(TAG, "ActivityBatchExportResult: UI populated — $itemCount items, path=$csvPath")

        // Entrance animations
        val scalePop = AnimationUtils.loadAnimation(this, R.anim.anim_scale_pop)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_fade)
        imageViewSuccess.startAnimation(scalePop)
        layoutContent.startAnimation(slideUp)
        Log.d(TAG, "ActivityBatchExportResult: animations started")

        // Open the exported CSV file
        buttonOpenFile.setOnClickListener {
            Log.d(TAG, "ActivityBatchExportResult: Open File tapped for $fileName.csv")
            openCsvFile(fileName)
        }

        // Continue scanning → go back to camera tab
        buttonContinueScanning.setOnClickListener {
            Log.d(TAG, "ActivityBatchExportResult: Continue Scanning → finish")
            finish()
        }

        // View History → navigate to history tab in bottom navigation
        buttonBackHome.setOnClickListener {
            Log.d(TAG, "ActivityBatchExportResult: View History → navigating to history tab")
            navigateToHistoryTab()
        }
    }

    /** Opens the exported CSV from Downloads using FileProvider */
    private fun openCsvFile(fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "$fileName.csv")
            Log.d(TAG, "openCsvFile: path=${file.absolutePath}, exists=${file.exists()}")

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "Open CSV"))
        } catch (e: Exception) {
            Log.e(TAG, "openCsvFile: failed to open file — ${e.message}", e)
        }
    }

    /** Navigates to ActivityBottomTabs and switches to the History tab */
    private fun navigateToHistoryTab() {
        val action = "${BuildConfig.APPLICATION_ID}.HISTORY"
        Log.d(TAG, "navigateToHistoryTab: starting ActivityBottomTabs with action=$action")
        val intent = Intent(this, ActivityBottomTabs::class.java).apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        Log.d(TAG, "navigateToHistoryTab: intent created, flags=CLEAR_TOP|SINGLE_TOP")
        startActivity(intent)
        finish()
        Log.d(TAG, "navigateToHistoryTab: finish() called")
    }
}
