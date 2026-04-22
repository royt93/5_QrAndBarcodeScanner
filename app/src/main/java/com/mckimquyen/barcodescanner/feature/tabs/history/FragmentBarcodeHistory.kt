package com.mckimquyen.barcodescanner.feature.tabs.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.FBarcodeHistoryBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentDeleteConfirmation
import com.mckimquyen.barcodescanner.feature.tabs.history.export.ActivityExportHistory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers


class FragmentBarcodeHistory : Fragment(), DialogFragmentDeleteConfirmation.Listener {
    private var _binding: FBarcodeHistoryBinding? = null
    private val binding get() = _binding!!

    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FBarcodeHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
        initTabs()
        handleMenuClicked()
    }

    override fun onDeleteConfirmed() {
        clearHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }

    private fun supportEdgeToEdge() {
        binding.appBarLayout.applySystemWindowInsets(applyTop = true)
    }

    private fun initTabs() {
        binding.viewPager.adapter = AdapterBarcodeHistoryViewPager(requireContext(), childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun handleMenuClicked() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemExportHistory -> navigateToExportHistoryScreen()
                R.id.itemClearHistory -> showDeleteHistoryConfirmationDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun navigateToExportHistoryScreen() {
        ActivityExportHistory.start(requireActivity())
    }

    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DialogFragmentDeleteConfirmation.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }

    private fun clearHistory() {
        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { },
                ::showError
            )
            .addTo(disposable)
    }
}
