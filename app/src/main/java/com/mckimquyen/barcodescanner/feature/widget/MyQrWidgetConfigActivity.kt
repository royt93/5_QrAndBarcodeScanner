package com.mckimquyen.barcodescanner.feature.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.feature.tabs.history.AdapterBarcodeHistory
import com.mckimquyen.barcodescanner.model.Barcode
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.a_my_qr_widget_config.*

class MyQrWidgetConfigActivity : AppCompatActivity(), AdapterBarcodeHistory.Listener {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val disposable = CompositeDisposable()
    private val scanHistoryAdapter: AdapterBarcodeHistory by lazy { AdapterBarcodeHistory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Nhận Widget ID từ Intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // Nếu bị hủy
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.a_my_qr_widget_config)

        toolbar.setNavigationOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        initRecyclerView()
        loadHistory()
    }

    private fun initRecyclerView() {
        recyclerViewCodes.apply {
            layoutManager = LinearLayoutManager(this@MyQrWidgetConfigActivity)
            adapter = scanHistoryAdapter
        }
    }

    private fun loadHistory() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(20)
            .build()

        RxPagedListBuilder(barcodeDatabase.getAll(), config)
            .buildFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    scanHistoryAdapter.submitList(list)
                    updateEmptyState(list.isEmpty())
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewCodes.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onBarcodeClicked(barcode: Barcode) {
        saveWidgetBarcodeId(this, appWidgetId, barcode.id)

        // Cập nhật Widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        MyQrWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

        // Trả kết quả thành công và tắt
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    companion object {
        private const val PREFS_NAME = "com.mckimquyen.barcodescanner.MyQrWidget"
        private const val PREF_PREFIX_KEY = "my_qr_"

        fun saveWidgetBarcodeId(context: Context, appWidgetId: Int, barcodeId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(PREF_PREFIX_KEY + appWidgetId, barcodeId).apply()
        }

        fun getWidgetBarcodeId(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
        }

        fun deleteWidgetBarcodeId(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
        }
    }
}
