package com.mckimquyen.barcodescanner.feature.tabs.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.FCreateBarcodeBinding
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.clipboardManager
import com.mckimquyen.barcodescanner.extension.orZero
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.ActivityCreateBarcodeAll
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.ActivityCreateQrCodeAll
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema
import com.roy.sdkadbmob.AdManager


class FragmentCreateBarcode : Fragment() {
    private var _binding: FCreateBarcodeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FCreateBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManager.loadInterstitial(requireActivity())
        supportEdgeToEdge()
        handleButtonsClicked()
    }

    private fun supportEdgeToEdge() {
        binding.appBarLayout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonsClicked() {
        // QR code
        binding.buttonClipboard.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTHER,
                getClipboardContent()
            )
        }
        binding.buttonText.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTHER
            )
        }
        binding.buttonUrl.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.URL
            )
        }
        binding.buttonWifi.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.WIFI
            )
        }
        binding.buttonLocation.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.GEO
            )
        }
        binding.buttonContactVcard.setOnClickListener {
            ActivityCreateBarcode.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.VCARD
            )
        }
        binding.buttonShowAllQrCode.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ActivityCreateQrCodeAll.start(requireActivity())
            }
        }

        // Barcode
        binding.buttonCreateBarcode.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ActivityCreateBarcodeAll.start(requireActivity())
            }
        }
    }

    private fun getClipboardContent(): String {
        val clip = requireActivity().clipboardManager?.primaryClip ?: return ""
        return when (clip.itemCount.orZero()) {
            0 -> ""
            else -> clip.getItemAt(0).text.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}