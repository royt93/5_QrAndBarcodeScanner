package com.mckimquyen.barcodescanner.feature.tabs.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.mckimquyen.barcodescanner.BuildConfig
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.ext.moreApp
import com.mckimquyen.barcodescanner.extension.ext.openBrowserPolicy
import com.mckimquyen.barcodescanner.extension.ext.openUrlInBrowser
import com.mckimquyen.barcodescanner.extension.ext.rateApp
import com.mckimquyen.barcodescanner.extension.ext.shareApp
import com.mckimquyen.barcodescanner.extension.packageManager
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentDeleteConfirmation
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.ActivityCreateBarcodeAll
import com.mckimquyen.barcodescanner.feature.tabs.setting.camera.ChooseCameraActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.formats.SupportedFormatsActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.permissions.AllPermissionsActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.search.ChooseSearchEngineActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.theme.ChooseThemeActivityBase
import com.mckimquyen.barcodescanner.feature.tabs.setting.language.ChooseLanguageActivity
import com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager
import com.mckimquyen.barcodescanner.sdkadbmob.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.f_settings.*

class SettingsFragment : Fragment(), DialogFragmentDeleteConfirmation.Listener, AdMobManager.InterstitialAdListener {
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.f_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdMobManager.setCurrentActivity(requireActivity())
        AdMobManager.interstitialListener = this

//        createAdInter()
        AdMobManager.loadInterstitial(requireContext(), BuildConfig.ADMOB_INTERSTITIAL_ID)
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
    }

    fun supportEdgeToEdge() {
        appBarLayout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonCheckedChanged() {
        buttonInverseBarcodeColorsInDarkTheme.setCheckedChangedListener { settings.areBarcodeColorsInversed = it }
        buttonOpenLinksAutomatically.setCheckedChangedListener { settings.openLinksAutomatically = it }
        buttonCopyToClipboard.setCheckedChangedListener { settings.copyToClipboard = it }
        buttonSimpleAutoFocus.setCheckedChangedListener { settings.simpleAutoFocus = it }
        buttonFlashlight.setCheckedChangedListener { settings.flash = it }
        buttonVibrate.setCheckedChangedListener { settings.vibrate = it }
        buttonContinuousScanning.setCheckedChangedListener { settings.continuousScanning = it }
        buttonConfirmScansManually.setCheckedChangedListener { settings.confirmScansManually = it }
        buttonSaveScannedBarcodes.setCheckedChangedListener { settings.saveScannedBarcodesToHistory = it }
        buttonSaveCreatedBarcodes.setCheckedChangedListener { settings.saveCreatedBarcodesToHistory = it }
        buttonDoNotSaveDuplicates.setCheckedChangedListener { settings.doNotSaveDuplicates = it }
        buttonEnableErrorReports.setCheckedChangedListener { settings.areErrorReportsEnabled = it }
    }

    private fun handleButtonClicks() {
        buttonChooseTheme.setOnClickListener {
            AdMobManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    Logger.i("Ad đã hiển thị và đóng thành công")
                } else {
                    Logger.i("Ad không hiển thị được hoặc có lỗi")
                }
                ChooseThemeActivityBase.start(requireActivity())
            }
        }
        buttonChooseLanguage.setOnClickListener {
            AdMobManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    Logger.i("Ad đã hiển thị và đóng thành công")
                } else {
                    Logger.i("Ad không hiển thị được hoặc có lỗi")
                }
                ChooseLanguageActivity.start(requireActivity())
            }
        }
        buttonChooseCamera.setOnClickListener {
            AdMobManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    Logger.i("Ad đã hiển thị và đóng thành công")
                } else {
                    Logger.i("Ad không hiển thị được hoặc có lỗi")
                }
                ChooseCameraActivityBase.start(requireActivity())
            }
        }
        buttonSelectSupportedFormats.setOnClickListener {
            AdMobManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    Logger.i("Ad đã hiển thị và đóng thành công")
                } else {
                    Logger.i("Ad không hiển thị được hoặc có lỗi")
                }
                SupportedFormatsActivityBase.start(requireActivity())
            }
        }
        buttonClearHistory.setOnClickListener {
            //TODO roy93~ update dialog material 3
            showDeleteHistoryConfirmationDialog()
        }
        buttonChooseSearchEngine.setOnClickListener {
            AdMobManager.showInterstitial(requireActivity()) { success ->
                if (success) {
                    Logger.i("Ad đã hiển thị và đóng thành công")
                } else {
                    Logger.i("Ad không hiển thị được hoặc có lỗi")
                }
                ChooseSearchEngineActivityBase.start(requireContext())
            }
        }
        buttonPermissions.setOnClickListener {
            AllPermissionsActivityBase.start(requireActivity())
        }
        buttonCheckUpdates.setOnClickListener {
            showAppInMarket()
        }
        buttonSourceCode.setOnClickListener {
            context.openUrlInBrowser("https://github.com/wewewe718/QrAndBarcodeScanner")
        }
        buttonSourceCodeFork.setOnClickListener {
            context.openUrlInBrowser("https://github.com/gj-loitp/5_QrAndBarcodeScanner")
        }
        btRateApp.setOnClickListener {
            requireContext().rateApp(requireContext().packageName)
        }
        btMoreApp.setOnClickListener {
            requireContext().moreApp()
        }
        btShareApp.setOnClickListener {
            requireContext().shareApp()
        }
        btPolicy.setOnClickListener {
            requireContext().openBrowserPolicy()
        }
    }

    private fun clearHistory() {
        buttonClearHistory.isEnabled = false

        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    buttonClearHistory.isEnabled = true
                },
                { error ->
                    buttonClearHistory.isEnabled = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showSettings() {
        settings.apply {
            buttonInverseBarcodeColorsInDarkTheme.isChecked = areBarcodeColorsInversed
            buttonOpenLinksAutomatically.isChecked = openLinksAutomatically
            buttonCopyToClipboard.isChecked = copyToClipboard
            buttonSimpleAutoFocus.isChecked = simpleAutoFocus
            buttonFlashlight.isChecked = flash
            buttonVibrate.isChecked = vibrate
            buttonContinuousScanning.isChecked = continuousScanning
            buttonConfirmScansManually.isChecked = confirmScansManually
            buttonSaveScannedBarcodes.isChecked = saveScannedBarcodesToHistory
            buttonSaveCreatedBarcodes.isChecked = saveCreatedBarcodesToHistory
            buttonDoNotSaveDuplicates.isChecked = doNotSaveDuplicates
            buttonEnableErrorReports.isChecked = areErrorReportsEnabled
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
        buttonAppVersion.hint = BuildConfig.VERSION_NAME
    }

    override fun onAdLoaded() {
    }

    override fun onAdFailedToLoad(error: LoadAdError) {
    }

    override fun onAdShowed() {
    }

    override fun onAdDismissed() {
    }

    override fun onAdClicked() {
    }

    override fun onAdFailedToShow(error: AdError) {
    }

    override fun onAdNotAvailable() {
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