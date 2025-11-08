package com.mckimquyen.barcodescanner.feature.tabs.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.extension.orZero
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.model.Barcode
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.f_barcode_history_list.*

class FragmentBarcodeHistoryList : Fragment(), AdapterBarcodeHistory.Listener {

    companion object {
        private const val PAGE_SIZE = 20
        private const val TYPE_ALL = 0
        private const val TYPE_FAVORITES = 1
        private const val TYPE_KEY = "TYPE_KEY"

        fun newInstanceAll(): FragmentBarcodeHistoryList {
            return FragmentBarcodeHistoryList().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_ALL)
                }
            }
        }

        fun newInstanceFavorites(): FragmentBarcodeHistoryList {
            return FragmentBarcodeHistoryList().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_FAVORITES)
                }
            }
        }
    }

    private val disposable = CompositeDisposable()
    private val scanHistoryAdapter = AdapterBarcodeHistory(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.f_barcode_history_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadHistory()
    }

    override fun onBarcodeClicked(barcode: Barcode) {
        ActivityBarcode.start(requireActivity(), barcode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    private fun initRecyclerView() {
        recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun loadHistory() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PAGE_SIZE)
            .build()

        val dataSource = when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> barcodeDatabase.getAll()
            TYPE_FAVORITES -> barcodeDatabase.getFavorites()
            else -> return
        }

        RxPagedListBuilder(dataSource, config)
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
        layoutEmptyState?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewHistory?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
