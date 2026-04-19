package com.mckimquyen.barcodescanner.feature.barcode

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.print.PrintHelper
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.*
import com.mckimquyen.barcodescanner.extension.*
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.barcode.otp.ActivityOtp
import com.mckimquyen.barcodescanner.feature.barcode.save.ActivitySaveBarcodeAsImage
import com.mckimquyen.barcodescanner.feature.barcode.save.ActivitySaveBarcodeAsText
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentChooseSearchEngine
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentDeleteConfirmation
import com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentEditBarcodeName
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.model.ParsedBarcode
import com.mckimquyen.barcodescanner.model.SearchEngine
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema
import com.mckimquyen.barcodescanner.model.schema.OtpAuth
import com.mckimquyen.barcodescanner.usecase.Logger
import com.mckimquyen.barcodescanner.usecase.save
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.a_barcode.*
import java.text.SimpleDateFormat
import java.util.*


class ActivityBarcode : ActivityBase(), DialogFragmentDeleteConfirmation.Listener,
    DialogFragmentChooseSearchEngine.Listener, DialogFragmentEditBarcodeName.Listener,
    com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentSecurityAlert.Listener {

    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
        private const val IS_CREATED = "IS_CREATED"

        fun start(context: Context, barcode: Barcode, isCreated: Boolean = false) {
            val intent = Intent(context, ActivityBarcode::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
                putExtra(IS_CREATED, isCreated)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    private val originalBarcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    private val isCreated by unsafeLazy {
        intent?.getBooleanExtra(IS_CREATED, false).orFalse()
    }

    private val barcode by unsafeLazy {
        ParsedBarcode(originalBarcode)
    }

    private val clipboardManager by unsafeLazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private var originalBrightness: Float = 0.5f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.a_barcode)

        supportEdgeToEdge()
        saveOriginalBrightness()
        applySettings()

        handleToolbarBackPressed()
        handleToolbarMenuClicked()
        handleButtonsClicked()

        showBarcode()
        showOrHideButtons()
        showButtonText()
    }

    override fun onDeleteConfirmed() {
        deleteBarcode()
    }

    override fun onNameConfirmed(name: String) {
        updateBarcodeName(name)
    }

    override fun onSearchEngineSelected(searchEngine: SearchEngine) {
        performWebSearchUsingSearchEngine(searchEngine)
    }

    override fun onSecurityProceed(url: String) {
        startActivityIfExists(Intent.ACTION_VIEW, url)
    }

    override fun onSecurityCancel() {
        // Do nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    private fun supportEdgeToEdge() {
        rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun saveOriginalBrightness() {
        originalBrightness = window.attributes.screenBrightness
    }

    private fun applySettings() {
        if (settings.copyToClipboard) {
            copyToClipboard(barcode.text)
        }

        if (settings.openLinksAutomatically.not() || isCreated) {
            return
        }

        when (barcode.schema) {
            BarcodeSchema.APP -> openInAppMarket()
            BarcodeSchema.BOOKMARK -> saveBookmark()
            BarcodeSchema.CRYPTOCURRENCY -> openBitcoinUrl()
            BarcodeSchema.EMAIL -> sendEmail(barcode.email)
            BarcodeSchema.GEO -> showLocation()
            BarcodeSchema.GOOGLE_MAPS -> showLocation()
            BarcodeSchema.MMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.MECARD -> addToContacts()
            BarcodeSchema.OTP_AUTH -> openOtpInOtherApp()
            BarcodeSchema.PHONE -> callPhone(barcode.phone)
            BarcodeSchema.SMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.URL -> openLink()
            BarcodeSchema.VEVENT -> addToCalendar()
            BarcodeSchema.VCARD -> addToContacts()
            BarcodeSchema.WIFI -> connectToWifi()
            BarcodeSchema.YOUTUBE -> openInYoutube()
            BarcodeSchema.NZCOVIDTRACER -> openLink()
            BarcodeSchema.BOARDINGPASS -> return
            else -> return
        }
    }


    private fun handleToolbarBackPressed() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemIncreaseBrightness -> {
                    increaseBrightnessToMax()
                    toolbar.menu.findItem(R.id.itemIncreaseBrightness).isVisible = false
                    toolbar.menu.findItem(R.id.itemDecreaseBrightness).isVisible = true
                }

                R.id.itemDecreaseBrightness -> {
                    restoreOriginalBrightness()
                    toolbar.menu.findItem(R.id.itemIncreaseBrightness).isVisible = true
                    toolbar.menu.findItem(R.id.itemDecreaseBrightness).isVisible = false
                }

                R.id.itemAddToFavorites -> toggleIsFavorite()
                R.id.itemShowBarcodeImage -> navigateToBarcodeImageActivity()
                R.id.itemSave -> saveBarcode()
                R.id.itemDelete -> showDeleteBarcodeConfirmationDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun handleButtonsClicked() {
        buttonEditName.setOnClickListener {
            showEditBarcodeNameDialog()
        }
        buttonSearchOnWeb.setOnClickListener {
            searchBarcodeTextOnInternet()
        }
        buttonAddToCalendar.setOnClickListener {
            addToCalendar()
        }
        buttonAddToContacts.setOnClickListener {
            addToContacts()
        }
        buttonShowLocation.setOnClickListener {
            showLocation()
        }
        buttonConnectToWifi.setOnClickListener {
            connectToWifi()
        }
        buttonOpenWifiSettings.setOnClickListener {
            openWifiSettings()
        }
        buttonCopyNetworkName.setOnClickListener {
            copyNetworkNameToClipboard()
        }
        buttonCopyNetworkPassword.setOnClickListener {
            copyNetworkPasswordToClipboard()
        }
        buttonOpenApp.setOnClickListener {
            openApp()
        }
        buttonOpenInAppMarket.setOnClickListener {
            openInAppMarket()
        }
        buttonOpenInYoutube.setOnClickListener {
            openInYoutube()
        }
        buttonShowOtp.setOnClickListener {
            showOtp()
        }
        buttonOpenOtp.setOnClickListener {
            openOtpInOtherApp()
        }
        buttonOpenBitcoinUri.setOnClickListener {
            openBitcoinUrl()
        }
        buttonOpenLink.setOnClickListener {
            openLink()
        }
        buttonSaveBookmark.setOnClickListener {
            saveBookmark()
        }
        buttonCallPhone1.setOnClickListener {
            callPhone(barcode.phone)
        }
        buttonCallPhone2.setOnClickListener {
            callPhone(barcode.secondaryPhone)
        }
        buttonCallPhone3.setOnClickListener {
            callPhone(barcode.tertiaryPhone)
        }
        buttonSendSmsOrMms1.setOnClickListener {
            sendSmsOrMms(barcode.phone)
        }
        button_send_sms_or_mms_2.setOnClickListener {
            sendSmsOrMms(barcode.secondaryPhone)
        }
        buttonSendSmsOrMms3.setOnClickListener {
            sendSmsOrMms(barcode.tertiaryPhone)
        }
        buttonSendEmail1.setOnClickListener {
            sendEmail(barcode.email)
        }
        buttonSendEmail2.setOnClickListener {
            sendEmail(barcode.secondaryEmail)
        }
        buttonSendEmail3.setOnClickListener {
            sendEmail(barcode.tertiaryEmail)
        }
        buttonShareAsText.setOnClickListener {
            shareBarcodeAsText()
        }
        buttonCopy.setOnClickListener {
            copyBarcodeTextToClipboard()
        }
        buttonSearch.setOnClickListener {
            searchBarcodeTextOnInternet()
        }
        buttonSaveAsText.setOnClickListener {
            navigateToSaveBarcodeAsTextActivity()
        }
        buttonShareAsImage.setOnClickListener {
            shareBarcodeAsImage()
        }
        buttonSaveAsImage.setOnClickListener {
            navigateToSaveBarcodeAsImageActivity()
        }
        buttonPrint.setOnClickListener {
            printBarcode()
        }
    }


    private fun toggleIsFavorite() {
        val newBarcode = originalBarcode.copy(isFavorite = barcode.isFavorite.not())

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.isFavorite = newBarcode.isFavorite
                    showBarcodeIsFavorite(newBarcode.isFavorite)
                },
                {}
            )
            .addTo(disposable)
    }

    private fun updateBarcodeName(name: String) {
        if (name.isBlank()) {
            return
        }

        val newBarcode = originalBarcode.copy(
            id = barcode.id,
            name = name
        )

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.name = name
                    showBarcodeName(name)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun saveBarcode() {
        toolbar?.menu?.findItem(R.id.itemSave)?.isVisible = false

        barcodeDatabase.save(originalBarcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    barcode.id = id
                    buttonEditName.isVisible = true
                    toolbar?.menu?.findItem(R.id.itemDelete)?.isVisible = true
                },
                { error ->
                    toolbar?.menu?.findItem(R.id.itemSave)?.isVisible = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun deleteBarcode() {
        showLoading(true)

        barcodeDatabase.delete(barcode.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { finish() },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun addToCalendar() {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, barcode.eventSummary)
            putExtra(CalendarContract.Events.DESCRIPTION, barcode.eventDescription)
            putExtra(CalendarContract.Events.EVENT_LOCATION, barcode.eventLocation)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, barcode.eventStartDate)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, barcode.eventEndDate)
        }
        startActivityIfExists(intent)
    }

    private fun addToContacts() {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE

            val fullName = "${barcode.firstName.orEmpty()} ${barcode.lastName.orEmpty()}"
            putExtra(ContactsContract.Intents.Insert.NAME, fullName)
            putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.organization.orEmpty())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, barcode.jobTitle.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE, barcode.phone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, barcode.phoneType.orEmpty().toPhoneType())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, barcode.secondaryPhone.orEmpty())
            putExtra(
                ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                barcode.secondaryPhoneType.orEmpty().toPhoneType()
            )
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, barcode.tertiaryPhone.orEmpty())
            putExtra(
                ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                barcode.tertiaryPhoneType.orEmpty().toPhoneType()
            )
            putExtra(ContactsContract.Intents.Insert.EMAIL, barcode.email.orEmpty())
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, barcode.emailType.orEmpty().toEmailType())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, barcode.secondaryEmail.orEmpty())
            putExtra(
                ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
                barcode.secondaryEmailType.orEmpty().toEmailType()
            )
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, barcode.tertiaryEmail.orEmpty())
            putExtra(
                ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE,
                barcode.tertiaryEmailType.orEmpty().toEmailType()
            )

            putExtra(ContactsContract.Intents.Insert.NOTES, barcode.note.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun callPhone(phone: String?) {
        val phoneUri = "tel:${phone.orEmpty()}"
        startActivityIfExists(Intent.ACTION_DIAL, phoneUri)
    }

    private fun sendSmsOrMms(phone: String?) {
        val uri = Uri.parse("sms:${phone.orEmpty()}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", barcode.smsBody.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun sendEmail(email: String?) {
        val uri = Uri.parse("mailto:${email.orEmpty()}")
        val intent = Intent(Intent.ACTION_SEND, uri).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.orEmpty()))
            putExtra(Intent.EXTRA_SUBJECT, barcode.emailSubject.orEmpty())
            putExtra(Intent.EXTRA_TEXT, barcode.emailBody.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun showLocation() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.geoUri.orEmpty())
    }

    private fun connectToWifi() {
        showConnectToWifiButtonEnabled(false)

        wifiConnector.connect(
            context = this,
            authType = barcode.networkAuthType.orEmpty(),
            name = barcode.networkName.orEmpty(),
            password = barcode.networkPassword.orEmpty(),
            isHidden = barcode.isHidden.orFalse(),
            anonymousIdentity = barcode.anonymousIdentity.orEmpty(),
            identity = barcode.identity.orEmpty(),
            eapMethod = barcode.eapMethod.orEmpty(),
            phase2Method = barcode.phase2Method.orEmpty()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showConnectToWifiButtonEnabled(true)
                    showToast(R.string.activity_barcode_connecting_to_wifi)
                },
                { error ->
                    showConnectToWifiButtonEnabled(true)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivityIfExists(intent)
    }

    private fun copyNetworkNameToClipboard() {
        copyToClipboard(barcode.networkName.orEmpty())
        showToast(R.string.activity_barcode_copied)
    }

    private fun copyNetworkPasswordToClipboard() {
        copyToClipboard(barcode.networkPassword.orEmpty())
        showToast(R.string.activity_barcode_copied)
    }

    private fun openApp() {
        val intent = packageManager?.getLaunchIntentForPackage(barcode.appPackage.orEmpty())
        if (intent != null) {
            startActivityIfExists(intent)
        }
    }

    private fun openInAppMarket() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.appMarketUrl.orEmpty())
    }

    private fun openInYoutube() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.youtubeUrl.orEmpty())
    }

    private fun showOtp() {
        val otp = OtpAuth.parse(barcode.otpUrl.orEmpty()) ?: return
        ActivityOtp.start(this, otp)
    }

    private fun openOtpInOtherApp() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.otpUrl.orEmpty())
    }

    private fun openBitcoinUrl() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.bitcoinUri.orEmpty())
    }

    private fun openLink() {
        val url = barcode.url.orEmpty()
        if (url.isBlank()) return
        
        val safeDomains = listOf("google.com", "facebook.com", "youtube.com", "vnexpress.net", "github.com", "amazon.com", "apple.com")
        var isSafe = false
        for (domain in safeDomains) {
            if (url.contains(domain, ignoreCase = true)) {
                isSafe = true
                break
            }
        }
        
        if (!isSafe) {
            val dialog = com.mckimquyen.barcodescanner.feature.common.dlg.DialogFragmentSecurityAlert.newInstance(url)
            dialog.show(supportFragmentManager, "SecurityAlert")
        } else {
            startActivityIfExists(Intent.ACTION_VIEW, url)
        }
    }

    private fun saveBookmark() {
        val intent = Intent(Intent.ACTION_INSERT, Uri.parse("content://browser/bookmarks")).apply {
            putExtra("title", barcode.bookmarkTitle.orEmpty())
            putExtra("url", barcode.url.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun shareBarcodeAsText() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, barcode.text)
        }
        startActivityIfExists(intent)
    }

    private fun copyBarcodeTextToClipboard() {
        copyToClipboard(barcode.text)
        showToast(R.string.activity_barcode_copied)
    }

    private fun searchBarcodeTextOnInternet() {
        val searchEngine = settings.searchEngine
        when (searchEngine) {
            SearchEngine.NONE -> performWebSearch()
            SearchEngine.ASK_EVERY_TIME -> showSearchEnginesDialog()
            else -> performWebSearchUsingSearchEngine(searchEngine)
        }
    }

    private fun performWebSearch() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, barcode.text)
        }
        startActivityIfExists(intent)
    }

    private fun performWebSearchUsingSearchEngine(searchEngine: SearchEngine) {
        val url = searchEngine.templateUrl + barcode.text
        startActivityIfExists(Intent.ACTION_VIEW, url)
    }

    private fun shareBarcodeAsImage() {
        val imageUri = try {
            val image = barcodeImageGenerator.generateBitmap(
                barcode = originalBarcode,
                width = 200,
                height = 200,
                margin = 1
            )
            barcodeImageSaver.saveImageToCache(this, image, barcode)
        } catch (ex: Exception) {
            Logger.log(ex)
            showError(ex)
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityIfExists(intent)
    }

    private fun printBarcode() {
        val barcodeImage = try {
            barcodeImageGenerator.generateBitmap(
                barcode = originalBarcode,
                width = 1000,
                height = 1000,
                margin = 3
            )
        } catch (ex: Exception) {
            Logger.log(ex)
            showError(ex)
            return
        }

        PrintHelper(this).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            printBitmap("${barcode.format}_${barcode.schema}_${barcode.date}", barcodeImage)
        }
    }

    private fun navigateToBarcodeImageActivity() {
        ActivityBarcodeImage.start(this, originalBarcode)
    }

    private fun navigateToSaveBarcodeAsTextActivity() {
        ActivitySaveBarcodeAsText.start(this, originalBarcode)
    }

    private fun navigateToSaveBarcodeAsImageActivity() {
        ActivitySaveBarcodeAsImage.start(this, originalBarcode)
    }


    private fun showBarcode() {
        showBarcodeMenuIfNeeded()
        showBarcodeIsFavorite()
        showBarcodeImageIfNeeded()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeName()
        showBarcodeText()
        showBarcodeCountry()
    }

    private fun showBarcodeMenuIfNeeded() {
        toolbar.inflateMenu(R.menu.menu_barcode)
        toolbar.menu.apply {
            findItem(R.id.itemIncreaseBrightness).isVisible = isCreated
            findItem(R.id.itemAddToFavorites)?.isVisible = barcode.isInDb
            findItem(R.id.itemShowBarcodeImage)?.isVisible = isCreated.not()
            findItem(R.id.itemSave)?.isVisible = barcode.isInDb.not()
            findItem(R.id.itemDelete)?.isVisible = barcode.isInDb
        }
    }

    private fun showBarcodeIsFavorite() {
        showBarcodeIsFavorite(barcode.isFavorite)
    }

    private fun showBarcodeIsFavorite(isFavorite: Boolean) {
        val iconId = if (isFavorite) {
            R.drawable.ic_favorite_checked
        } else {
            R.drawable.ic_favorite_unchecked
        }
        toolbar.menu?.findItem(R.id.itemAddToFavorites)?.icon = ContextCompat.getDrawable(this, iconId)
    }

    private fun showBarcodeImageIfNeeded() {
        if (isCreated) {
            showBarcodeImage()
        }
    }

    private fun showBarcodeImage() {
        try {
            val bitmap = barcodeImageGenerator.generateBitmap(
                barcode = originalBarcode,
                width = 2000,
                height = 2000,
                margin = 0,
                codeColor = settings.barcodeContentColor,
                backgroundColor = settings.barcodeBackgroundColor
            )
            layoutBarcodeImageBackground.isVisible = true
            imageViewBarcode.isVisible = true
            imageViewBarcode.setImageBitmap(bitmap)
            imageViewBarcode.setBackgroundColor(settings.barcodeBackgroundColor)
            layoutBarcodeImageBackground.setBackgroundColor(settings.barcodeBackgroundColor)

            if (settings.isDarkTheme.not() || settings.areBarcodeColorsInversed) {
                layoutBarcodeImageBackground.setPadding(
                    /* left = */ 0,
                    /* top = */ 0,
                    /* right = */ 0,
                    /* bottom = */ 0
                )
            }
        } catch (ex: Exception) {
            Logger.log(ex)
            imageViewBarcode.isVisible = false
        }
    }

    private fun showBarcodeDate() {
        textViewDate.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        toolbar.setTitle(format)
    }

    private fun showBarcodeName() {
        showBarcodeName(barcode.name)
    }

    private fun showBarcodeName(name: String?) {
        textViewBarcodeName.isVisible = name.isNullOrBlank().not()
        textViewBarcodeName.text = name.orEmpty()
    }

    private fun showBarcodeText() {
        textViewBarcodeText.text = if (isCreated) {
            barcode.text
        } else {
            barcode.formattedText
        }
    }

    private fun showBarcodeCountry() {
        val country = barcode.country ?: return
        when (country.contains('/')) {
            false -> showOneBarcodeCountry(country)
            true -> showTwoBarcodeCountries(country.split('/'))
        }
    }

    private fun showOneBarcodeCountry(country: String) {
        val fullCountryName = buildFullCountryName(country)
        showFullCountryName(fullCountryName)
    }

    private fun showTwoBarcodeCountries(countries: List<String>) {
        val firstFullCountryName = buildFullCountryName(countries[0])
        val secondFullCountryName = buildFullCountryName(countries[1])
        val fullCountryName = "$firstFullCountryName / $secondFullCountryName"
        showFullCountryName(fullCountryName)
    }

    private fun buildFullCountryName(country: String): String {
        val currentLocale = currentLocale ?: return ""
        val countryName = Locale("", country).getDisplayName(currentLocale)
        val countryEmoji = country.toCountryEmoji()
        return "$countryEmoji $countryName"
    }

    private fun showFullCountryName(fullCountryName: String) {
        textViewCountry.apply {
            text = fullCountryName
            isVisible = fullCountryName.isBlank().not()
        }
    }

    private fun showOrHideButtons() {
        buttonSearch.isVisible = isCreated.not()
        buttonEditName.isVisible = barcode.isInDb

        if (isCreated) {
            return
        }

        buttonSearchOnWeb.isVisible = barcode.isProductBarcode
        buttonSearch.isVisible = barcode.isProductBarcode.not()

        buttonAddToCalendar.isVisible = barcode.schema == BarcodeSchema.VEVENT
        buttonAddToContacts.isVisible =
            barcode.schema == BarcodeSchema.VCARD || barcode.schema == BarcodeSchema.MECARD

        buttonCallPhone1.isVisible = barcode.phone.isNullOrEmpty().not()
        buttonCallPhone2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        buttonCallPhone3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()

        buttonSendSmsOrMms1.isVisible =
            barcode.phone.isNullOrEmpty().not() || barcode.smsBody.isNullOrEmpty().not()
        button_send_sms_or_mms_2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        buttonSendSmsOrMms3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()

        buttonSendEmail1.isVisible = barcode.email.isNullOrEmpty().not() || barcode.emailSubject.isNullOrEmpty()
            .not() || barcode.emailBody.isNullOrEmpty().not()
        buttonSendEmail2.isVisible = barcode.secondaryEmail.isNullOrEmpty().not()
        buttonSendEmail3.isVisible = barcode.tertiaryEmail.isNullOrEmpty().not()

        buttonShowLocation.isVisible = barcode.geoUri.isNullOrEmpty().not()
        buttonConnectToWifi.isVisible = barcode.schema == BarcodeSchema.WIFI
        buttonOpenWifiSettings.isVisible = barcode.schema == BarcodeSchema.WIFI
        buttonCopyNetworkName.isVisible = barcode.networkName.isNullOrEmpty().not()
        buttonCopyNetworkPassword.isVisible = barcode.networkPassword.isNullOrEmpty().not()
        buttonOpenApp.isVisible = barcode.appPackage.isNullOrEmpty().not() && isAppInstalled(barcode.appPackage)
        buttonOpenInAppMarket.isVisible = barcode.appMarketUrl.isNullOrEmpty().not()
        buttonOpenInYoutube.isVisible = barcode.youtubeUrl.isNullOrEmpty().not()
        buttonShowOtp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        buttonOpenOtp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        buttonOpenBitcoinUri.isVisible = barcode.bitcoinUri.isNullOrEmpty().not()
        buttonOpenLink.isVisible = barcode.url.isNullOrEmpty().not()
        buttonSaveBookmark.isVisible = barcode.schema == BarcodeSchema.BOOKMARK
    }

    private fun showButtonText() {
        buttonCallPhone1.text = getString(R.string.activity_barcode_call_phone, barcode.phone)
        buttonCallPhone2.text = getString(R.string.activity_barcode_call_phone, barcode.secondaryPhone)
        buttonCallPhone3.text = getString(R.string.activity_barcode_call_phone, barcode.tertiaryPhone)

        buttonSendSmsOrMms1.text = getString(R.string.activity_barcode_send_sms, barcode.phone)
        button_send_sms_or_mms_2.text = getString(R.string.activity_barcode_send_sms, barcode.secondaryPhone)
        buttonSendSmsOrMms3.text = getString(R.string.activity_barcode_send_sms, barcode.tertiaryPhone)

        buttonSendEmail1.text = getString(R.string.activity_barcode_send_email, barcode.email)
        buttonSendEmail2.text = getString(R.string.activity_barcode_send_email, barcode.secondaryEmail)
        buttonSendEmail3.text = getString(R.string.activity_barcode_send_email, barcode.tertiaryEmail)
    }

    private fun showConnectToWifiButtonEnabled(isEnabled: Boolean) {
        buttonConnectToWifi.isEnabled = isEnabled
    }

    private fun showDeleteBarcodeConfirmationDialog() {
        val dialog = DialogFragmentDeleteConfirmation.newInstance(R.string.dialog_delete_barcode_message)
        dialog.show(supportFragmentManager, "")
    }

    private fun showEditBarcodeNameDialog() {
        val dialog = DialogFragmentEditBarcodeName.newInstance(barcode.name)
        dialog.show(supportFragmentManager, "")
    }

    private fun showSearchEnginesDialog() {
        val dialog = DialogFragmentChooseSearchEngine()
        dialog.show(supportFragmentManager, "")
    }

    private fun showLoading(isLoading: Boolean) {
        progressBarLoading.isVisible = isLoading
        scrollView.isVisible = isLoading.not()
    }


    private fun startActivityIfExists(action: String, uri: String) {
        val intent = Intent(action, Uri.parse(uri))
        startActivityIfExists(intent)
    }

    private fun startActivityIfExists(intent: Intent) {
        intent.apply {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showToast(R.string.activity_barcode_no_app)
        }
    }

    private fun isAppInstalled(appPackage: String?): Boolean {
        return packageManager?.getLaunchIntentForPackage(appPackage.orEmpty()) != null
    }

    private fun copyToClipboard(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun increaseBrightnessToMax() {
        setBrightness(1.0f)
    }

    private fun restoreOriginalBrightness() {
        setBrightness(originalBrightness)
    }

    private fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = brightness
        }
    }
}
