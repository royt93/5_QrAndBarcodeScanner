package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeSmsBinding
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Schema
import com.mckimquyen.barcodescanner.model.schema.Sms

class FragmentCreateQrCodeSms : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeSmsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FCreateQrCodeSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }

    override fun showPhone(phone: String) {
        binding.editTextPhone.apply {
            setText(phone)
            setSelection(phone.length)
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Sms(
            phone = binding.editTextPhone.textString,
            message = binding.editTextMessage.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextPhone.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextPhone.addTextChangedListener { toggleCreateBarcodeButton() }
        binding.editTextMessage.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            binding.editTextPhone.isNotBlank() || binding.editTextMessage.isNotBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}