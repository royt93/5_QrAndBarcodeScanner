package com.mckimquyen.barcodescanner.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.feature.tabs.create.ActivityCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat

class CreateShortcutsWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
    }

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
        val views = RemoteViews(context.packageName, R.layout.widget_create_shortcuts)

        // 1. Text (OTHER)
        val intentText = Intent(context, ActivityCreateBarcode::class.java).apply {
            putExtra(BARCODE_FORMAT_KEY, BarcodeFormat.QR_CODE.ordinal)
            putExtra(BARCODE_SCHEMA_KEY, BarcodeSchema.OTHER.ordinal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentText = PendingIntent.getActivity(
            context,
            10,
            intentText,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCreateText, pendingIntentText)

        // 2. URL
        val intentUrl = Intent(context, ActivityCreateBarcode::class.java).apply {
            putExtra(BARCODE_FORMAT_KEY, BarcodeFormat.QR_CODE.ordinal)
            putExtra(BARCODE_SCHEMA_KEY, BarcodeSchema.URL.ordinal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentUrl = PendingIntent.getActivity(
            context,
            11,
            intentUrl,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCreateUrl, pendingIntentUrl)

        // 3. Wi-Fi
        val intentWifi = Intent(context, ActivityCreateBarcode::class.java).apply {
            putExtra(BARCODE_FORMAT_KEY, BarcodeFormat.QR_CODE.ordinal)
            putExtra(BARCODE_SCHEMA_KEY, BarcodeSchema.WIFI.ordinal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentWifi = PendingIntent.getActivity(
            context,
            12,
            intentWifi,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCreateWifi, pendingIntentWifi)

        // 4. Contact (VCard)
        val intentContact = Intent(context, ActivityCreateBarcode::class.java).apply {
            putExtra(BARCODE_FORMAT_KEY, BarcodeFormat.QR_CODE.ordinal)
            putExtra(BARCODE_SCHEMA_KEY, BarcodeSchema.VCARD.ordinal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentContact = PendingIntent.getActivity(
            context,
            13,
            intentContact,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCreateContact, pendingIntentContact)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
