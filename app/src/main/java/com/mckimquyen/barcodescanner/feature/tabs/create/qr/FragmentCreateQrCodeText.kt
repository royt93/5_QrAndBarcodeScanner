package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeTextBinding
import com.mckimquyen.barcodescanner.di.barcodeParser
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodeText : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeTextBinding? = null
    private val binding get() = _binding!!


    companion object {
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"

        fun newInstance(defaultText: String): FragmentCreateQrCodeText {
            return FragmentCreateQrCodeText().apply {
                arguments = Bundle().apply {
                    putString(DEFAULT_TEXT_KEY, defaultText)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodeTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTextChanged()
        initEditText()
    }

    override fun getBarcodeSchema(): Schema {
        return barcodeParser.parseSchema(BarcodeFormat.QR_CODE, binding.editText.textString)
    }

    private fun initEditText() {
        val defaultText = arguments?.getString(DEFAULT_TEXT_KEY).orEmpty()
        binding.editText.apply {
            setText(defaultText)
            setSelection(defaultText.length)
            requestFocus()
        }
    }

    private fun handleTextChanged() {
        binding.editText.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = binding.editText.isNotBlank()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}