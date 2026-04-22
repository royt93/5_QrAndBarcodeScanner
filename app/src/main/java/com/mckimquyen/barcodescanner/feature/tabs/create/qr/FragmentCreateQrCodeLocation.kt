package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeLocationBinding
import com.mckimquyen.barcodescanner.extension.isNotBlank
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.schema.Geo
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodeLocation : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeLocationBinding? = null
    private val binding get() = _binding!!


    override val latitude: Double?
        get() = binding.editTextLatitude.textString.toDoubleOrNull()

    override val longitude: Double?
        get() = binding.editTextLongitude.textString.toDoubleOrNull()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodeLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLatitudeEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema(): Schema {
        return Geo(
            latitude = binding.editTextLatitude.textString,
            longitude = binding.editTextLongitude.textString,
            altitude = binding.editTextAltitude.textString
        )
    }

    override fun showLocation(latitude: Double?, longitude: Double?) {
        latitude?.apply {
            binding.editTextLatitude.setText(latitude.toString())
        }
        longitude?.apply {
            binding.editTextLongitude.setText(longitude.toString())
        }
    }

    private fun initLatitudeEditText() {
        binding.editTextLatitude.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextLatitude.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
        binding.editTextLongitude.addTextChangedListener {
            toggleCreateBarcodeButton()
        }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            binding.editTextLatitude.isNotBlank() && binding.editTextLongitude.isNotBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}