package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodePhoneBinding
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Phone
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodePhone : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodePhoneBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodePhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditText()
        handleTextChanged()
    }

    override fun showPhone(phone: String) {
        binding.editText.apply {
            setText(phone)
            setSelection(phone.length)
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Phone(binding.editText.textString)
    }

    private fun initEditText() {
        binding.editText.requestFocus()
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