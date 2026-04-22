package com.mckimquyen.barcodescanner.feature.tabs.setting.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mckimquyen.barcodescanner.sdkadbmob.Logger
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.common.view.RadioButtonSettings
import com.mckimquyen.barcodescanner.usecase.LocaleHelper

class ChooseLanguageActivity : ActivityBase(), DialogFragmentChangeLanguage.Listener {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseLanguageActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val languageButtons = mutableListOf<RadioButtonSettings>()
    private var pendingLanguageCode: String? = null
    private var pendingSelectedButton: RadioButtonSettings? = null
    private lateinit var rootView: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var languagesContainer: LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_choose_language)

        rootView = findViewById(R.id.rootView)
        toolbar = findViewById(R.id.toolbar)
        languagesContainer = findViewById(R.id.languagesContainer)

        supportEdgeToEdge()
        initToolbar()
        createLanguageButtons()
    }

    override fun onResume() {
        super.onResume()
        showInitialSettings()
    }

    private fun supportEdgeToEdge() {
        rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun createLanguageButtons() {
        val supportedLanguages = LocaleHelper.getSupportedLanguages()

        supportedLanguages.forEach { (code, name) ->
            val button = RadioButtonSettings(this).apply {
                tag = code
            }

            // Set text to the TextView inside RadioButtonSettings
            val textView = button.findViewById<android.widget.TextView>(R.id.textViewText)
            textView?.text = name

            button.setCheckedChangedListener { isChecked ->
                if (isChecked) {
                    val oldLanguage = settings.language

                    // Only show dialog if language actually changed
                    if (oldLanguage != code) {
                        showLanguageChangeDialog(code, button)
                    }
                }
            }

            languageButtons.add(button)
            languagesContainer.addView(button)
        }
    }

    private fun showLanguageChangeDialog(newLanguageCode: String, selectedButton: RadioButtonSettings) {
        // Store selected button tag so we can revert on cancel
        pendingLanguageCode = newLanguageCode
        pendingSelectedButton = selectedButton
        val bs = DialogFragmentChangeLanguage.newInstance(
            languageCode = newLanguageCode,
            title = getString(R.string.dialog_change_language_title),
            message = getString(R.string.dialog_change_language_message),
            positive = getString(R.string.dialog_change_language_positive),
            negative = getString(R.string.dialog_change_language_negative),
        )
        bs.show(supportFragmentManager, "ChangeLanguage")
    }

    // Rotation-safe: called by DialogFragmentChangeLanguage.Listener interface
    override fun onLanguageChangeConfirmed(languageCode: String) {
        val sharedPreferences = getSharedPreferences("SHARED_PREFERENCES_NAME", MODE_PRIVATE)
        val success = sharedPreferences.edit().putString("LANGUAGE", languageCode).commit()
        Logger.i("Saved language: $languageCode, success: $success")
        settings.language = languageCode
        restartApp()
    }

    override fun onLanguageChangeCancelled() {
        pendingSelectedButton?.isChecked = false
        pendingSelectedButton = null
        pendingLanguageCode = null
        showInitialSettings()
    }

    private fun restartApp() {
        // Get launch intent
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Finish all activities and restart
        finishAffinity()
        startActivity(intent)

        // Exit process to ensure clean restart
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun showInitialSettings() {
        val currentLanguage = settings.language
        languageButtons.forEach { button ->
            button.isChecked = button.tag == currentLanguage
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Read language directly from SharedPreferences
        val sharedPreferences = newBase.getSharedPreferences("SHARED_PREFERENCES_NAME", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("LANGUAGE", "system") ?: "system"
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }
}
