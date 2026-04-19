package com.mckimquyen.barcodescanner.feature.tabs.setting.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.mckimquyen.barcodescanner.sdkadbmob.Logger
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.common.view.RadioButtonSettings
import com.mckimquyen.barcodescanner.usecase.LocaleHelper

class ChooseLanguageActivity : ActivityBase() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseLanguageActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val languageButtons = mutableListOf<RadioButtonSettings>()
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
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.dialog_change_language_title))
            .setMessage(getString(R.string.dialog_change_language_message))
            .setPositiveButton(getString(R.string.dialog_change_language_positive)) { _, _ ->
                // Save new language directly to SharedPreferences to ensure it's saved
                val sharedPreferences = getSharedPreferences("SHARED_PREFERENCES_NAME", MODE_PRIVATE)
                val success = sharedPreferences.edit()
                    .putString("LANGUAGE", newLanguageCode)
                    .commit() // Use commit() instead of apply() to ensure synchronous save

                Logger.i("Saved language: $newLanguageCode, success: $success")

                // Also save via Settings for consistency
                settings.language = newLanguageCode

                // Restart app to apply language
                restartApp()
            }
            .setNegativeButton(getString(R.string.dialog_change_language_negative)) { _, _ ->
                // User cancelled, revert selection
                selectedButton.isChecked = false
                showInitialSettings()
            }
            .setCancelable(false)
            .show()
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
