package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeEmailBinding
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Email
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodeEmail : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeEmailBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodeEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema(): Schema {
        return Email(
            email = binding.editTextEmail.textString,
            subject = binding.editTextSubject.textString,
            body = binding.editTextMessage.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextEmail.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextEmail.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
        binding.editTextSubject.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
        binding.editTextMessage.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            binding.editTextEmail.isNotBlank() || binding.editTextSubject.isNotBlank() || binding.editTextMessage.isNotBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}