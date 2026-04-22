package com.mckimquyen.barcodescanner.feature.tabs.setting.formats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.ASupportedFormatsBinding
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.usecase.SupportedBarcodeFormats

class SupportedFormatsActivityBase : ActivityBase(), FormatsAdapter.Listener {
    private lateinit var binding: ASupportedFormatsBinding


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SupportedFormatsActivityBase::class.java)
            context.startActivity(intent)
        }
    }

    private val formats by unsafeLazy { SupportedBarcodeFormats.FORMATS }
    private val formatSelection by unsafeLazy { formats.map(settings::isFormatSelected) }
    private val formatsAdapter by unsafeLazy { FormatsAdapter(this, formats, formatSelection) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ASupportedFormatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initRecyclerView()
        handleToolbarBackClicked()
    }

    override fun onFormatChecked(format: BarcodeFormat, isChecked: Boolean) {
        settings.setFormatSelected(format, isChecked)
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initRecyclerView() {
        binding.recyclerViewFormats.apply {
            layoutManager = LinearLayoutManager(this@SupportedFormatsActivityBase)
            adapter = formatsAdapter
        }
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}