package com.mckimquyen.barcodescanner.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.model.Barcode

class RecentScansWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_recent_scans)

        // Bind data to ListView
        val intent = Intent(context, RecentScansWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.lvRecentScans, intent)
        views.setEmptyView(R.id.lvRecentScans, R.id.tvEmpty)

        // Template cho items click
        val clickIntent = Intent(context, RecentScansWidgetProvider::class.java).apply {
            action = "ACTION_OPEN_BARCODE"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.lvRecentScans, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_OPEN_BARCODE") {
            val barcode = intent.getSerializableExtra("EXTRA_BARCODE") as? Barcode
            barcode?.let {
                val actIntent = Intent(context, ActivityBarcode::class.java).apply {
                    putExtra("BARCODE_KEY", it)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(actIntent)
            }
        } else if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || intent.action == "com.mckimquyen.barcodescanner.APPWIDGET_UPDATE_RECENT") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, RecentScansWidgetProvider::class.java)
            )
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lvRecentScans)
        }
    }
}
