package com.mckimquyen.barcodescanner.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.feature.tabs.ActivityBottomTabs
import com.mckimquyen.barcodescanner.feature.tabs.scan.file.ActivityScanBarcodeFromFile

class QuickScanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_scan)

        // 1. Scan from Camera (Mở ActivityBottomTabs với tab Scan mặc định)
        val intentScanCamera = Intent(context, ActivityBottomTabs::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentScanCamera = PendingIntent.getActivity(
            context,
            0,
            intentScanCamera,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llScanCamera, pendingIntentScanCamera)

        // 2. Scan from Image (Mở ActivityScanBarcodeFromFile)
        val intentScanImage = Intent(context, ActivityScanBarcodeFromFile::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentScanImage = PendingIntent.getActivity(
            context,
            1,
            intentScanImage,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llScanImage, pendingIntentScanImage)

        // 3. Create Barcode (Mở ActivityBottomTabs với action CREATE_BARCODE)
        val intentCreate = Intent(context, ActivityBottomTabs::class.java).apply {
            action = "${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentCreate = PendingIntent.getActivity(
            context,
            2,
            intentCreate,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCreateCode, pendingIntentCreate)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
