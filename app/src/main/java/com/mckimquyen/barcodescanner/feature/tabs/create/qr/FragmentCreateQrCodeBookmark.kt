package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeBookmarkBinding
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Bookmark
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodeBookmark : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeBookmarkBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodeBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema(): Schema {
        return Bookmark(
            title = binding.editTextTitle.textString,
            url = binding.editTextUrl.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextTitle.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextTitle.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
        binding.editTextUrl.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            binding.editTextTitle.isNotBlank() || binding.editTextUrl.isNotBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}