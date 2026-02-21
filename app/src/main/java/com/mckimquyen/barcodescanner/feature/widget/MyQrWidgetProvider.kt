package com.mckimquyen.barcodescanner.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.usecase.BarcodeImageGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class MyQrWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            MyQrWidgetConfigActivity.deleteWidgetBarcodeId(context, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val barcodeId = MyQrWidgetConfigActivity.getWidgetBarcodeId(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_my_qr)

            if (barcodeId == -1L) {
                // Không tìm thấy Barcode mapping
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Gọi database để lấy thông tin barcode
            val db = com.mckimquyen.barcodescanner.usecase.BarcodeDatabase.getInstance(context)
            val disposable = CompositeDisposable()
            
            db.getById(barcodeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ barcode: Barcode ->
                    // Set Name
                    views.setTextViewText(R.id.tvQrName, barcode.name ?: context.getString(R.string.app_name))
                    
                    // Generate Bitmap for Widget
                    BarcodeImageGenerator.generateBitmapAsync(
                        barcode = barcode,
                        width = 400,
                        height = 400,
                        margin = 1,
                        codeColor = Color.BLACK,
                        backgroundColor = Color.WHITE
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bitmap: android.graphics.Bitmap ->
                        views.setImageViewBitmap(R.id.ivQrCode, bitmap)
                        
                        // Set Click to open ActivityBarcode
                        val intent = Intent(context, ActivityBarcode::class.java).apply {
                            putExtra("BARCODE_KEY", barcode as java.io.Serializable)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.llWidgetRoot, pendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }, { error: Throwable -> 
                        error.printStackTrace()
                    }).addTo(disposable)

                }, { error: Throwable ->
                    error.printStackTrace()
                }).addTo(disposable)
        }
    }
}
