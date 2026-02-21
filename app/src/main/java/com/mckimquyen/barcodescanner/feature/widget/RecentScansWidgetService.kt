package com.mckimquyen.barcodescanner.feature.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.extension.toColorId
import com.mckimquyen.barcodescanner.extension.toImageId
import com.mckimquyen.barcodescanner.extension.toStringId
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.usecase.BarcodeDatabase
import java.text.SimpleDateFormat
import java.util.*

class RecentScansWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RecentScansRemoteViewsFactory(this.applicationContext)
    }
}

class RecentScansRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val barcodes = mutableListOf<Barcode>()
    private val database = BarcodeDatabase.getInstance(context)
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    override fun onCreate() {
        // Khởi tạo
    }

    override fun onDataSetChanged() {
        // Hàm này chạy trên background thread, do đó gọi query database an toàn
        barcodes.clear()
        barcodes.addAll(database.getRecentScans(10))
    }

    override fun onDestroy() {
        barcodes.clear()
    }

    override fun getCount(): Int = barcodes.size

    override fun getViewAt(position: Int): RemoteViews {
        val barcode = barcodes[position]
        val views = RemoteViews(context.packageName, R.layout.item_widget_recent_scan)

        // Text
        val text = barcode.name ?: barcode.formattedText
        views.setTextViewText(R.id.tvText, text)
        views.setTextViewText(R.id.tvFormat, context.getString(barcode.format.toStringId()))
        views.setTextViewText(R.id.tvDate, dateFormatter.format(barcode.date))

        // Icon
        val imageId = barcode.schema.toImageId() ?: barcode.format.toImageId()
        views.setImageViewResource(R.id.ivIcon, imageId)

        // FillIntent để xử lý click mở app
        val fillInIntent = Intent().apply {
            putExtra("EXTRA_BARCODE", barcode)
        }
        views.setOnClickFillInIntent(R.id.llRoot, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = barcodes[position].id

    override fun hasStableIds(): Boolean = true
}
