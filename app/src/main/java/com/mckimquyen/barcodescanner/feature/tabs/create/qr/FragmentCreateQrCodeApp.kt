package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeAppBinding
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.App
import com.mckimquyen.barcodescanner.model.schema.Schema
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers

class FragmentCreateQrCodeApp : FragmentBaseCreateBarcode() {
    private val disposable = CompositeDisposable()
    private val adapterApp by unsafeLazy { AdapterApp(parentActivity) }

    private var _binding: FCreateQrCodeAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FCreateQrCodeAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadApps()
    }

    override fun getBarcodeSchema(): Schema {
        return App.fromPackage("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }

    private fun initRecyclerView() {
        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterApp
        }
    }

    private fun loadApps() {
        showLoading(true)

        Single.just(getApps())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { apps ->
                    showLoading(false)
                    showApps(apps)
                },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun getApps(): List<ResolveInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return requireContext().packageManager
            .queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo?.packageName != null }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.recyclerViewApps.isVisible = isLoading.not()
    }

    private fun showApps(apps: List<ResolveInfo>) {
        adapterApp.apps = apps
    }
}
