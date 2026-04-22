package com.mckimquyen.barcodescanner.feature.tabs.create

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.ACreateBarcodeBinding
import com.mckimquyen.barcodescanner.di.barcodeDatabase
import com.mckimquyen.barcodescanner.di.barcodeParser
import com.mckimquyen.barcodescanner.di.contactHelper
import com.mckimquyen.barcodescanner.di.permissionsHelper
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.showError
import com.mckimquyen.barcodescanner.extension.toStringId
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.barcode.ActivityBarcode
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateAztec
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateCodabar
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateCode128
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateCode39
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateCode93
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateDataMatrix
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateEan13
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateEan8
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateItf14
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreatePdf417
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateUpcA
import com.mckimquyen.barcodescanner.feature.tabs.create.barcode.FragmentCreateUpcE
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.AdapterApp
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeApp
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeBookmark
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeCryptocurrency
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeEmail
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeEvent
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeLocation
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeMeCard
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeMms
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeOtp
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodePhone
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeSms
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeText
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeUrl
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeVCard
import com.mckimquyen.barcodescanner.feature.tabs.create.qr.FragmentCreateQrCodeWifi
import com.mckimquyen.barcodescanner.model.Barcode
import com.mckimquyen.barcodescanner.model.schema.App
import com.mckimquyen.barcodescanner.model.schema.BarcodeSchema
import com.mckimquyen.barcodescanner.model.schema.Schema
import com.mckimquyen.barcodescanner.usecase.Logger
import com.mckimquyen.barcodescanner.usecase.save
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ActivityCreateBarcode : ActivityBase(), AdapterApp.Listener {
    private lateinit var binding: ACreateBarcodeBinding


    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"

        private const val CHOOSE_PHONE_REQUEST_CODE = 1
        private const val CHOOSE_CONTACT_REQUEST_CODE = 2

        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
        private val CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)

        fun start(
            context: Context,
            barcodeFormat: BarcodeFormat,
            barcodeSchema: BarcodeSchema? = null,
            defaultText: String? = null,
        ) {
            val intent = Intent(context, ActivityCreateBarcode::class.java).apply {
                putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
                putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
                putExtra(DEFAULT_TEXT_KEY, defaultText)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()

    private val barcodeFormat by unsafeLazy {
        BarcodeFormat.values().getOrNull(intent?.getIntExtra(BARCODE_FORMAT_KEY, -1) ?: -1)
            ?: BarcodeFormat.QR_CODE
    }

    private val barcodeSchema by unsafeLazy {
        BarcodeSchema.values().getOrNull(intent?.getIntExtra(BARCODE_SCHEMA_KEY, -1) ?: -1)
    }

    private val defaultText by unsafeLazy {
        intent?.getStringExtra(DEFAULT_TEXT_KEY).orEmpty()
    }

    var isCreateBarcodeButtonEnabled: Boolean
        get() = false
        set(enabled) {
            val iconId = if (enabled) {
                R.drawable.ic_confirm_enabled
            } else {
                R.drawable.ic_confirm_disabled
            }

            binding.toolbar.menu?.findItem(R.id.itemCreateBarcode)?.apply {
                icon = ContextCompat.getDrawable(this@ActivityCreateBarcode, iconId)
                isEnabled = enabled
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (createBarcodeImmediatelyIfNeeded()) {
            return
        }

        binding = ACreateBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        handleToolbarBackClicked()
        handleToolbarMenuItemClicked()
        showToolbarTitle()
        showToolbarMenu()
        showFragment()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            CHOOSE_PHONE_REQUEST_CODE -> showChosenPhone(data)
            CHOOSE_CONTACT_REQUEST_CODE -> showChosenContact(data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && permissionsHelper.areAllPermissionsGranted(grantResults)) {
            chooseContact()
        }
    }

    override fun onAppClicked(packageName: String) {
        createBarcode(App.fromPackage(packageName))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun createBarcodeImmediatelyIfNeeded(): Boolean {
        if (intent?.action != Intent.ACTION_SEND) {
            return false
        }

        return when (intent?.type) {
            "text/plain" -> {
                createBarcodeForPlainText()
                true
            }

            "text/x-vcard" -> {
                createBarcodeForVCard()
                true
            }

            else -> false
        }
    }

    private fun createBarcodeForPlainText() {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun createBarcodeForVCard() {
        val uri = intent?.extras?.get(Intent.EXTRA_STREAM) as? Uri ?: return
        val text = readDataFromVCardUri(uri).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun readDataFromVCardUri(uri: Uri): String? {
        val stream = try {
            contentResolver.openInputStream(uri) ?: return null
        } catch (e: Exception) {
            Logger.log(e)
            return null
        }

        val fileContent = StringBuilder("")

        var ch: Int
        try {
            while (stream.read().also { ch = it } != -1) {
                fileContent.append(ch.toChar())
            }
        } catch (e: Exception) {
            Logger.log(e)
        }
        stream.close()

        return fileContent.toString()
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuItemClicked() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemPhone -> choosePhone()
                R.id.itemContacts -> requestContactsPermissions()
                R.id.itemCreateBarcode -> createBarcode()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showToolbarTitle() {
        val titleId = barcodeSchema?.toStringId() ?: barcodeFormat.toStringId()
        binding.toolbar.setTitle(titleId)
    }

    private fun showToolbarMenu() {
        val menuId = when (barcodeSchema) {
            BarcodeSchema.APP -> return
            BarcodeSchema.PHONE, BarcodeSchema.SMS, BarcodeSchema.MMS -> R.menu.menu_create_qr_code_phone
            BarcodeSchema.VCARD, BarcodeSchema.MECARD -> R.menu.menu_create_qr_code_contacts
            else -> R.menu.menu_create_barcode
        }
        binding.toolbar.inflateMenu(menuId)
    }

    private fun showFragment() {
        val fragment = when {
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTHER -> FragmentCreateQrCodeText.newInstance(
                defaultText
            )

            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.URL -> FragmentCreateQrCodeUrl()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.BOOKMARK -> FragmentCreateQrCodeBookmark()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.PHONE -> FragmentCreateQrCodePhone()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.WIFI -> FragmentCreateQrCodeWifi()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.EMAIL -> FragmentCreateQrCodeEmail()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.SMS -> FragmentCreateQrCodeSms()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MMS -> FragmentCreateQrCodeMms()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.CRYPTOCURRENCY -> FragmentCreateQrCodeCryptocurrency()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.GEO -> FragmentCreateQrCodeLocation()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.APP -> FragmentCreateQrCodeApp()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTP_AUTH -> FragmentCreateQrCodeOtp()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VEVENT -> FragmentCreateQrCodeEvent()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VCARD -> FragmentCreateQrCodeVCard()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MECARD -> FragmentCreateQrCodeMeCard()
            barcodeFormat == BarcodeFormat.DATA_MATRIX -> FragmentCreateDataMatrix()
            barcodeFormat == BarcodeFormat.AZTEC -> FragmentCreateAztec()
            barcodeFormat == BarcodeFormat.PDF_417 -> FragmentCreatePdf417()
            barcodeFormat == BarcodeFormat.CODABAR -> FragmentCreateCodabar()
            barcodeFormat == BarcodeFormat.CODE_39 -> FragmentCreateCode39()
            barcodeFormat == BarcodeFormat.CODE_93 -> FragmentCreateCode93()
            barcodeFormat == BarcodeFormat.CODE_128 -> FragmentCreateCode128()
            barcodeFormat == BarcodeFormat.EAN_8 -> FragmentCreateEan8()
            barcodeFormat == BarcodeFormat.EAN_13 -> FragmentCreateEan13()
            barcodeFormat == BarcodeFormat.ITF -> FragmentCreateItf14()
            barcodeFormat == BarcodeFormat.UPC_A -> FragmentCreateUpcA()
            barcodeFormat == BarcodeFormat.UPC_E -> FragmentCreateUpcE()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun choosePhone() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        startActivityForResultIfExists(intent, CHOOSE_PHONE_REQUEST_CODE)
    }

    private fun showChosenPhone(data: Intent?) {
        val phone = contactHelper.getPhone(this, data) ?: return
        getCurrentFragment().showPhone(phone)
    }

    private fun requestContactsPermissions() {
        permissionsHelper.requestPermissions(this, CONTACTS_PERMISSIONS, CONTACTS_PERMISSION_REQUEST_CODE)
    }

    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResultIfExists(intent, CHOOSE_CONTACT_REQUEST_CODE)
    }

    private fun showChosenContact(data: Intent?) {
        val contact = contactHelper.getContact(this, data) ?: return
        getCurrentFragment().showContact(contact)
    }

    private fun startActivityForResultIfExists(intent: Intent, requestCode: Int) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, requestCode)
        } else {
            Toast.makeText(this, R.string.activity_barcode_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBarcode() {
        val schema = getCurrentFragment().getBarcodeSchema()
        createBarcode(schema)
    }

    private fun createBarcode(schema: Schema, finish: Boolean = false) {
        val barcode = Barcode(
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = barcodeFormat,
            schema = schema.schema,
            date = System.currentTimeMillis(),
            isGenerated = true
        )

        if (settings.saveCreatedBarcodesToHistory.not()) {
            navigateToBarcodeScreen(barcode, finish)
            return
        }

        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    navigateToBarcodeScreen(barcode.copy(id = id), finish)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun getCurrentFragment(): FragmentBaseCreateBarcode {
        return supportFragmentManager.findFragmentById(R.id.container) as FragmentBaseCreateBarcode
    }

    private fun navigateToBarcodeScreen(barcode: Barcode, finish: Boolean) {
        ActivityBarcode.start(this, barcode, true)

        if (finish) {
            finish()
        }
    }
}
