package com.mckimquyen.barcodescanner.feature.tabs.setting.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mckimquyen.barcodescanner.databinding.AChooseSearchEngineBinding
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.extension.unsafeLazy
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.common.view.RadioButtonSettings
import com.mckimquyen.barcodescanner.model.SearchEngine

class ChooseSearchEngineActivityBase : ActivityBase() {
    private lateinit var binding: AChooseSearchEngineBinding


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseSearchEngineActivityBase::class.java)
            context.startActivity(intent)
        }
    }

    private val buttons by unsafeLazy {
        listOf(
            binding.buttonNone,
            binding.buttonAskEveryTime,
            binding.buttonBing,
            binding.buttonDuckDuckGo,
            binding.buttonGoogle,
            binding.buttonQwant,
            binding.buttonStartPage,
            binding.buttonYahoo,
            binding.buttonYandex
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AChooseSearchEngineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initToolbar()
        showInitialValue()
        handleSettingsChanged()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showInitialValue() {
        when (settings.searchEngine) {
            SearchEngine.NONE -> binding.buttonNone.isChecked = true
            SearchEngine.ASK_EVERY_TIME -> binding.buttonAskEveryTime.isChecked = true
            SearchEngine.BING -> binding.buttonBing.isChecked = true
            SearchEngine.DUCK_DUCK_GO -> binding.buttonDuckDuckGo.isChecked = true
            SearchEngine.GOOGLE -> binding.buttonGoogle.isChecked = true
            SearchEngine.QWANT -> binding.buttonQwant.isChecked = true
            SearchEngine.STARTPAGE -> binding.buttonStartPage.isChecked = true
            SearchEngine.YAHOO -> binding.buttonYahoo.isChecked = true
            SearchEngine.YANDEX -> binding.buttonYandex.isChecked = true
        }
    }

    private fun handleSettingsChanged() {
        binding.buttonNone.setCheckedChangedListener(SearchEngine.NONE)
        binding.buttonAskEveryTime.setCheckedChangedListener(SearchEngine.ASK_EVERY_TIME)
        binding.buttonBing.setCheckedChangedListener(SearchEngine.BING)
        binding.buttonDuckDuckGo.setCheckedChangedListener(SearchEngine.DUCK_DUCK_GO)
        binding.buttonGoogle.setCheckedChangedListener(SearchEngine.GOOGLE)
        binding.buttonQwant.setCheckedChangedListener(SearchEngine.QWANT)
        binding.buttonStartPage.setCheckedChangedListener(SearchEngine.STARTPAGE)
        binding.buttonYahoo.setCheckedChangedListener(SearchEngine.YAHOO)
        binding.buttonYandex.setCheckedChangedListener(SearchEngine.YANDEX)
    }

    private fun RadioButtonSettings.setCheckedChangedListener(searchEngine: SearchEngine) {
        setCheckedChangedListener { isChecked ->
            if (isChecked) {
                uncheckOtherButtons(this)
                settings.searchEngine = searchEngine
            }
        }
    }

    private fun uncheckOtherButtons(checkedButton: View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}
