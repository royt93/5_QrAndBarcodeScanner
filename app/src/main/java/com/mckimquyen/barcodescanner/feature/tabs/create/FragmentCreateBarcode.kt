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
//        createAdInter()
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

    // Callbacks completely removed since AdmobWrapper handles this natively now.
//    private var interstitialAd: MaxInterstitialAd? = null
//
//    private fun createAdInter() {
//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            interstitialAd = MaxInterstitialAd(getString(R.string.INTER), context)
//            interstitialAd?.let { ad ->
//                ad.setListener(object : MaxAdListener {
//                    override fun onAdLoaded(p0: MaxAd) {
////                        logI("onAdLoaded")
////                        retryAttempt = 0
//                    }
//
//                    override fun onAdDisplayed(p0: MaxAd) {
////                        logI("onAdDisplayed")
//                    }
//
//                    override fun onAdHidden(p0: MaxAd) {
////                        logI("onAdHidden")
//                        // Interstitial Ad is hidden. Pre-load the next ad
//                        interstitialAd?.loadAd()
//                    }
//
//                    override fun onAdClicked(p0: MaxAd) {
////                        logI("onAdClicked")
//                    }
//
//                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
////                        logI("onAdLoadFailed")
////                        retryAttempt++
////                        val delayMillis =
////                            TimeUnit.SECONDS.toMillis(2.0.pow(min(6, retryAttempt)).toLong())
////
////                        Handler(Looper.getMainLooper()).postDelayed(
////                            {
////                                interstitialAd?.loadAd()
////                            }, delayMillis
////                        )
//                    }
//
//                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
////                        logI("onAdDisplayFailed")
//                        // Interstitial ad failed to display. We recommend loading the next ad.
//                        interstitialAd?.loadAd()
//                    }
//
//                })
//                ad.setRevenueListener {
////                    logI("onAdDisplayed")
//                }
//
//                // Load the first ad.
//                ad.loadAd()
//            }
//        }
//    }
//
//    private fun showAd(runnable: Runnable? = null) {
//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            if (interstitialAd == null) {
//                runnable?.run()
//            } else {
//                interstitialAd?.let { ad ->
//                    if (ad.isReady) {
////                        showDialogProgress()
////                        setDelay(500.getRandomNumber() + 500) {
////                            hideDialogProgress()
////                            ad.showAd()
////                            runnable?.run()
////                        }
//                        ad.showAd()
//                        runnable?.run()
//                    } else {
//                        runnable?.run()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(context, "Applovin show ad Inter in debug mode", Toast.LENGTH_SHORT).show()
//            runnable?.run()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}