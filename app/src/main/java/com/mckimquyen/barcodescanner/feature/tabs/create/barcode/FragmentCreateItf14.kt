package com.mckimquyen.barcodescanner.feature.tabs.create.barcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateItf14Binding
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Other
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateItf14 : FragmentBaseCreateBarcode() {
    private var _binding: FCreateItf14Binding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateItf14Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editText.requestFocus()
        binding.editText.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = (binding.editText.text?.length?.rem(2)) == 0
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Other(binding.editText.textString)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}