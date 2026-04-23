package com.mckimquyen.barcodescanner.feature.tabs.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.mckimquyen.barcodescanner.databinding.FBarcodeHistoryListBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.extension.orZero
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.model.Barcode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FragmentBarcodeHistoryList : Fragment(), AdapterBarcodeHistory.Listener {
    private var _binding: FBarcodeHistoryListBinding? = null
    private val binding get() = _binding!!


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

    private val scanHistoryAdapter = AdapterBarcodeHistory(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FBarcodeHistoryListBinding.inflate(inflater, container, false)
        return binding.root
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
        _binding = null
    }

    private fun initRecyclerView() {
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun loadHistory() {
        val config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false
        )

        val pager = when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> Pager(config) { barcodeDatabase.getAll() }
            TYPE_FAVORITES -> Pager(config) { barcodeDatabase.getFavorites() }
            else -> return
        }

        lifecycleScope.launch {
            pager.flow
                .cachedIn(lifecycleScope)
                .collectLatest { pagingData ->
                    scanHistoryAdapter.submitData(pagingData)
                    updateEmptyState(scanHistoryAdapter.itemCount == 0)
                }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyState?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewHistory?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
