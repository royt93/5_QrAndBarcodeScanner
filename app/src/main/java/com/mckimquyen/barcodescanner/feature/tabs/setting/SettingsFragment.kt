package com.mckimquyen.barcodescanner.feature.tabs.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.FSettingsBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.ext.moreApp
import com.mckimquyen.barcodescanner.extension.ext.openBrowserPolicy
import com.mckimquyen.barcodescanner.extension.ext.openUrlInBrowser
import com.mckimquyen.barcodescanner.extension.ext.rateApp
import com.mckimquyen.barcodescanner.extension.ext.shareApp
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentDeleteConfirmation
import com.mckimquyen.barcodescanner.feature.tabs.setting.camera.ChooseCameraActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.formats.SupportedFormatsActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.language.ChooseLanguageActivity
import com.mckimquyen.barcodescanner.feature.tabs.setting.permissions.AllPermissionsActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.search.ChooseSearchEngineActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.theme.ChooseThemeActivityBase
import com.roy.sdkadbmob.AdManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class SettingsFragment : Fragment(), DialogFragmentDeleteConfirmation.Listener {
    private var _binding: FSettingsBinding? = null
    private val binding get() = _binding!!

    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManager.loadInterstitial(requireActivity())
        supportEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        handleButtonCheckedChanged()
        handleButtonClicks()
        showSettings()
        showAppVersion()
    }

    override fun onDeleteConfirmed() {
        clearHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }

    fun supportEdgeToEdge() {
        binding.appBarLayout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonCheckedChanged() {
        binding.buttonInverseBarcodeColorsInDarkTheme.setCheckedChangedListener {
            settings.areBarcodeColorsInversed = it
        }
        binding.buttonOpenLinksAutomatically.setCheckedChangedListener { settings.openLinksAutomatically = it }
        binding.buttonCopyToClipboard.setCheckedChangedListener { settings.copyToClipboard = it }
        binding.buttonSimpleAutoFocus.setCheckedChangedListener { settings.simpleAutoFocus = it }
        binding.buttonFlashlight.setCheckedChangedListener { settings.flash = it }
        binding.buttonVibrate.setCheckedChangedListener { settings.vibrate = it }
        binding.buttonContinuousScanning.setCheckedChangedListener { settings.continuousScanning = it }
        binding.buttonConfirmScansManually.setCheckedChangedListener { settings.confirmScansManually = it }
        binding.buttonSaveScannedBarcodes.setCheckedChangedListener { settings.saveScannedBarcodesToHistory = it }
        binding.buttonSaveCreatedBarcodes.setCheckedChangedListener { settings.saveCreatedBarcodesToHistory = it }
        binding.buttonDoNotSaveDuplicates.setCheckedChangedListener { settings.doNotSaveDuplicates = it }
        binding.buttonEnableErrorReports.setCheckedChangedListener { settings.areErrorReportsEnabled = it }
    }

    private fun handleButtonClicks() {
        binding.buttonChooseTheme.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ChooseThemeActivityBase.start(requireActivity())
            }
        }
        binding.buttonChooseLanguage.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ChooseLanguageActivity.start(requireActivity())
            }
        }
        binding.buttonChooseCamera.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ChooseCameraActivityBase.start(requireActivity())
            }
        }
        binding.buttonSelectSupportedFormats.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                SupportedFormatsActivityBase.start(requireActivity())
            }
        }
        binding.buttonClearHistory.setOnClickListener {
            showDeleteHistoryConfirmationDialog()
        }
        binding.buttonChooseSearchEngine.setOnClickListener {
            AdManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    android.util.Log.i("roy93~", "Ad đã hiển thị và đóng thành công")
                } else {
                    android.util.Log.i("roy93~", "Ad không hiển thị được hoặc có lỗi")
                }
                ChooseSearchEngineActivityBase.start(requireContext())
            }
        }
        binding.buttonPermissions.setOnClickListener {
            AllPermissionsActivityBase.start(requireActivity())
        }
        binding.buttonCheckUpdates.setOnClickListener {
            showAppInMarket()
        }
        binding.buttonSourceCode.setOnClickListener {
            context.openUrlInBrowser("https://github.com/wewewe718/QrAndBarcodeScanner")
        }
        binding.buttonSourceCodeFork.setOnClickListener {
            context.openUrlInBrowser("https://github.com/gj-loitp/5_QrAndBarcodeScanner")
        }
        binding.btRateApp.setOnClickListener {
            requireContext().rateApp(requireContext().packageName)
        }
        binding.btMoreApp.setOnClickListener {
            requireContext().moreApp()
        }
        binding.btShareApp.setOnClickListener {
            requireContext().shareApp()
        }
        binding.btPolicy.setOnClickListener {
            requireContext().openBrowserPolicy()
        }
    }

    private fun clearHistory() {
        binding.buttonClearHistory.isEnabled = false

        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    binding.buttonClearHistory.isEnabled = true
                },
                { error ->
                    binding.buttonClearHistory.isEnabled = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showSettings() {
        settings.apply {
            binding.buttonInverseBarcodeColorsInDarkTheme.isChecked = areBarcodeColorsInversed
            binding.buttonOpenLinksAutomatically.isChecked = openLinksAutomatically
            binding.buttonCopyToClipboard.isChecked = copyToClipboard
            binding.buttonSimpleAutoFocus.isChecked = simpleAutoFocus
            binding.buttonFlashlight.isChecked = flash
            binding.buttonVibrate.isChecked = vibrate
            binding.buttonContinuousScanning.isChecked = continuousScanning
            binding.buttonConfirmScansManually.isChecked = confirmScansManually
            binding.buttonSaveScannedBarcodes.isChecked = saveScannedBarcodesToHistory
            binding.buttonSaveCreatedBarcodes.isChecked = saveCreatedBarcodesToHistory
            binding.buttonDoNotSaveDuplicates.isChecked = doNotSaveDuplicates
            binding.buttonEnableErrorReports.isChecked = areErrorReportsEnabled
        }
    }

    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DialogFragmentDeleteConfirmation.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }

    private fun showAppInMarket() {
        val uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags =
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun showAppVersion() {
        binding.buttonAppVersion.hint = BuildConfig.VERSION_NAME
    }



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
}