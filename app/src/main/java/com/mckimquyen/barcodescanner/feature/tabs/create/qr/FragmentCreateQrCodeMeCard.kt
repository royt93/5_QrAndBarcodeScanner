package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mckimquyen.barcodescanner.databinding.FCreateQrCodeMecardBinding
import com.mckimquyen.barcodescanner.extension.textString
import com.mckimquyen.barcodescanner.feature.tabs.create.FragmentBaseCreateBarcode
import com.mckimquyen.barcodescanner.model.Contact
import com.mckimquyen.barcodescanner.model.schema.MeCard
import com.mckimquyen.barcodescanner.model.schema.Schema

class FragmentCreateQrCodeMeCard : FragmentBaseCreateBarcode() {
    private var _binding: FCreateQrCodeMecardBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateQrCodeMecardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTextFirstName.requestFocus()
        parentActivity.isCreateBarcodeButtonEnabled = true
    }

    override fun getBarcodeSchema(): Schema {
        return MeCard(
            firstName = binding.editTextFirstName.textString,
            lastName = binding.editTextLastName.textString,
            email = binding.editTextEmail.textString,
            phone = binding.editTextPhone.textString
        )
    }

    override fun showContact(contact: Contact) {
        binding.editTextFirstName.setText(contact.firstName)
        binding.editTextLastName.setText(contact.lastName)
        binding.editTextEmail.setText(contact.email)
        binding.editTextPhone.setText(contact.phone)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}